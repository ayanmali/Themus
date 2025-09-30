package com.delphi.delphi.configs.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
public class KafkaConfig {

    private final String KAFKA_BOOTSTRAP_SERVERS;
    private final String KAFKA_CONSUMER_GROUP;
    private final String KAFKA_SECURITY_PROTOCOL;
    private final String KAFKA_SASL_MECHANISM;
    private final String KAFKA_SASL_JAAS_CONFIG;
    private final String KAFKA_CLIENT_DNS_LOOKUP;
    private final String KAFKA_SESSION_TIMEOUT_MS;
    private final String KAFKA_ACKS;
    private final String KAFKA_CLIENT_ID;

    public KafkaConfig(
        @Value("${spring.kafka.bootstrap-servers}") String kafkaBootstrapServers,
        @Value("${spring.kafka.consumer.group-id}") String kafkaGroupId,
        @Value("${spring.kafka.security.protocol:SASL_SSL}") String securityProtocol,
        @Value("${spring.kafka.sasl.mechanism:PLAIN}") String saslMechanism,
        @Value("${spring.kafka.sasl.jaas.config}") String saslJaasConfig,
        @Value("${spring.kafka.client.dns.lookup:use_all_dns_ips}") String clientDnsLookup,
        @Value("${spring.kafka.session.timeout.ms:45000}") String sessionTimeoutMs,
        @Value("${spring.kafka.acks:all}") String acks,
        @Value("${spring.kafka.client.id}") String clientId
    ) {
        this.KAFKA_BOOTSTRAP_SERVERS = kafkaBootstrapServers;
        this.KAFKA_CONSUMER_GROUP = kafkaGroupId;
        this.KAFKA_SECURITY_PROTOCOL = securityProtocol;
        this.KAFKA_SASL_MECHANISM = saslMechanism;
        this.KAFKA_SASL_JAAS_CONFIG = saslJaasConfig;
        this.KAFKA_CLIENT_DNS_LOOKUP = clientDnsLookup;
        this.KAFKA_SESSION_TIMEOUT_MS = sessionTimeoutMs;
        this.KAFKA_ACKS = acks;
        this.KAFKA_CLIENT_ID = clientId;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, KAFKA_ACKS);
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, KAFKA_CLIENT_ID);
        // Security for Confluent Cloud
        props.put("security.protocol", KAFKA_SECURITY_PROTOCOL);
        props.put("sasl.mechanism", KAFKA_SASL_MECHANISM);
        props.put("sasl.jaas.config", KAFKA_SASL_JAAS_CONFIG);
        props.put("client.dns.lookup", KAFKA_CLIENT_DNS_LOOKUP);
        props.put("session.timeout.ms", KAFKA_SESSION_TIMEOUT_MS);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("com.delphi.delphi.*");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_CONSUMER_GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // Security for Confluent Cloud
        props.put("security.protocol", KAFKA_SECURITY_PROTOCOL);
        props.put("sasl.mechanism", KAFKA_SASL_MECHANISM);
        props.put("sasl.jaas.config", KAFKA_SASL_JAAS_CONFIG);
        props.put("client.dns.lookup", KAFKA_CLIENT_DNS_LOOKUP);
        props.put("session.timeout.ms", KAFKA_SESSION_TIMEOUT_MS);
        props.put("client.id", KAFKA_CLIENT_ID);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jsonDeserializer);
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(1500L, 3L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
        ConsumerFactory<String, Object> consumerFactory,
        DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(3);
        return factory;
    }
}




