# Payment RabbitMQ Integration Guide

This document outlines how to use the new RabbitMQ-based payment messaging system for asynchronous Stripe payment processing.

## Overview

The payment messaging system provides asynchronous processing for:
- Customer creation
- Checkout session creation  
- Stripe webhook processing
- Subscription data synchronization

## Architecture

### Message Flow
```
Client Request → PaymentController → PaymentMessagePublisher → RabbitMQ Queue 
                                                                      ↓
PaymentResponseSubscriber ← RabbitMQ Response Queue ← PaymentMessageSubscriber
```

### Exchanges and Queues

1. **Payment Topic Exchange** (`paymentTopicExchange`)
   - Queue: `paymentTopicQueue`
   - Routing: `topic.payment.#`

2. **Payment Response Exchange** (`paymentResponseTopicExchange`)
   - Queue: `paymentResponseTopicQueue`  
   - Routing: `topic.payment.response.#`

3. **Payment Webhook Exchange** (`paymentWebhookTopicExchange`)
   - Queue: `paymentWebhookTopicQueue`
   - Routing: `topic.payment.webhook.#`

## API Endpoints

### Async Endpoints

All payment endpoints now return immediately with a `requestId` for tracking:

```json
{
  "message": "Operation initiated",
  "requestId": "uuid-request-id",
  "status": "processing"
}
```

#### 1. Initiate Checkout
```http
GET /api/payments/initiate-checkout
```

**Response:**
```json
{
  "message": "Checkout session creation initiated",
  "requestId": "abc-123-def",
  "status": "processing"
}
```

#### 2. Stripe Webhook (Async)
```http
POST /api/payments/stripe/webhook
Headers: Stripe-Signature: your-signature
Body: webhook-body
```

**Response:**
```json
{
  "message": "Webhook received and queued for processing",
  "requestId": "xyz-789-ghi", 
  "status": "processing"
}
```

#### 3. Checkout Success
```http
GET /api/payments/checkout/success
```

#### 4. Get Subscription
```http
GET /api/payments/subscription
```

Returns cached data immediately, or initiates async fetch if not available.

#### 5. Check Operation Status
```http
GET /api/payments/status/{requestId}
```

## Message Types

### PaymentRequestDto
```java
public enum PaymentOperation {
    CREATE_CUSTOMER,
    CREATE_CHECKOUT_SESSION,
    SYNC_SUBSCRIPTION_DATA,
    GET_SUBSCRIPTION
}
```

### PaymentResponseDto
Contains success/error status and relevant data:
- `Customer` for customer creation
- `Session` for checkout sessions
- `StripeSubCache` for subscription data

### StripeWebhookDto
Contains webhook signature and body for async processing.

## Usage Examples

### 1. Publishing Payment Requests

```java
@Autowired
private PaymentMessagePublisher paymentMessagePublisher;

// Create customer
String requestId = paymentMessagePublisher.publishCreateCustomerRequest(userId);

// Create checkout session  
String requestId = paymentMessagePublisher.publishCreateCheckoutSessionRequest(customerId);

// Sync subscription data
String requestId = paymentMessagePublisher.publishSyncSubscriptionDataRequest(customerId);

// Process webhook
String requestId = paymentMessagePublisher.publishStripeWebhook(signature, body);
```

### 2. Handling Responses

The `PaymentResponseSubscriber` automatically processes responses. For client updates, implement WebSocket or polling:

```java
@Component
@RabbitListener(queues = TopicConfig.PAYMENT_RESPONSE_TOPIC_QUEUE_NAME)
public class PaymentResponseSubscriber {
    
    @RabbitHandler
    public void processPaymentResponse(PaymentResponseDto response) {
        if (response.isSuccess()) {
            // Send WebSocket update to client
            webSocketHandler.sendUpdate(response.getRequestId(), response);
        } else {
            // Send error notification
            webSocketHandler.sendError(response.getRequestId(), response.getError());
        }
    }
}
```

## Integration Steps

### 1. Update Frontend
Change payment API calls to handle async responses:

```javascript
// Old synchronous approach
const response = await fetch('/api/payments/initiate-checkout');
const checkoutUrl = await response.text();
window.location.href = checkoutUrl;

// New async approach
const response = await fetch('/api/payments/initiate-checkout');
const {requestId} = await response.json();

// Subscribe to WebSocket for updates or poll status endpoint
websocket.on('payment-update', (data) => {
  if (data.requestId === requestId && data.checkoutSession) {
    window.location.href = data.checkoutSession.url;
  }
});
```

### 2. WebSocket Integration (Recommended)
Implement WebSocket handlers for real-time updates:

```java
@Component
public class PaymentWebSocketHandler {
    
    public void sendPaymentUpdate(String requestId, String type, Object data) {
        // Send WebSocket message to client
        Map<String, Object> message = Map.of(
            "requestId", requestId,
            "type", type,
            "data", data
        );
        webSocketTemplate.convertAndSend("/topic/payments/" + requestId, message);
    }
}
```

### 3. Error Handling
Implement retry logic and dead letter queues for failed operations:

```java
@Component
public class PaymentErrorHandler {
    
    @RabbitListener(queues = "payment.dlq")
    public void handleFailedPayments(PaymentRequestDto failedRequest) {
        // Log error, alert monitoring, etc.
        log.error("Payment operation failed after retries: {}", failedRequest);
    }
}
```

## Benefits

1. **Improved Performance**: Non-blocking payment operations
2. **Better User Experience**: Immediate response to user actions
3. **Reliability**: Message queues ensure operations complete even if services restart
4. **Scalability**: Can scale payment processing independently
5. **Observability**: Clear audit trail of payment operations
6. **Fault Tolerance**: Failed operations can be retried automatically

## Testing

Use the existing RabbitMQ test patterns:

```java
@Test
void testCreateCustomerFlow() {
    // Arrange
    Long userId = 1L;
    
    // Act
    String requestId = paymentMessagePublisher.publishCreateCustomerRequest(userId);
    
    // Assert
    await().atMost(Duration.ofSeconds(5))
        .until(() -> {
            verify(stripeService).createCustomer(any(User.class));
            verify(paymentMessagePublisher).publishPaymentResponse(eq(requestId), any(Customer.class));
            return true;
        });
}
```

## Monitoring

Monitor queue sizes, processing times, and error rates:
- Payment queue depth
- Processing latency
- Error rates by operation type
- Webhook processing success rate

## Migration Notes

- Existing synchronous endpoints remain functional
- Gradually migrate clients to async endpoints
- Monitor both sync and async usage during transition
- Consider feature flags for rollback capability 