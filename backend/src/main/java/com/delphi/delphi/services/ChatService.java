package com.delphi.delphi.services;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.components.GithubTools;
import com.delphi.delphi.dtos.cache.ChatMessageCacheDto;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.repositories.AssessmentRepository;
import com.delphi.delphi.repositories.ChatMessageRepository;
import com.delphi.delphi.utils.OpenAiToolCall;

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
 */
public class ChatService {

    private final AssessmentRepository assessmentRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final ChatModel chatModel;

    private final GithubTools githubTools;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatMessageRepository chatMessageRepository, ChatModel chatModel, GithubTools githubTools, AssessmentRepository assessmentRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatModel;
        this.githubTools = githubTools;
        log.info("ChatService initialized with Spring AI ChatModel, targeting OpenRouter.");
        this.assessmentRepository = assessmentRepository;
    }

    /**
     * Spring AI Methods
     */

    /*
     * Get a chat completion from the AI model
     */
    //@Cacheable(value = "chatCompletions", key = "#chatHistoryId")
    public ChatResponse getChatCompletion(String userMessage, String model, Long assessmentId, Long userId) {
        log.info("Sending prompt to OpenRouter model '{}':\nUSER MESSAGE: '{}'", model, userMessage);
        try {
            Assessment assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(() -> new Exception("Assessment not found with id: " + assessmentId));
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

            // Create a system message from a template and substitute the values
            // SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("Tell me a {adjective} joke about {topic}");
            // Message systemMessage = systemPromptTemplate.createMessage(Map.of("adjective", "offensive", "topic", "old people"));

            // add the user message to the chat history
            addMessageToChatHistory(userMessage, MessageType.USER, List.of(), assessment, model);
            // add the user message to the messages list
            //messages.add(new ChatMessage(userMessage, chatHistory, MessageType.USER, model));

            // get the chat history

            // get the messages from the chat history
            List<Message> messages = assessment.getMessagesAsSpringMessages();

            // create a prompt message from template
            // PromptTemplate pt = new PromptTemplate("You are a helpful assistant. You are given a conversation history and a user message. You need to respond to the user message based on the conversation history. The conversation history is: {conversationHistory}. The user message is: {userMessage}. The response should be in the same language as the conversation history.");
            // Message systemMessage = pt.createMessage(Map.of("conversationHistory", messages, "userMessage", userMessage));

            // creating a prompt with a system message and a user message
            Prompt prompt = new Prompt(
                // convert the messages to Spring AI messages
                messages,
                OpenAiChatOptions.builder()
                    .model(model)
                    .toolCallbacks(ToolCallbacks.from(githubTools))
                    .toolContext(Map.of("assessmentId", assessmentId, "userId", userId, "model", model))
                    //.internalToolExecutionEnabled(false) // disable framework-enabled tool execution
                    .temperature(0.75) // double between 0 and 1
                    .build()
            );

            // Create a new prompt with a user message
            // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
            //         .model(model)
            //         .build());

            // adding LLM response to chat history
            ChatResponse response = chatModel.call(prompt);
            log.info("Response: {}", response.getResults().stream().map(r -> r.getOutput().getText()).collect(Collectors.joining("\n")));
            for (Generation generation : response.getResults()) {
                addMessageToChatHistory(generation.getOutput(), assessment, model);
            }
            return response;
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    /*
     * Get a chat completion from the AI model using a user prompt template
     */
    //@Cacheable(value = "chatCompletions", key = "#chatHistoryId")
    public ChatResponse getChatCompletion(String userPromptTemplateMessage, Map<String, Object> userPromptVariables, String model, Long assessmentId, Long userId) {
        log.info("Sending prompt to OpenRouter model '{}':\nUSER MESSAGE: '{}'", model, userPromptTemplateMessage);
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

            // Create a system message from a template and substitute the values
            // SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("Tell me a {adjective} joke about {topic}");
            // Message systemMessage = systemPromptTemplate.createMessage(Map.of("adjective", "offensive", "topic", "old people"));

            // create a user message from the template and substitute the values
            Assessment assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(() -> new Exception("Assessment not found with id: " + assessmentId));

            PromptTemplate userPromptTemplate = new PromptTemplate(userPromptTemplateMessage);
            Message userMessage = userPromptTemplate.createMessage(userPromptVariables);

            addMessageToChatHistory(userMessage.getText(), MessageType.USER, List.of(), assessment, model);
            // add the user message to the messages list
            //messages.add(new ChatMessage(userMessage, chatHistory, MessageType.USER, model));

            // get the messages from the chat history
            List<Message> messages = assessment.getMessagesAsSpringMessages();

            // create a prompt message from template
            // PromptTemplate pt = new PromptTemplate("You are a helpful assistant. You are given a conversation history and a user message. You need to respond to the user message based on the conversation history. The conversation history is: {conversationHistory}. The user message is: {userMessage}. The response should be in the same language as the conversation history.");
            // Message systemMessage = pt.createMessage(Map.of("conversationHistory", messages, "userMessage", userMessage));

            // creating a prompt with a system message and a user message
            Prompt prompt = new Prompt(
                // convert the messages to Spring AI messages
                messages,
                OpenAiChatOptions.builder()
                    .model(model)
                    .toolCallbacks(ToolCallbacks.from(githubTools))
                    .toolContext(Map.of("assessmentId", assessmentId, "userId", userId, "model", model))
                    //.internalToolExecutionEnabled(false) // disable framework-enabled tool execution
                    .temperature(0.75) // double between 0 and 1
                    .build()
            );

            // Create a new prompt with a user message
            // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
            //         .model(model)
            //         .build());

            // adding LLM response to chat history
            ChatResponse response = chatModel.call(prompt);
            log.info("Response: {}", response.getResults().stream().map(r -> r.getOutput().getText()).collect(Collectors.joining("\n")));
            for (Generation generation : response.getResults()) {
                addMessageToChatHistory(generation.getOutput(), assessment, model);
            }
            return response;
        } catch (Exception e) {
            log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
        }
    }

    // /*
    //  * Get completion w/ a template system prompt string)
    //  * Surround variables with curly braces in the system prompt template ("{ }")
    //  */
    // public ChatResponse getChatCompletion(String systemMessageTemplate, Map<String, Object> systemMessageVariables, String userMessage, String model) {
    //     log.info("Sending prompt to OpenRouter model '{}':\nSYSTEM PROMPT: '{}'\nUSER MESSAGE: '{}'",
    //             model,
    //             systemMessageTemplate.substring(0, Math.min(systemMessageTemplate.length(), 100)) + "...",
    //             userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
    //     try {
    //         // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

    //         // call a chat model with a string user message
    //         // ChatResponse response = chatModel.call(
    //         //         new Prompt(
    //         //                 userMessage,
    //         //                 OpenAiChatOptions.builder()
    //         //                         .model(model)
    //         //                         .build()));

    //         // Create a system message from a template and substitute the values
    //         SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemMessageTemplate);
    //         Message systemMessage = systemPromptTemplate.createMessage(systemMessageVariables);

    //         // creating a prompt with a system message and a user message
    //         Prompt prompt = new Prompt(
    //             List.of(
    //                 systemMessage,
    //                 new UserMessage(userMessage)),
    //             OpenAiChatOptions.builder()
    //                 .model(model)
    //                 .temperature(0.75) // double between 0 and 1
    //                 .build()
    //         );

    //         // Create a new prompt with a user message
    //         // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
    //         //         .model(model)
    //         //         .build());
            
    //         return chatModel.call(prompt);
    //     } catch (Exception e) {
    //         log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
    //         throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
    //     }
    // }
    
    // /*
    //  * Pass in a system prompt message (no template) and a user message
    //  */
    // public ChatResponse getChatCompletion(String systemPromptMessage, String userMessage, String model) {
    //     log.info("Sending prompt to OpenRouter model '{}':\nSYSTEM PROMPT: '{}'\nUSER MESSAGE: '{}'",
    //             model,
    //             systemPromptMessage.substring(0, Math.min(systemPromptMessage.length(), 100)) + "...",
    //             userMessage.substring(0, Math.min(userMessage.length(), 100)) + "...");
    //     try {
    //         // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

    //         // call a chat model with a string user message
    //         // ChatResponse response = chatModel.call(
    //         //         new Prompt(
    //         //                 userMessage,
    //         //                 OpenAiChatOptions.builder()
    //         //                         .model(model)
    //         //                         .build()));

    //         // Create a system message from a template and substitute the values

    //         // creating a prompt with a system message and a user message
    //         Prompt prompt = new Prompt(
    //             List.of(
    //                 new SystemMessage(systemPromptMessage),
    //                 new UserMessage(userMessage)),
    //             OpenAiChatOptions.builder()
    //                 .model(model)
    //                 .temperature(0.75) // double between 0 and 1
    //                 .build()
    //         );

    //         // Create a new prompt with a user message
    //         // Prompt p2 = new Prompt(new UserMessage(""), OpenAiChatOptions.builder()
    //         //         .model(model)
    //         //         .build());
            
    //         return chatModel.call(prompt);
    //     } catch (Exception e) {
    //         log.error("Error calling OpenRouter via Spring AI: {}", e.getMessage(), e);
    //         throw new RuntimeException("Failed to get completion from AI service: " + e.getMessage(), e);
    //     }
    // }

    /*
     * Database Repository Methods
     */

    // @CachePut(value = "chatHistories", key = "#chatHistory.id")
    // public ChatHistory createChatHistory(ChatHistory chatHistory, String systemMessage) throws Exception {
    //     // adding system prompt to chat history
    //     log.info("Creating chat history with system message: {}", systemMessage.substring(0, Math.min(systemMessage.length(), 100)) + "...");
    //     ChatHistory savedChatHistory = chatHistoryRepository.save(chatHistory);
    //     log.info("Chat history saved in DB with id: {} - adding system message to chat history...", savedChatHistory.getId());
    //     addMessageToChatHistory(systemMessage, MessageType.SYSTEM, List.of(), savedChatHistory, "N/A");
    //     log.info("System message added to chat history in DB with id: {}", savedChatHistory.getId());
    //     // save chat history
    //     return savedChatHistory;
    // }

    // @Cacheable(value = "chatHistories", key = "#id")
    // public ChatHistory getChatHistoryById(Long id) throws Exception {
    //     try {
    //         return chatHistoryRepository.findById(id)
    //                 .orElseThrow(() -> new Exception("Chat history not found with id: " + id));
    //     } catch (Exception e) {
    //         throw new Exception("Chat history not found with id: " + id);
    //     }
    // }

    // @Cacheable(value = "chatHistories", key = "#assessmentId")
    // public ChatHistory getChatHistoryByAssessmentId(Long assessmentId) throws Exception {
    //     try {
    //         return chatHistoryRepository.findByAssessmentId(assessmentId);
    //     } catch (Exception e) {
    //         throw new Exception("Chat history not found with assessment id: " + assessmentId);
    //     }
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatHistory updateChatHistory(Long id, ChatHistory chatHistory) throws Exception {
    //     ChatHistory existingChatHistory = getChatHistoryById(id);
    //     existingChatHistory.setAssessment(chatHistory.getAssessment());
    //     return chatHistoryRepository.save(existingChatHistory);
    // }

    // @CachePut(value = "chatHistories", key = "#result.id")
    // public ChatHistory updateChatHistory(Long id, Assessment assessment) throws Exception {
    //     ChatHistory existingChatHistory = getChatHistoryById(id);
    //     existingChatHistory.setAssessment(assessment);
    //     return chatHistoryRepository.save(existingChatHistory);
    // }

    // @CacheEvict(value = "chatHistories", key = "#id")
    // public void deleteChatHistory(Long id) throws Exception {
    //     ChatHistory existingChatHistory = getChatHistoryById(id);
    //     chatHistoryRepository.delete(existingChatHistory);
    // }

    // @Cacheable(value = "chatHistories")
    // public List<ChatHistory> getAllChatHistories() {
    //     return chatHistoryRepository.findAll();
    // }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatMessageCacheDto addMessageToChatHistory(ChatMessage message) throws Exception {
        Assessment assessment = assessmentRepository.findById(message.getAssessment().getId())
                .orElseThrow(() -> new Exception("Assessment not found with id: " + message.getAssessment().getId()));
        assessment.addMessage(message);
        assessmentRepository.save(assessment);
        return new ChatMessageCacheDto(message);
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatMessageCacheDto addMessageToChatHistory(AssistantMessage message, Assessment assessment, String model) throws Exception {
        // TODO: integrate message.getToolCalls() and store tool calls in message entities
        ChatMessage chatMessage = new ChatMessage(message, assessment, model);

        assessment.addMessage(chatMessage);
        // existingChatHistory.getMessages().add(new ChatMessage(message, existingChatHistory, model));
        assessmentRepository.save(assessment);
        return new ChatMessageCacheDto(chatMessage);
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatMessageCacheDto addMessageToChatHistory(String text, MessageType messageType, List<OpenAiToolCall> toolCalls, Assessment assessment, String model) throws Exception {
        log.info("Adding chat message to DB with id: {} - message: {}", assessment.getId(), text.substring(0, Math.min(text.length(), 100)) + "...");
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setText(text);
        chatMessage.setAssessment(assessment);
        chatMessage.setMessageType(messageType);
        chatMessage.setModel(model);
        if (!toolCalls.isEmpty()) {
            chatMessage.setToolCalls(toolCalls);
        }
        // chatMessage.setMetadata(metadata);
        chatMessageRepository.save(chatMessage);
        log.info("Message added to DB with id: {}", chatMessage.getId());
        assessment.addMessage(chatMessage);
        assessmentRepository.save(assessment);
        log.info("ChatMessage added to assessment chat history in DB with id: {}", chatMessage.getId());
        return new ChatMessageCacheDto(chatMessage);
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatMessageCacheDto addMessageToChatHistory(String text, MessageType messageType, List<OpenAiToolCall> toolCalls, Long assessmentId, String model) throws Exception {
        log.info("Adding chat message to DB with id: {} - message: {}", assessmentId, text.substring(0, Math.min(text.length(), 100)) + "...");
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new Exception("Assessment not found with id: " + assessmentId));
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setText(text);
        chatMessage.setAssessment(assessment); // TODO: this is not needed, the chat history is already set in the chat message
        chatMessage.setMessageType(messageType);
        chatMessage.setModel(model);
        if (!toolCalls.isEmpty()) {
            chatMessage.setToolCalls(toolCalls);
        }
        // chatMessage.setMetadata(metadata);
        chatMessageRepository.save(chatMessage);
        log.info("Message added to DB with id: {}", chatMessage.getId());
        assessment.addMessage(chatMessage);
        assessmentRepository.save(assessment);
        log.info("ChatMessage added to assessment chat history in DB with id: {}", chatMessage.getId());
        return new ChatMessageCacheDto(chatMessage);
    }

    /*
     * Get a message by message id
     */
    @Cacheable(value = "chatMessages", key = "#id")
    public ChatMessageCacheDto getMessageById(Long id) throws Exception {
        return new ChatMessageCacheDto(chatMessageRepository.findById(id)
                .orElseThrow(() -> new Exception("Chat message not found with id: " + id)));
    }

    // public ChatMessage createMessage(ChatMessage message) throws
    // Exception {
    // return chatMessageRepository.save(message);
    // }

    @Cacheable(value = "chatMessages", key = "#assessmentId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public List<ChatMessageCacheDto> getMessagesByAssessmentId(Long assessmentId, Pageable pageable) {
        return chatMessageRepository.findByAssessmentId(assessmentId, pageable).getContent().stream().map(ChatMessageCacheDto::new).collect(Collectors.toList());
    }

    // public ChatMessage updateMessage(Long id, ChatMessage message) throws Exception {
    //     ChatMessage existingMessage = getMessageById(id);
    //     existingMessage.setText(message.getText());
    //     existingMessage.setMessageType(message.getMessageType());
    //     // existingMessage.setMetadata(message.getMetadata());
    //     return chatMessageRepository.save(existingMessage);
    // }

    @CacheEvict(value = "chatMessages", key = "#id")
    public void deleteMessage(Long id) throws Exception {
        ChatMessage existingMessage = chatMessageRepository.findById(id)
                .orElseThrow(() -> new Exception("Chat message not found with id: " + id));
        chatMessageRepository.delete(existingMessage);
    }

}