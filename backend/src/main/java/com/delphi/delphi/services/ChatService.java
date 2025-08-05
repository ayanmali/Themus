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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.delphi.delphi.components.GithubTools;
import com.delphi.delphi.entities.Assessment;
import com.delphi.delphi.entities.ChatHistory;
import com.delphi.delphi.entities.ChatMessage;
import com.delphi.delphi.repositories.ChatHistoryRepository;
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
 */
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final ChatModel chatModel;

    private final GithubTools githubTools;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatHistoryRepository chatHistoryRepository,
            ChatMessageRepository chatMessageRepository, ChatModel chatModel, GithubTools githubTools) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatModel = chatModel;
        this.githubTools = githubTools;
        log.info("ChatService initialized with Spring AI ChatModel, targeting OpenRouter.");
    }

    /**
     * Spring AI Methods
     */

    /*
     * Get a chat completion from the AI model
     */
    //@Cacheable(value = "chatCompletions", key = "#chatHistoryId")
    public ChatResponse getChatCompletion(String userMessage, String model, Long assessmentId, Long userId, Long chatHistoryId) {
        log.info("Sending prompt to OpenRouter model '{}':\nUSER MESSAGE: '{}'", model, userMessage);
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

            // Create a system message from a template and substitute the values
            // SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("Tell me a {adjective} joke about {topic}");
            // Message systemMessage = systemPromptTemplate.createMessage(Map.of("adjective", "offensive", "topic", "old people"));

            // add the user message to the chat history
            addMessageToChatHistory(userMessage, MessageType.USER, List.of(), chatHistoryId, model);
            // add the user message to the messages list
            //messages.add(new ChatMessage(userMessage, chatHistory, MessageType.USER, model));

            // get the chat history
            ChatHistory chatHistory = getChatHistoryById(chatHistoryId);

            // get the messages from the chat history
            List<Message> messages = chatHistory.getMessagesAsSpringMessages();

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
                    .toolContext(Map.of("assessmentId", assessmentId, "userId", userId, "chatHistoryId", chatHistoryId, "model", model))
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
                addMessageToChatHistory(generation.getOutput(), chatHistoryId, model);
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
    public ChatResponse getChatCompletion(String userPromptTemplateMessage, Map<String, Object> userPromptVariables, String model, Long assessmentId, Long userId, Long chatHistoryId) {
        log.info("Sending prompt to OpenRouter model '{}':\nUSER MESSAGE: '{}'", model, userPromptTemplateMessage);
        try {
            // The Spring AI ChatModel handles the call to OpenRouter based on application.properties

            // Create a system message from a template and substitute the values
            // SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("Tell me a {adjective} joke about {topic}");
            // Message systemMessage = systemPromptTemplate.createMessage(Map.of("adjective", "offensive", "topic", "old people"));

            // create a user message from the template and substitute the values
            PromptTemplate userPromptTemplate = new PromptTemplate(userPromptTemplateMessage);
            Message userMessage = userPromptTemplate.createMessage(userPromptVariables);

            addMessageToChatHistory(userMessage.getText(), MessageType.USER, List.of(), chatHistoryId, model);
            // add the user message to the messages list
            //messages.add(new ChatMessage(userMessage, chatHistory, MessageType.USER, model));

            // get the chat history
            ChatHistory chatHistory = getChatHistoryById(chatHistoryId);

            // get the messages from the chat history
            List<Message> messages = chatHistory.getMessagesAsSpringMessages();

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
                    .toolContext(Map.of("assessmentId", assessmentId, "userId", userId, "chatHistoryId", chatHistoryId, "model", model))
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
                addMessageToChatHistory(generation.getOutput(), chatHistoryId, model);
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

    @CachePut(value = "chatHistories", key = "#chatHistory.id")
    public ChatHistory createChatHistory(ChatHistory chatHistory, String systemMessage) throws Exception {
        // adding system prompt to chat history
        ChatHistory savedChatHistory = chatHistoryRepository.save(chatHistory);
        addMessageToChatHistory(systemMessage, MessageType.SYSTEM, List.of(), savedChatHistory.getId(), "N/A");
        // save chat history
        return savedChatHistory;
    }

    @Cacheable(value = "chatHistories", key = "#id")
    public ChatHistory getChatHistoryById(Long id) throws Exception {
        try {
            return chatHistoryRepository.findById(id)
                    .orElseThrow(() -> new Exception("Chat history not found with id: " + id));
        } catch (Exception e) {
            throw new Exception("Chat history not found with id: " + id);
        }
    }

    @Cacheable(value = "chatHistories", key = "#assessmentId")
    public ChatHistory getChatHistoryByAssessmentId(Long assessmentId) throws Exception {
        try {
            return chatHistoryRepository.findByAssessmentId(assessmentId);
        } catch (Exception e) {
            throw new Exception("Chat history not found with assessment id: " + assessmentId);
        }
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatHistory updateChatHistory(Long id, ChatHistory chatHistory) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(id);
        existingChatHistory.setAssessment(chatHistory.getAssessment());
        return chatHistoryRepository.save(existingChatHistory);
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatHistory updateChatHistory(Long id, Assessment assessment) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(id);
        existingChatHistory.setAssessment(assessment);
        return chatHistoryRepository.save(existingChatHistory);
    }

    @CacheEvict(value = "chatHistories", key = "#id")
    public void deleteChatHistory(Long id) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(id);
        chatHistoryRepository.delete(existingChatHistory);
    }

    @Cacheable(value = "chatHistories")
    public List<ChatHistory> getAllChatHistories() {
        return chatHistoryRepository.findAll();
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatMessage addMessageToChatHistory(ChatMessage message) throws Exception {
        ChatHistory existingChatHistory = getChatHistoryById(message.getChatHistory().getId());
        existingChatHistory.addMessage(message);
        // existingChatHistory.getMessages().add(message);
        chatHistoryRepository.save(existingChatHistory);
        return message;
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatMessage addMessageToChatHistory(AssistantMessage message, Long chatHistoryId, String model) throws Exception {
        // TODO: integrate message.getToolCalls() and store tool calls in message entities
        ChatHistory existingChatHistory = getChatHistoryById(chatHistoryId);
        ChatMessage chatMessage = new ChatMessage(message, existingChatHistory, model);

        existingChatHistory.addMessage(chatMessage);
        // existingChatHistory.getMessages().add(new ChatMessage(message, existingChatHistory, model));
        chatHistoryRepository.save(existingChatHistory);
        return chatMessage;
    }

    @CachePut(value = "chatHistories", key = "#result.id")
    public ChatMessage addMessageToChatHistory(String text, MessageType messageType, List<OpenAiToolCall> toolCalls, Long chatHistoryId, String model) throws Exception {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setText(text);
        chatMessage.setChatHistory(getChatHistoryById(chatHistoryId));
        chatMessage.setMessageType(messageType);
        chatMessage.setModel(model);
        if (!toolCalls.isEmpty()) {
            chatMessage.setToolCalls(toolCalls);
        }
        // chatMessage.setMetadata(metadata);
        chatMessageRepository.save(chatMessage);

        ChatHistory existingChatHistory = getChatHistoryById(chatHistoryId);
        existingChatHistory.getMessages().add(chatMessage);
        chatHistoryRepository.save(existingChatHistory);
        return chatMessage;
    }

    /*
     * Get a message by message id
     */
    @Cacheable(value = "chatMessages", key = "#id")
    public ChatMessage getMessageById(Long id) throws Exception {
        return chatMessageRepository.findById(id)
                .orElseThrow(() -> new Exception("Chat message not found with id: " + id));
    }

    // public ChatMessage createMessage(ChatMessage message) throws
    // Exception {
    // return chatMessageRepository.save(message);
    // }

    @Cacheable(value = "chatMessages", key = "#chatHistoryId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ChatMessage> getMessagesByChatId(Long chatHistoryId, Pageable pageable) {
        return chatMessageRepository.findByChatHistoryId(chatHistoryId, pageable);
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
        ChatMessage existingMessage = getMessageById(id);
        chatMessageRepository.delete(existingMessage);
    }

}