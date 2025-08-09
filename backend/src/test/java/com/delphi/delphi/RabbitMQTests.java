package com.delphi.delphi;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.test.RabbitListenerTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.delphi.delphi.components.messaging.chat.ChatMessagePublisher;
import com.delphi.delphi.components.messaging.chat.ChatMessageSubscriber;
import com.delphi.delphi.components.messaging.chat.ChatResponseSubscriber;
import com.delphi.delphi.configs.rabbitmq.TopicConfig;
import com.delphi.delphi.dtos.messaging.chat.ChatCompletionRequestDto;
import com.delphi.delphi.dtos.messaging.chat.ChatCompletionResponseDto;
import com.delphi.delphi.services.ChatService;

@SpringBootTest
@Testcontainers
@RabbitListenerTest(capture = true)
@TestPropertySource(properties = {
    "spring.rabbitmq.host=localhost",
    "logging.level.com.delphi.delphi.components.messaging=DEBUG"
})
public class RabbitMQTests {

    @Container
    @SuppressWarnings("resource")
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.11-management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQContainer::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @MockitoSpyBean
    private ChatMessagePublisher chatMessagePublisher;

    @MockitoSpyBean
    private ChatMessageSubscriber chatMessageSubscriber;

    @MockitoSpyBean
    private ChatResponseSubscriber chatResponseSubscriber;

    @MockitoSpyBean
    private ChatService chatService;

