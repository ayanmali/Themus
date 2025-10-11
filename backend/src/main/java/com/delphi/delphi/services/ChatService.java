package com.delphi.delphi.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.AssistantMessage.ToolCall;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.components.tools.ToolCallHandler;
import com.delphi.delphi.components.tools.Tools;
import com.delphi.delphi.dtos.cache.ChatMessageCacheDto;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.repositories.ChatMessageRepository;
import com.delphi.delphi.repositories.OpenAIToolCallRepository;
import com.delphi.delphi.repositories.OpenAIToolResponseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
/*
 * Service for managing user chat history and messages.
 * 
 * This service is used to create, get, update, and delete chat histories and
 * messages.
 * It is also used to add messages to a chat history.
 * It is also used to get messages by chat history id.
 * It is also used to get a message by id.
 * It is also used to create a message.
 * 
 * There are two different caches used here
 * 1. chat_messages - information about a given chat message
 * 2. chat_histories - the list of chat messages for an assessment
 * 
 * TODO: cache intermediate chat messages in Redis
 */
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    private final OpenAIToolCallRepository openAIToolCallRepository;

    private final OpenAIToolResponseRepository openAIToolResponseRepository;

    private final ChatModel chatModel;

    private final ToolCallHandler toolCallHandler;

    private final RedisService redisService;

    private final ObjectMapper objectMapper;

    private final boolean PARALLEL_TOOL_CALLS = true;

    // SSE emitter management
    private final Map<UUID, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatMessageRepository chatMessageRepository, ChatModel chatModel,
            OpenAIToolCallRepository openAIToolCallRepository,
            OpenAIToolResponseRepository openAIToolResponseRepository,
            ToolCallHandler toolCallHandler,
            RedisService redisService) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatModel;
        this.openAIToolCallRepository = openAIToolCallRepository;
        this.openAIToolResponseRepository = openAIToolResponseRepository;
        this.toolCallHandler = toolCallHandler;
        this.redisService = redisService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Spring AI Methods
     */

    /*
     * Get a chat completion from the AI model
     */
    // @Cacheable(value = "chatCompletions", key = "#chatHistoryId")
    /**
     * The initial message sent to the agent to plan the assessment.
     * Stops agent execution to wait for user confirmation/feedback
     */
    // @Cacheable(value = "chatCompletions", key = "#chatHistoryId")

    public String getRepoAnalysis(UUID jobId, List<Message> existingMessages, String userMessage,
            String model, Long assessmentId, String encryptedGithubToken, String githubUsername,
            String githubRepoName, Tools tools, String preset) {
        return getRepoAnalysis(jobId, existingMessages, new UserMessage(userMessage), model, assessmentId,
                encryptedGithubToken, githubUsername, githubRepoName, tools, preset);
    }

    public String getRepoAnalysis(UUID jobId, List<Message> existingMessages, Message userMessage,
            String model, Long assessmentId, String encryptedGithubToken, String githubUsername,
            String githubRepoName, Tools tools, String preset) {
        try {
            String finalResult = "";
            String notes = "";
            if (userMessage == null || userMessage.getText().isEmpty()) {
                throw new Exception("User message cannot be empty");
            }
            boolean endConversation = false;

            // TODO: create a cache in Redis to temporarily store new messages until they
            // are added to the chat_messages cache
            List<Message> newMessages = new ArrayList<>();
            /*
             * Entire context window represented as:
             * Stream.concat(existingMessages.stream(), newMessages.stream()).toList()
             */

            // update conversation history in memory
            // TODO: add the user message to the cache in Redis
            newMessages.add(userMessage);

            // printing all messages in context window
            log.info("--------------------------------");
            log.info("MESSAGES IN CONTEXT WINDOW:");
            for (Message message : Stream.concat(existingMessages.stream(), newMessages.stream()).toList()) {
                log.info("Message Type: {}", message.getMessageType().toString());
                String messageText = message.getText();
                if (messageText != null) {
                    log.info("Message: {}...{}", messageText.substring(0, Math.min(messageText.length(), 50)),
                            messageText.substring(Math.max(messageText.length() - 30, 0)));
                } else {
                    log.info("Message: null");
                }
                log.info("Message Metadata: {}", message.getMetadata().toString());
            }
            log.info("--------------------------------");

            // creating a prompt with a system message and a user message
            OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .model(String.format("%s@preset/%s", model, preset))
                    .toolCallbacks(ToolCallbacks.from(tools))
                    .toolChoice(OpenAiApi.ChatCompletionRequest.ToolChoiceBuilder.AUTO)
                    .internalToolExecutionEnabled(false) // disable framework-enabled tool execution
                    .parallelToolCalls(PARALLEL_TOOL_CALLS)
                    .build();

            // TODO: load newmessages from redis cache
            Prompt prompt = new Prompt(
                    Stream.concat(existingMessages.stream(), newMessages.stream()).toList(),
                    chatOptions);

            // adding LLM response to chat history
            int count = 0;
            ChatResponse response;
            // Retrying in case the LLM generates a faulty response
            try {
                response = chatModel.call(prompt);
            } catch (RestClientException e) {
                log.error("Error calling OpenRouter via Spring AI: {}. Retrying...", e.getMessage(), e);
                try {
                    response = chatModel.call(prompt);
                } catch (Exception ex) {
                    log.error("Error calling OpenRouter via Spring AI: {}", ex.getMessage(), ex);
                    throw new RuntimeException("Failed to get completion from AI service: " + ex.getMessage(), ex);
                }
            }
            log.info("Response created");
            for (Generation generation : response.getResults()) {
                log.info("--------------------------------");
                log.info("GENERATION {}:", count);
                log.info("GENERATION MESSAGE TYPE: {}", generation.getOutput().getMessageType().toString());
                log.info("Generation Text: {}", generation.getOutput().getText().substring(0,
                        Math.min(generation.getOutput().getText().length(), 50)) + "...");
                log.info("Generation Tool Calls: {}", generation.getOutput().getToolCalls().toString());
                log.info("--------------------------------");

                // TODO: dont add message to chat history if it wasnt generated by the
                // sendMessageToUser tool call?
                if (!generation.getOutput().hasToolCalls()) {
                    // TODO: add the message to the newMessagescache in Redis
                    newMessages.add(generation.getOutput());
                    sendSseEvent(jobId, "message", generation.getOutput());
                }
                count++;
            }

            // Agent loop
            // NOTE: I dont think we need to get tool responses for sendMessageToUser tool
            // calls since it would trigger the LLM again and cause it to send another
            // message back to user
            while (response.hasToolCalls() && !endConversation) {
                try {
                    // TODO: make tool calls asynchronous (virtual threads or @async annotations)
                    for (Generation generation : response.getResults()) {
                        log.info("--------------------------------");
                        log.info("GENERATION MESSAGE TYPE: {}", generation.getOutput().getMessageType().toString());
                        log.info("Generation Text: {}", generation.getOutput().getText().substring(0,
                                Math.min(generation.getOutput().getText().length(), 50)) + "...");
                        log.info("Generation Tool Calls: {}", generation.getOutput().getToolCalls().toString());
                        log.info("--------------------------------");

                        // TODO: add the message to the newMessagescache in Redis
                        // if generation.getOutput() contains a sendMessageToUser tool call, add the
                        // text argument to the assistant message text field
                        AssistantMessage asstMsg = getGenerationOutput(generation.getOutput());
                        newMessages.add(asstMsg);
                        sendSseEvent(jobId, "message", asstMsg);

                        // sendSseEvent(jobId, "message", generation.getOutput());

                        log.info("Going through tool calls...");
                        List<ToolResponse> toolResponses = new ArrayList<>();
                        for (ToolCall toolCall : generation.getOutput().getToolCalls()) {
                            log.info("Tool Call Name: {}", toolCall.name());
                            log.info("Tool Call Arguments: {}", toolCall.arguments());

                            // executeToolCall should return a ToolResponse object
                            log.info("Executing tool call: {}", toolCall.name());
                            ToolResponse toolResponse = toolCallHandler.executeToolCall(toolCall, encryptedGithubToken,
                                    githubUsername, githubRepoName);
                            if (toolCall.name().equals("returnRepositoryAnalysis")) {
                                log.info("Detected returnRepositoryAnalysis tool call - stopping conversation");
                                endConversation = true;
                                finalResult = toolResponse.responseData();
                                break;
                            }
                            if (toolCall.name().equals("addNote")) {
                                log.info("Detected addNote tool call - adding note to final result");
                                notes += toolResponse.responseData();
                                notes += "\n";
                            }
                            if (toolCall.name().equals("getNotes")) {
                                toolResponse = new ToolResponse(toolCall.id(), toolCall.name(), notes);
                            }
                            if (toolResponse != null) {
                                toolResponses.add(toolResponse);
                            }
                            // addMessageToChatHistory(new ToolResponseMessage(/* List of ToolResponse
                            // objects here */), assessment, model);
                        }
                        // generate a tool response message
                        // TODO: add the message to the newMessagescache in Redis
                        // dont add addNote tool calls to the context window
                        newMessages.add(new ToolResponseMessage(toolResponses.stream().filter(toolResponse -> !toolResponse.name().equals("addNote")).collect(Collectors.toList())));
                        sendSseEvent(jobId, "message", new ToolResponseMessage(toolResponses));
                    }

                    if (endConversation) {
                        break;
                    }

                    // Getting the next response from the LLM
                    // TODO: load newmessages from redis cache
                    prompt = new Prompt(Stream.concat(existingMessages.stream(), newMessages.stream()).toList(),
                            chatOptions);

                    response = chatModel.call(prompt);

                } catch (Exception e) {
                    log.error("Error executing tool calls: {}", e.getMessage(), e);
                    throw e;
                }
            }
            // agent loop is finished executing
            log.info("--------------------------------");

            // TODO: add each message in newMessages to chat_messages cache
            // ...
            // save new messages to primary DB (batch write)
            return finalResult;
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    public List<ChatMessageCacheDto> getChatCompletion(UUID jobId, List<Message> existingMessages, String userMessage,
            String model, Long assessmentId, String encryptedGithubToken, String githubUsername,
            String githubRepoName, Tools tools, String preset) {
        try {
            return getChatCompletion(jobId, existingMessages, new UserMessage(userMessage), model, assessmentId,
                    encryptedGithubToken, githubUsername, githubRepoName, tools, preset);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    public List<ChatMessageCacheDto> getChatCompletion(UUID jobId, List<Message> existingMessages, Message userMessage,
            String model, Long assessmentId, String encryptedGithubToken, String githubUsername,
            String githubRepoName, Tools tools, String preset) {
        try {
            if (userMessage == null || userMessage.getText().isEmpty()) {
                throw new Exception("User message cannot be empty");
            }
            boolean endConversation = false;

            // TODO: create a cache in Redis to temporarily store new messages until they
            // are added to the chat_messages cache
            List<Message> newMessages = new ArrayList<>();
            /*
             * Entire context window represented as:
             * Stream.concat(existingMessages.stream(), newMessages.stream()).toList()
             */

            // update conversation history in memory
            // TODO: add the user message to the cache in Redis
            newMessages.add(userMessage);

            // printing all messages in context window
            log.info("--------------------------------");
            log.info("MESSAGES IN CONTEXT WINDOW:");
            for (Message message : Stream.concat(existingMessages.stream(), newMessages.stream()).toList()) {
                log.info("Message Type: {}", message.getMessageType().toString());
                String messageText = message.getText();
                if (messageText != null) {
                    log.info("Message: {}...{}", messageText.substring(0, Math.min(messageText.length(), 50)),
                            messageText.substring(Math.max(messageText.length() - 30, 0)));
                } else {
                    log.info("Message: null");
                }
                log.info("Message Metadata: {}", message.getMetadata().toString());
            }
            log.info("--------------------------------");

            // creating a prompt with a system message and a user message
            
            OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .model(String.format("%s@preset/%s", model, preset))
                    .toolCallbacks(ToolCallbacks.from(tools))
                    .toolChoice(OpenAiApi.ChatCompletionRequest.ToolChoiceBuilder.AUTO)
                    .internalToolExecutionEnabled(false) // disable framework-enabled tool execution
                    .parallelToolCalls(PARALLEL_TOOL_CALLS)
                    .build();

            // TODO: load newmessages from redis cache
            Prompt prompt = new Prompt(
                    Stream.concat(existingMessages.stream(), newMessages.stream()).toList(),
                    chatOptions);

            // adding LLM response to chat history
            int count = 0;
            ChatResponse response;
            // Retrying in case the LLM generates a faulty response
            try {
                response = chatModel.call(prompt);
            } catch (RestClientException e) {
                log.error("Error calling OpenRouter via Spring AI: {}. Retrying...", e.getMessage(), e);
                try {
                    response = chatModel.call(prompt);
                } catch (Exception ex) {
                    log.error("Error calling OpenRouter via Spring AI: {}", ex.getMessage(), ex);
                    throw new RuntimeException("Failed to get completion from AI service: " + ex.getMessage(), ex);
                }
            }
            log.info("Response created");
            for (Generation generation : response.getResults()) {
                log.info("--------------------------------");
                log.info("GENERATION {}:", count);
                log.info("GENERATION MESSAGE TYPE: {}", generation.getOutput().getMessageType().toString());
                log.info("Generation Text: {}", generation.getOutput().getText().substring(0,
                        Math.min(generation.getOutput().getText().length(), 50)) + "...");
                log.info("Generation Tool Calls: {}", generation.getOutput().getToolCalls().toString());
                log.info("--------------------------------");

                // TODO: dont add message to chat history if it wasnt generated by the
                // sendMessageToUser tool call?
                if (!generation.getOutput().hasToolCalls()) {
                    // TODO: add the message to the newMessagescache in Redis
                    newMessages.add(generation.getOutput());
                    sendSseEvent(jobId, "message", generation.getOutput());
                }
                count++;
            }

            // Agent loop
            // NOTE: I dont think we need to get tool responses for sendMessageToUser tool
            // calls since it would trigger the LLM again and cause it to send another
            // message back to user
            while (response.hasToolCalls() && !endConversation) {
                try {
                    // TODO: make tool calls asynchronous (virtual threads or @async annotations)
                    for (Generation generation : response.getResults()) {
                        log.info("--------------------------------");
                        log.info("GENERATION MESSAGE TYPE: {}", generation.getOutput().getMessageType().toString());
                        log.info("Generation Text: {}", generation.getOutput().getText().substring(0,
                                Math.min(generation.getOutput().getText().length(), 50)) + "...");
                        log.info("Generation Tool Calls: {}", generation.getOutput().getToolCalls().toString());
                        log.info("--------------------------------");

                        // TODO: add the message to the newMessagescache in Redis
                        // if generation.getOutput() contains a sendMessageToUser tool call, add the
                        // text argument to the assistant message text field
                        AssistantMessage asstMsg = getGenerationOutput(generation.getOutput());
                        newMessages.add(asstMsg);
                        sendSseEvent(jobId, "message", asstMsg);

                        // sendSseEvent(jobId, "message", generation.getOutput());

                        log.info("Going through tool calls...");
                        List<ToolResponse> toolResponses = new ArrayList<>();
                        for (ToolCall toolCall : generation.getOutput().getToolCalls()) {
                            log.info("Tool Call Name: {}", toolCall.name());
                            log.info("Tool Call Arguments: {}", toolCall.arguments());
                            if (toolCall.name().equals("sendMessageToUser")) {
                                log.info("Detected sendMessageToUser tool call - stopping conversation");
                                endConversation = true;
                                break;
                            }
                            // executeToolCall should return a ToolResponse object
                            log.info("Executing tool call: {}", toolCall.name());
                            ToolResponse toolResponse = toolCallHandler.executeToolCall(toolCall, encryptedGithubToken,
                                    githubUsername, githubRepoName);
                            if (toolResponse != null) {
                                toolResponses.add(toolResponse);
                            }
                            // addMessageToChatHistory(new ToolResponseMessage(/* List of ToolResponse
                            // objects here */), assessment, model);
                        }
                        // generate a tool response message
                        // TODO: add the message to the newMessagescache in Redis
                        newMessages.add(new ToolResponseMessage(toolResponses));
                        sendSseEvent(jobId, "message", new ToolResponseMessage(toolResponses));
                    }

                    if (endConversation) {
                        break;
                    }

                    // Getting the next response from the LLM
                    // TODO: load newmessages from redis cache
                    prompt = new Prompt(Stream.concat(existingMessages.stream(), newMessages.stream()).toList(),
                            chatOptions);

                    response = chatModel.call(prompt);

                } catch (Exception e) {
                    log.error("Error executing tool calls: {}", e.getMessage(), e);
                    throw e;
                }
            }
            // agent loop is finished executing
            log.info("--------------------------------");

            // TODO: add each message in newMessages to chat_messages cache
            // ...
            // save new messages to primary DB (batch write)
            return addMessagesToChatHistory(newMessages, assessmentId, model);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    private AssistantMessage getGenerationOutput(AssistantMessage output) {
        List<ToolCall> toolCalls = output.getToolCalls();
        if (toolCalls != null && !toolCalls.isEmpty()) {
            for (ToolCall toolCall : toolCalls) {
                if ("sendMessageToUser".equals(toolCall.name())) {
                    try {
                        // Parse the JSON arguments string
                        String argumentsJson = toolCall.arguments();
                        Map<String, Object> args = objectMapper.readValue(argumentsJson,
                                new TypeReference<Map<String, Object>>() {
                                });

                        // Extract the message content
                        Object messageObj = args.get("message");
                        if (messageObj instanceof String messageText && !messageText.isBlank()) {
                            // Return new AssistantMessage with the extracted text
                            return new AssistantMessage(messageText, output.getMetadata());
                        }
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing sendMessageToUser arguments: {}", e.getMessage());
                        // Fallback: return original message if parsing fails
                    }
                }
            }
        }
        return output;
    }

    /**
     * The initial message sent to the agent to plan the assessment.
     * Stops agent execution to wait for user confirmation/feedback
     */
    // @Cacheable(value = "chatCompletions", key = "#chatHistoryId")
    public List<ChatMessageCacheDto> getChatCompletion(UUID jobId, List<Message> existingMessages,
            String userPromptTemplateMessage, Map<String, Object> userPromptVariables,
            String model, Long assessmentId, String encryptedGithubToken, String githubUsername,
            String githubRepoName, Tools tools, String preset) {
        try {
            // Input validation
            if (userPromptTemplateMessage == null || userPromptTemplateMessage.isEmpty()) {
                throw new Exception("User prompt template message cannot be empty");
            }
            if (userPromptVariables == null || userPromptVariables.isEmpty()) {
                throw new Exception("User prompt variables cannot be empty");
            }
            PromptTemplate userPromptTemplate = new PromptTemplate(userPromptTemplateMessage);
            Message userMessage = userPromptTemplate.createMessage(userPromptVariables);

            return getChatCompletion(jobId, existingMessages, userMessage, model, assessmentId, encryptedGithubToken,
                    githubUsername, githubRepoName, tools, preset);
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    // public ChatResponse testAgent(String userMessage, String model) {
    // log.info("Sending prompt to OpenRouter model '{}':\nUSER MESSAGE: '{}'",
    // model, userMessage);
    // try {
    // OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
    // .model(model)
    // .toolCallbacks(ToolCallbacks.from(testTools))
    // .internalToolExecutionEnabled(false)
    // .toolChoice(OpenAiApi.ChatCompletionRequest.ToolChoiceBuilder.AUTO)
    // //
    // .toolChoice(OpenAiApi.ChatCompletionRequest.ToolChoiceBuilder.FUNCTION("get_weather"))
    // // .internalToolExecutionEnabled(false) // disable framework-enabled tool
    // // execution
    // .parallelToolCalls(PARALLEL_TOOL_CALLS)
    // .build();
    // Prompt prompt = new Prompt(
    // // convert the messages to Spring AI messages
    // List.of(
    // new SystemMessage(
    // "You are a helpful assistant. You have access to the get_weather tool. This
    // tool returns the weather for a given city as an integerin degrees Celsius.
    // The city must be in the format of 'City, State'. For example, 'New York,
    // NY'."),
    // new UserMessage(userMessage)),
    // chatOptions);

    // // Create a new prompt with a user message
    // // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
    // // .model(model)
    // // .build());

    // // adding LLM response to chat history
    // ChatResponse response = chatModel.call(prompt);
    // log.info("--------------------------------");
    // log.info("RAW RESPONSE:");
    // log.info("RAW RESPONSE METADATA: {}",
    // response.getResult().getMetadata().toString());
    // log.info("RAW RESPONSE MESSAGE TYPE: {}",
    // response.getResult().getOutput().getMessageType().toString());
    // log.info("RAW RESPONSE TEXT: {}",
    // response.getResult().getOutput().getText());
    // log.info("RAW RESPONSE TOOL CALLS: {}",
    // response.getResult().getOutput().getToolCalls().toString());
    // log.info("--------------------------------");

    // // save tool calls to DB
    // // response.getResult().getOutput().getToolCalls()

    // log.info("--------------------------------");
    // log.info("TOOL CALLS:");
    // while (response.hasToolCalls()) {
    // ToolExecutionResult toolExecutionResult =
    // toolCallingManager.executeToolCalls(prompt, response);

    // log.info("--------------------------------");
    // log.info("TOOL EXECUTION RESULT:");
    // log.info("TOOL EXECUTION RESULT CONVERSATION HISTORY: {}",
    // toolExecutionResult.conversationHistory().toString());
    // log.info("--------------------------------");
    // prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);

    // response = chatModel.call(prompt);
    // // add message to chat history
    // log.info("--------------------------------");
    // log.info("RESPONSE:");
    // log.info("RESPONSE METADATA: {}",
    // response.getResult().getMetadata().toString());
    // log.info("RESPONSE MESSAGE TYPE: {}",
    // response.getResult().getOutput().getMessageType().toString());
    // log.info("RESPONSE TEXT: {}", response.getResult().getOutput().getText());
    // log.info("RESPONSE TOOL CALLS: {}",
    // response.getResult().getOutput().getToolCalls().toString());
    // }
    // log.info("--------------------------------");

    // // log.info("Response: {}", response.getResults().stream().map(r ->
    // // r.getOutput().getText()).collect(Collectors.joining("\n\n")));
    // // for (Generation generation : response.getResults()) {
    // // log.info("Generation {}: {}", count,
    // // generation.getOutput().getText().substring(0,
    // // Math.min(generation.getOutput().getText().length(), 100)) + "...");
    // // count++;
    // // }
    // return response;
    // } catch (Exception e) {
    // log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
    // throw new RuntimeException("Failed to get completion from AI service: " +
    // e.getMessage(), e);
    // }
    // }

    // /*
    // * Get completion w/ a template system prompt string)
    // * Surround variables with curly braces in the system prompt template ("{ }")
    // */
    // public ChatResponse getChatCompletion(String systemMessageTemplate,
    // Map<String, Object> systemMessageVariables, String userMessage, String model)
    // {
    // log.info("Sending prompt to OpenRouter model '{}':\nSYSTEM PROMPT: '{}'\nUSER
    // MESSAGE: '{}'",
    // model,
    // systemMessageTemplate.substring(0, Math.min(systemMessageTemplate.length(),
    // 100)) + "...",
    // userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
    // try {
    // // The Spring AI ChatModel handles the call to OpenRouter based on
    // application.properties

    // // call a chat model with a string user message
    // // ChatResponse response = chatModel.call(
    // // new Prompt(
    // // userMessage,
    // // OpenAiChatOptions.builder()
    // // .model(model)
    // // .build()));

    // // Create a system message from a template and substitute the values
    // SystemPromptTemplate systemPromptTemplate = new
    // SystemPromptTemplate(systemMessageTemplate);
    // Message systemMessage =
    // systemPromptTemplate.createMessage(systemMessageVariables);

    // // creating a prompt with a system message and a user message
    // Prompt prompt = new Prompt(
    // List.of(
    // systemMessage,
    // new UserMessage(userMessage)),
    // OpenAiChatOptions.builder()
    // .model(model)
    // .temperature(TEMPERATURE) // double between 0 and 1
    // .build()
    // );

    // // Create a new prompt with a user message
    // // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
    // // .model(model)
    // // .build());

    // return chatModel.call(prompt);
    // } catch (Exception e) {
    // log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
    // throw new RuntimeException("Failed to get completion from AI service: " +
    // e.getMessage(), e);
    // }
    // }

    // /*
    // * Pass in a system prompt message (no template) and a user message
    // */
    // public ChatResponse getChatCompletion(String systemPromptMessage, String
    // userMessage, String model) {
    // log.info("Sending prompt to OpenRouter model '{}':\nSYSTEM PROMPT: '{}'\nUSER
    // MESSAGE: '{}'",
    // model,
    // systemPromptMessage.substring(0, Math.min(systemPromptMessage.length(), 100))
    // + "...",
    // userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
    // try {
    // // The Spring AI ChatModel handles the call to OpenRouter based on
    // application.properties

    // // call a chat model with a string user message
    // // ChatResponse response = chatModel.call(
    // // new Prompt(
    // // userMessage,
    // // OpenAiChatOptions.builder()
    // // .model(model)
    // // .build()));

    // // Create a system message from a template and substitute the values

    // // creating a prompt with a system message and a user message
    // Prompt prompt = new Prompt(
    // List.of(
    // new SystemMessage(systemPromptMessage),
    // new UserMessage(userMessage)),
    // OpenAiChatOptions.builder()
    // .model(model)
    // .temperature(TEMPERATURE) // double between 0 and 1
    // .build()
    // );

    // // Create a new prompt with a user message
    // // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
    // // .model(model)
    // // .build());

    // return chatModel.call(prompt);
    // } catch (Exception e) {
    // log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
    // throw new RuntimeException("Failed to get completion from AI service: " +
    // e.getMessage(), e);
    // }
    // }

    /*
     * Database Repository Methods
     */

    // @CachePut(value = "chatHistories", key = "#chatHistory.id")
    // public ChatHistory createChatHistory(ChatHistory chatHistory, String
    // systemMessage) throws Exception {
    // // adding system prompt to chat history
    // log.info("Creating chat history with system message: {}",
    // systemMessage.substring(0, Math.min(systemMessage.length(), 100)) + "...");
    // ChatHistory savedChatHistory = chatHistoryRepository.save(chatHistory);
    // log.info("Chat history saved in DB with id: {} - adding system message to
    // chat history...", savedChatHistory.getId());
    // addMessageToChatHistory(systemMessage, MessageType.SYSTEM, List.of(),
    // savedChatHistory, "N/A");
    // log.info("System message added to chat history in DB with id: {}",
    // savedChatHistory.getId());
    // // save chat history
    // return savedChatHistory;
    // }

    // @Cacheable(value = "chatHistories", key = "#id")
    // public ChatHistory getChatHistoryById(Long id) throws Exception {
    // try {
    // return chatHistoryRepository.findById(id)
    // .orElseThrow(() -> new Exception("Chat history not found with id: " + id));
    // } catch (Exception e) {
    // throw new Exception("Chat history not found with id: " + id);
    // }
    // }

    // @Cacheable(value = "chatHistories", key = "#assessmentId")
    // public ChatHistory getChatHistoryByAssessmentId(Long assessmentId) throws
    // Exception {
    // try {
    // return chatHistoryRepository.findByAssessmentId(assessmentId);
    // } catch (Exception e) {
    // throw new Exception("Chat history not found with assessment id: " +
    // assessmentId);
    // }
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatHistory updateChatHistory(Long id, ChatHistory chatHistory) throws
    // Exception {
    // ChatHistory existingChatHistory = getChatHistoryById(id);
    // existingChatHistory.setAssessment(chatHistory.getAssessment());
    // return chatHistoryRepository.save(existingChatHistory);
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatHistory updateChatHistory(Long id, Assessment assessment) throws
    // Exception {
    // ChatHistory existingChatHistory = getChatHistoryById(id);
    // existingChatHistory.setAssessment(assessment);
    // return chatHistoryRepository.save(existingChatHistory);
    // }

    // @CacheEvict(value = "chatHistories", key = "#id")
    // public void deleteChatHistory(Long id) throws Exception {
    // ChatHistory existingChatHistory = getChatHistoryById(id);
    // chatHistoryRepository.delete(existingChatHistory);
    // }

    // @Cacheable(value = "chatHistories")
    // public List<ChatHistory> getAllChatHistories() {
    // return chatHistoryRepository.findAll();
    // }

    /**
     * Storing in memory conversation history to primary DB
     * Writing a batch of chat messages to PostgreSQL at a time
     */
    public List<ChatMessageCacheDto> addMessagesToChatHistory(List<Message> messages, Long assessmentId, String model) {
        List<ChatMessageCacheDto> savedDtos = new ArrayList<>();
        String assessmentCacheKey = "cache:chat_messages:assessment:" + assessmentId;
        List<ChatMessageCacheDto> existingMessages = getMessagesByAssessmentId(assessmentId);

        for (Message message : messages) {
            // Ignore empty assistant messages with no tool calls
            switch (message) {
                case AssistantMessage assistantMessage -> {
                    boolean noText = assistantMessage.getText() == null || assistantMessage.getText().isBlank();
                    boolean noToolCalls = assistantMessage.getToolCalls() == null
                            || assistantMessage.getToolCalls().isEmpty();
                    if (noText && noToolCalls) {
                        log.info("AssistantMessage text and tool calls are empty or null, skipping...");
                        continue;
                    }
                }
                case ToolResponseMessage toolResponseMessage -> {
                    boolean noText = toolResponseMessage.getText() == null || toolResponseMessage.getText().isBlank();
                    boolean noResponses = toolResponseMessage.getResponses() == null
                            || toolResponseMessage.getResponses().isEmpty();
                    if (noText && noResponses) {
                        log.info("ToolResponseMessage text and tool responses are empty or null, skipping...");
                        continue;
                    }
                }
                default -> {
                }
            }

            // Persist the chat message
            ChatMessage savedChatMessage = chatMessageRepository.save(new ChatMessage(message, assessmentId, model));

            // 1. Update individual message cache
            redisService.set("cache:chat_messages:message:" + savedChatMessage.getId(),
                    new ChatMessageCacheDto(savedChatMessage));

            // 2. Update assessment cache by adding the new message to the existing list

            // Add the new message to the existing list
            existingMessages.add(new ChatMessageCacheDto(savedChatMessage));

            // Update the assessment cache with the updated list
            redisService.set(assessmentCacheKey, existingMessages);
            // Persist tool calls or tool responses depending on message type
            switch (savedChatMessage.getMessageType()) {
                case MessageType.ASSISTANT -> {
                    log.info("ADDING ASSISTANT MESSAGE TO CHAT HISTORY");
                    if (savedChatMessage.getToolCalls() != null && !savedChatMessage.getToolCalls().isEmpty()) {
                        savedChatMessage.getToolCalls().stream().map(toolCall -> {
                            toolCall.setChatMessage(savedChatMessage);
                            return openAIToolCallRepository.save(toolCall);
                        }).collect(Collectors.toList());
                    }
                }
                case MessageType.USER -> {
                    log.info("ADDING USER MESSAGE TO CHAT HISTORY");
                }
                case MessageType.TOOL -> {
                    log.info("ADDING TOOL RESPONSE MESSAGE TO CHAT HISTORY");
                    if (savedChatMessage.getToolResponses() != null && !savedChatMessage.getToolResponses().isEmpty()) {
                        savedChatMessage.getToolResponses().stream().map(toolResponse -> {
                            toolResponse.setChatMessage(savedChatMessage);
                            return openAIToolResponseRepository.save(toolResponse);
                        }).collect(Collectors.toList());
                    }
                }
                default ->
                    throw new IllegalArgumentException("Invalid message type: " + savedChatMessage.getMessageType());
            }

            savedDtos.add(new ChatMessageCacheDto(savedChatMessage));

        }

        log.info("Saved {} chat messages to DB", savedDtos.size());
        return savedDtos;
    }

    /**
     * Deprecated
     * Storing in memory conversation history to DB
     */
    // public ChatMessageCacheDto addMessageToChatHistory(AbstractMessage message,
    // Assessment assessment, String model)
    // throws Exception {
    // if (message.getText() == null || message.getText().isEmpty() ||
    // message.getText().isBlank()) {
    // if (message instanceof AssistantMessage assistantMessage
    // && (assistantMessage.getToolCalls() == null
    // || assistantMessage.getToolCalls().isEmpty())) {
    // log.info("AssistantMessage text is empty or null and tool calls are empty or
    // null, skipping...");
    // return null;
    // }
    // else if (message instanceof ToolResponseMessage toolResponseMessage
    // && (toolResponseMessage.getResponses() == null
    // || toolResponseMessage.getResponses().isEmpty())) {
    // log.info("ToolResponseMessage text is empty or null and tool responses are
    // empty or null, skipping...");
    // return null;
    // }
    // }

    // ChatMessage chatMessage = new ChatMessage(message, assessment, model);

    // // Save the ChatMessage first to get its ID
    // ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

    // switch (message.getMessageType()) {
    // case MessageType.ASSISTANT -> {
    // log.info("ADDING ASSISTANT MESSAGE TO CHAT HISTORY");
    // // save tool calls in DB after ChatMessage is saved
    // if (savedChatMessage.getToolCalls() != null &&
    // !savedChatMessage.getToolCalls().isEmpty()) {
    // savedChatMessage.getToolCalls().stream().map(toolCall -> {
    // toolCall.setChatMessage(savedChatMessage);
    // return openAIToolCallRepository.save(toolCall);
    // }).collect(Collectors.toList());
    // }
    // break;
    // }
    // case MessageType.USER -> {
    // log.info("ADDING USER MESSAGE TO CHAT HISTORY");
    // break;
    // }
    // // case MessageType.SYSTEM -> {
    // // return addMessageToChatHistory(new ChatMessage(message, assessment,
    // // message.getModel()));
    // // }
    // case MessageType.TOOL -> {
    // log.info("ADDING TOOL RESPONSE MESSAGE TO CHAT HISTORY");
    // // save tool responses in DB after ChatMessage is saved
    // if (savedChatMessage.getToolResponses() != null &&
    // !savedChatMessage.getToolResponses().isEmpty()) {
    // savedChatMessage.getToolResponses().stream().map(toolResponse -> {
    // toolResponse.setChatMessage(savedChatMessage);
    // return openAIToolResponseRepository.save(toolResponse);
    // }).collect(Collectors.toList());
    // }
    // break;
    // }
    // default -> {
    // throw new IllegalArgumentException("Invalid message type: " +
    // message.getMessageType());
    // }
    // }

    // return new ChatMessageCacheDto(savedChatMessage);
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatMessageCacheDto addMessageToChatHistory(ChatMessage message)
    // throws Exception {
    // if (message.getText() == null || message.getText().isEmpty() ||
    // message.getText().isBlank()) {
    // log.info("Message text is empty or null, skipping...");
    // return null;
    // }
    // log.info("--------------------------------");
    // log.info("ADDING MESSAGE TO CHAT HISTORY - CHATMESSAGE MESSAGE:");
    // log.info("Message text: {}", message.getText());
    // log.info("Message type: {}", message.getMessageType());
    // log.info("Model: {}", message.getModel());
    // log.info("Assessment ID: {}", message.getAssessment().getId());
    // log.info("Tool calls: {}", message.getToolCalls());

    // Assessment assessment =
    // assessmentService.findById(message.getAssessment().getId())
    // .orElseThrow(() -> new Exception("Assessment not found with id: " +
    // message.getAssessment().getId()));

    // // Save the ChatMessage first to get its ID
    // ChatMessage savedMessage = chatMessageRepository.save(message);
    // log.info("ChatMessage saved with ID: {}", savedMessage.getId());

    // // Handle tool calls if they exist and aren't already saved
    // if (savedMessage.getToolCalls() != null &&
    // !savedMessage.getToolCalls().isEmpty()) {
    // List<OpenAiToolCall> savedToolCalls = new ArrayList<>();
    // for (OpenAiToolCall toolCall : savedMessage.getToolCalls()) {
    // if (toolCall.getId() == null) { // Only save if not already saved
    // toolCall.setChatMessage(savedMessage); // Link to the saved ChatMessage
    // OpenAiToolCall savedToolCall = openAIToolCallRepository.save(toolCall);
    // savedToolCalls.add(savedToolCall);
    // log.info("Tool call saved with ID: {}", savedToolCall.getId());
    // } else {
    // savedToolCalls.add(toolCall);
    // }
    // }
    // savedMessage.setToolCalls(savedToolCalls);
    // }

    // assessment.addMessage(savedMessage);
    // assessmentService.save(assessment);
    // log.info("ChatMessage added to assessment chat history in DB with id: {}",
    // savedMessage.getId());

    // return new ChatMessageCacheDto(savedMessage);
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatMessageCacheDto addMessageToChatHistory(AssistantMessage message,
    // Assessment assessment, String model) throws Exception {
    // if (message.getText() == null || message.getText().isEmpty() ||
    // message.getText().isBlank()) {
    // log.info("Message text is empty or null, skipping...");
    // return null;
    // }
    // log.info("--------------------------------");
    // log.info("ADDING MESSAGE TO CHAT HISTORY - SPRING AI ASSISTANTMESSAGE:");
    // log.info("Message text: {}", message.getText());
    // log.info("Message type: {}", message.getMessageType());
    // log.info("Model: {}", model);
    // log.info("Assessment ID: {}", assessment.getId());
    // for (ToolCall toolCall : message.getToolCalls()) {
    // log.info("Tool call: {}", toolCall.name());
    // log.info("Tool call arguments: {}", toolCall.arguments());
    // log.info("Tool call id: {}", toolCall.id());
    // }

    // // Create ChatMessage first
    // ChatMessage chatMessage = new ChatMessage(message, assessment, model);

    // // Save the ChatMessage first to get its ID
    // chatMessage = chatMessageRepository.save(chatMessage);
    // log.info("ChatMessage saved with ID: {}", chatMessage.getId());

    // // Now handle tool calls if they exist
    // if (message.getToolCalls() != null && !message.getToolCalls().isEmpty()) {
    // List<OpenAiToolCall> savedToolCalls = new ArrayList<>();
    // for (ToolCall toolCall : message.getToolCalls()) {
    // OpenAiToolCall openAiToolCall = new OpenAiToolCall(toolCall);
    // openAiToolCall.setChatMessage(chatMessage); // Link to the saved ChatMessage
    // OpenAiToolCall savedToolCall = openAIToolCallRepository.save(openAiToolCall);
    // savedToolCalls.add(savedToolCall);
    // log.info("Tool call saved with ID: {}", savedToolCall.getId());
    // }
    // chatMessage.setToolCalls(savedToolCalls);
    // }

    // // Add message to assessment and save
    // assessment.addMessage(chatMessage);
    // assessmentService.save(assessment);
    // log.info("ChatMessage added to assessment chat history in DB with id: {}",
    // chatMessage.getId());

    // return new ChatMessageCacheDto(chatMessage);
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatMessageCacheDto addMessageToChatHistory(String text, MessageType
    // messageType, List<OpenAiToolCall> toolCalls, Assessment assessment, String
    // model) throws Exception {
    // if (text == null || text.isEmpty() || text.isBlank()) {
    // log.info("Message text is empty or null, skipping...");
    // return null;
    // }
    // log.info("--------------------------------");
    // log.info("ADDING MESSAGE TO CHAT HISTORY - RAW TEXT:");
    // log.info("Message text: {}", text.substring(0, Math.min(text.length(), 100))
    // + "...");
    // log.info("Message type: {}", messageType);
    // log.info("Model: {}", model);
    // log.info("Assessment ID: {}", assessment.getId());
    // for (OpenAiToolCall toolCall : toolCalls) {
    // log.info("Tool call: {}", toolCall.getToolName());
    // log.info("Tool call arguments: {}", toolCall.getArguments());
    // log.info("Tool call id: {}", toolCall.getId());
    // log.info("Tool call chat message id: {}", toolCall.getChatMessage().getId());
    // }

    // List<OpenAiToolCall> savedToolCalls = List.of();
    // if (toolCalls != null && !toolCalls.isEmpty()) {
    // savedToolCalls = toolCalls.stream().map(toolCall -> {
    // return openAIToolCallRepository.save(toolCall);
    // }).collect(Collectors.toList());
    // }
    // ChatMessage chatMessage = new ChatMessage(text, savedToolCalls, assessment,
    // messageType, model);
    // // chatMessage.setMetadata(metadata);
    // chatMessage = chatMessageRepository.save(chatMessage);
    // log.info("Message added to DB with id: {}", chatMessage.getId());
    // assessment.addMessage(chatMessage);
    // assessmentService.save(assessment);
    // log.info("ChatMessage added to assessment chat history in DB with id: {}",
    // chatMessage.getId());

    // return new ChatMessageCacheDto(chatMessage);
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatMessageCacheDto addMessageToChatHistory(String text, MessageType
    // messageType, List<OpenAiToolCall> toolCalls, Long assessmentId, String model)
    // throws Exception {
    // log.info("Adding chat message to DB with id: {} - message: {}", assessmentId,
    // text.substring(0, Math.min(text.length(), 100)) + "...");
    // Assessment assessment = assessmentService.findById(assessmentId)
    // .orElseThrow(() -> new Exception("Assessment not found with id: " +
    // assessmentId));
    // ChatMessage chatMessage = new ChatMessage();
    // chatMessage.setText(text);
    // chatMessage.setAssessment(assessment); // TODO: this is not needed, the chat
    // history is already set in the chat message
    // chatMessage.setMessageType(messageType);
    // chatMessage.setModel(model);
    // if (toolCalls != null && !toolCalls.isEmpty()) {
    // List<OpenAiToolCall> savedToolCalls = toolCalls.stream().map(toolCall -> {
    // return openAIToolCallRepository.save(toolCall);
    // }).collect(Collectors.toList());
    // chatMessage.setToolCalls(savedToolCalls);
    // }
    // // chatMessage.setMetadata(metadata);
    // chatMessageRepository.save(chatMessage);
    // log.info("Message added to DB with id: {}", chatMessage.getId());
    // assessment.addMessage(chatMessage);
    // assessmentService.save(assessment);
    // log.info("ChatMessage added to assessment chat history in DB with id: {}",
    // chatMessage.getId());
    // return new ChatMessageCacheDto(chatMessage);
    // }

    /*
     * Get a message by message id
     */
    @Cacheable(value = "chat_messages", key = "'message:' + #id")
    public ChatMessageCacheDto getMessageById(Long id) throws Exception {
        return new ChatMessageCacheDto(chatMessageRepository.findById(id)
                .orElseThrow(() -> new Exception("Chat message not found with id: " + id)));
    }

    // public ChatMessage createMessage(ChatMessage message) throws
    // Exception {
    // return chatMessageRepository.save(message);
    // }

    @Cacheable(value = "chat_messages", key = "'assessment:' + #assessmentId")
    @Transactional(readOnly = true)
    public List<ChatMessageCacheDto> getMessagesByAssessmentId(Long assessmentId) {
        return chatMessageRepository.findByAssessmentIdOrderByCreatedAtAsc(assessmentId).stream()
                .map(ChatMessageCacheDto::new).collect(Collectors.toList());
    }

    // public ChatMessage updateMessage(Long id, ChatMessage message) throws
    // Exception {
    // ChatMessage existingMessage = getMessageById(id);
    // existingMessage.setText(message.getText());
    // existingMessage.setMessageType(message.getMessageType());
    // // existingMessage.setMetadata(message.getMetadata());
    // return chatMessageRepository.save(existingMessage);
    // }

    // @CacheEvict(value = "chat_messages", beforeInvocation = true, key =
    // "message:" + "#id")
    // public void deleteMessage(Long id) throws Exception {
    // // Get the message first to find its assessment ID before deletion
    // ChatMessage message = chatMessageRepository.findById(id)
    // .orElseThrow(() -> new Exception("Chat message not found with id: " + id));

    // Long assessmentId = message.getAssessment().getId();

    // // Delete the message from the database
    // chatMessageRepository.deleteById(id);

    // // Update the assessment cache by removing the deleted message
    // // Get fresh data from the database to ensure we have the most up-to-date
    // list
    // List<ChatMessage> freshMessages =
    // chatMessageRepository.findByAssessmentIdOrderByCreatedAtAsc(assessmentId);
    // List<ChatMessageCacheDto> currentMessages = freshMessages.stream()
    // .map(ChatMessageCacheDto::new)
    // .collect(Collectors.toList());

    // // Update the assessment cache with the fresh list (which no longer contains
    // the deleted message)
    // // The cache key should match the @Cacheable annotation format
    // redisService.set("chat_messages:assessment:" + assessmentId,
    // currentMessages);

    // log.info("Deleted message with id: {} and updated assessment cache for
    // assessment: {}", id, assessmentId);
    // }

    /**
     * SSE Emitter Management Methods
     */

    public void registerSseEmitter(UUID jobId, SseEmitter emitter) {
        sseEmitters.put(jobId, emitter);
        log.info("Registered SSE emitter for job: {}", jobId);
    }

    public void removeSseEmitter(UUID jobId) {
        SseEmitter emitter = sseEmitters.remove(jobId);
        if (emitter != null) {
            log.info("Removed SSE emitter for job: {}", jobId);
        }
    }

    public void sendSseEvent(UUID jobId, String eventName, Object data) {
        SseEmitter emitter = sseEmitters.get(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .id(jobId.toString())
                        .data(data));
                log.info("Sent SSE event '{}' for job: {}", eventName, jobId);
            } catch (IOException e) {
                log.error("Error sending SSE event '{}' for job: {} - {}", eventName, jobId, e.getMessage());
                // Complete the emitter properly on IO error to prevent hanging connections
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("Error completing SSE emitter with error for job: {}", jobId, ex);
                }
                removeSseEmitter(jobId);
            } catch (IllegalStateException e) {
                log.warn("SSE emitter already completed for job: {} - {}", jobId, e.getMessage());
                removeSseEmitter(jobId);
            }
        } else {
            log.warn("No SSE emitter found for job: {}", jobId);
        }
    }

    public void completeSseEmitter(UUID jobId) {
        SseEmitter emitter = sseEmitters.remove(jobId); // Remove and get in one operation
        if (emitter != null) {
            try {
                // Send a final event to indicate completion before closing
                emitter.send(SseEmitter.event()
                        .name("stream_complete")
                        .data(Map.of("message", "Stream completed successfully", "jobId", jobId.toString())));
                emitter.complete();
                log.info("Completed SSE emitter for job: {}", jobId);
            } catch (IOException e) {
                log.error("Error sending final SSE event for job: {} - {}", jobId, e.getMessage());
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("Error completing SSE emitter with error for job: {}", jobId, ex);
                }
            } catch (IllegalStateException e) {
                log.warn("SSE emitter already completed for job: {} - {}", jobId, e.getMessage());
            } catch (Exception e) {
                log.error("Error completing SSE emitter for job: {}", jobId, e);
            } finally {
                removeSseEmitter(jobId);
            }
        }
    }

}