    @MockitoSpyBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        // Test setup if needed
    }

    @Test
    void testChatCompletionRequestFlow() throws Exception {
        // Arrange
        String userMessage = "Hello, AI!";
        String model = "gpt-4o-mini";
        Long assessmentId = 1L;
        Long userId = 1L;

        ChatResponse mockResponse = createMockChatResponse("Hello! How can I help you?");
        when(chatService.getChatCompletion(anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(mockResponse);

        CountDownLatch latch = new CountDownLatch(1);
        
        // Mock the response subscriber to count down when message is received
        ChatResponseSubscriber spySubscriber = new ChatResponseSubscriber() {
            @Override
            public void processChatCompletionResponse(ChatCompletionResponseDto response) {
                super.processChatCompletionResponse(response);
                latch.countDown();
            }
        };

        // Act
        String requestId = chatMessagePublisher.publishChatCompletionRequest(
                userMessage, model, assessmentId, userId);

        // Assert
        assertThat(requestId).isNotNull();
        assertThat(UUID.fromString(requestId)).isNotNull(); // Valid UUID

        // Wait for async processing
        await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    try {
                        verify(chatService, times(1)).getChatCompletion(
                                eq(userMessage), eq(model), eq(assessmentId), eq(userId));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });

        // Verify response was published
        await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    try {
                        verify(chatMessagePublisher, times(1)).publishChatCompletionResponse(
                                eq(requestId), any(ChatResponse.class));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @Test
    void testTemplateBasedChatCompletionRequest() throws Exception {
        // Arrange
        String template = "Analyze this code for {language}: {code}";
        Map<String, Object> variables = Map.of(
                "language", "Java",
                "code", "public class Test {}"
        );
        String model = "gpt-4o-mini";
        Long assessmentId = 1L;
        Long userId = 1L;

        ChatResponse mockResponse = createMockChatResponse("This is a simple Java class.");
        when(chatService.getChatCompletion(anyString(), any(Map.class), anyString(), anyLong(), anyLong()))
                .thenReturn(mockResponse);

        // Act
        String requestId = chatMessagePublisher.publishChatCompletionRequest(
                template, variables, model, assessmentId, userId);

        // Assert
        assertThat(requestId).isNotNull();

        // Wait for async processing
        await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    try {
                        verify(chatService, times(1)).getChatCompletion(
                                eq(template), eq(variables), eq(model), eq(assessmentId), eq(userId));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @Test
    void testErrorHandlingInChatCompletion() throws Exception {
        // Arrange
        String userMessage = "This will fail";
        String model = "gpt-4o-mini";
        Long assessmentId = 1L;
        Long userId = 1L;       

        when(chatService.getChatCompletion(anyString(), anyString(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("AI service unavailable"));

        // Act
        String requestId = chatMessagePublisher.publishChatCompletionRequest(
                userMessage, model, assessmentId, userId);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    try {
                        verify(chatMessagePublisher, times(1)).publishChatCompletionError(
                                eq(requestId), anyString());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @Test
    void testInvalidRequestHandling() throws Exception {
        // Arrange - Request with neither userMessage nor userPromptTemplate
        ChatCompletionRequestDto invalidRequest = new ChatCompletionRequestDto();
        invalidRequest.setRequestId(UUID.randomUUID().toString());
        invalidRequest.setModel("gpt-4o-mini");
        invalidRequest.setAssessmentId(1L);
        invalidRequest.setUserId(1L);
        // Both userMessage and userPromptTemplate are null

        // Act
        chatMessageSubscriber.processChatCompletionRequest(invalidRequest);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .until(() -> {
                    try {
                        verify(chatMessagePublisher, times(1)).publishChatCompletionError(
                                eq(invalidRequest.getRequestId()), 
                                eq("Either userMessage or userPromptTemplate must be provided"));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @Test
    void testMessageSerialization() {
        // Test ChatCompletionRequestDto serialization
        ChatCompletionRequestDto request = new ChatCompletionRequestDto(
                "Hello", "gpt-4o-mini", 1L, 1L, UUID.randomUUID().toString());

        assertThat(request.getUserMessage()).isEqualTo("Hello");
        assertThat(request.getModel()).isEqualTo("gpt-4o-mini");
        assertThat(request.getAssessmentId()).isEqualTo(1L);
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getRequestId()).isNotNull();

        // Test ChatCompletionResponseDto serialization
        ChatResponse mockResponse = createMockChatResponse("Test response");
        ChatCompletionResponseDto response = new ChatCompletionResponseDto(
                request.getRequestId(), mockResponse);

        assertThat(response.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(response.getChatResponse()).isEqualTo(mockResponse);
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getError()).isNull();

        // Test error response
        ChatCompletionResponseDto errorResponse = new ChatCompletionResponseDto(
                request.getRequestId(), "Test error");

        assertThat(errorResponse.getRequestId()).isEqualTo(request.getRequestId());
        assertThat(errorResponse.getChatResponse()).isNull();
        assertThat(errorResponse.isSuccess()).isFalse();
        assertThat(errorResponse.getError()).isEqualTo("Test error");
    }

    @Test
    void testConcurrentRequests() throws Exception {
        // Arrange
        int numberOfRequests = 5;
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        
        ChatResponse mockResponse = createMockChatResponse("Concurrent response");
        when(chatService.getChatCompletion(anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(mockResponse);

        // Act - Send multiple concurrent requests
        for (int i = 0; i < numberOfRequests; i++) {
            chatMessagePublisher.publishChatCompletionRequest(
                    "Message " + i, "gpt-4o-mini", 1L, 1L);
        }

        // Assert - All requests should be processed
        await().atMost(Duration.ofSeconds(10))
                .until(() -> {
                    try {
                        verify(chatService, times(numberOfRequests)).getChatCompletion(
                                            anyString(), anyString(), anyLong(), anyLong());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    @Test
    void testQueueConfiguration() {
        // Verify that queues are properly configured
        assertThat(TopicConfig.CHAT_TOPIC_EXCHANGE_NAME).isEqualTo("chatTopicExchange");
        assertThat(TopicConfig.CHAT_TOPIC_QUEUE_NAME).isEqualTo("chatTopicQueue");
        assertThat(TopicConfig.CHAT_RESPONSE_TOPIC_EXCHANGE_NAME).isEqualTo("chatResponseTopicExchange");
        assertThat(TopicConfig.CHAT_RESPONSE_TOPIC_QUEUE_NAME).isEqualTo("chatResponseTopicQueue");
    }

    @Test
    void testResponseSubscriberLogging() throws Exception {
        // Arrange
        String requestId = UUID.randomUUID().toString();
        ChatResponse mockResponse = createMockChatResponse("Test response");
        ChatCompletionResponseDto responseDto = new ChatCompletionResponseDto(requestId, mockResponse);

        // Act
        chatResponseSubscriber.processChatCompletionResponse(responseDto);

        // Assert - This mainly tests that no exceptions are thrown
        // In a real scenario, you might want to verify WebSocket sending or other side effects
        assertThat(responseDto.isSuccess()).isTrue();
    }

    @Test
    void testResponseSubscriberErrorHandling() throws Exception {
        // Arrange
        String requestId = UUID.randomUUID().toString();
        ChatCompletionResponseDto errorResponseDto = new ChatCompletionResponseDto(requestId, "Test error");

        // Act
        chatResponseSubscriber.processChatCompletionResponse(errorResponseDto);

        // Assert - This mainly tests that no exceptions are thrown during error handling
        assertThat(errorResponseDto.isSuccess()).isFalse();
        assertThat(errorResponseDto.getError()).isEqualTo("Test error");
    }

    // Helper method to create mock ChatResponse
    private ChatResponse createMockChatResponse(String text) {
        AssistantMessage assistantMessage = new AssistantMessage(text);
        Generation generation = new Generation(assistantMessage);
        return new ChatResponse(List.of(generation));
    }
}
