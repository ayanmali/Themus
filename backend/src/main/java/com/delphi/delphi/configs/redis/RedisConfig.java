package com.delphi.delphi.configs.redis;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    private final String REDIS_HOST;
    private final int REDIS_PORT;
    private final int REDIS_DATABASE;
    private final String REDIS_PASSWORD;
    private final int REDIS_MAX_ACTIVE;
    private final int REDIS_MAX_IDLE;
    private final int REDIS_MIN_IDLE;
    private final int REDIS_MAX_WAIT;
    private final int REDIS_TIMEOUT;
    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    public RedisConfig(@Value("${spring.data.redis.host}") String redisHost,
            @Value("${spring.data.redis.port}") int redisPort,
            @Value("${spring.data.redis.database}") int redisDatabase,
            @Value("${spring.data.redis.password}") String redisPassword,
            @Value("${spring.data.redis.lettuce.pool.max-active}") int redisMaxActive,
            @Value("${spring.data.redis.lettuce.pool.max-idle}") int redisMaxIdle,
            @Value("${spring.data.redis.lettuce.pool.min-idle}") int redisMinIdle,
            @Value("${spring.data.redis.lettuce.pool.max-wait}") int redisMaxWait,
            @Value("${spring.data.redis.lettuce.shutdown-timeout}") int redisTimeout) {
        this.REDIS_HOST = redisHost;
        this.REDIS_PORT = redisPort;
        this.REDIS_DATABASE = redisDatabase;
        this.REDIS_PASSWORD = redisPassword;
        this.REDIS_MAX_ACTIVE = redisMaxActive;
        this.REDIS_MAX_IDLE = redisMaxIdle;
        this.REDIS_MIN_IDLE = redisMinIdle;
        this.REDIS_MAX_WAIT = redisMaxWait;
        this.REDIS_TIMEOUT = redisTimeout;
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (Object target, Method method, Object... params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName());
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append(obj.toString());
            }
            return sb.toString();
        };
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default 30 minutes
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer(new ObjectMapper())))
                .computePrefixWith(cacheName -> "cache:" + cacheName + ":");

        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
            "users", defaultCacheConfig.entryTtl(Duration.ofHours(1)),
            "assessments", defaultCacheConfig.entryTtl(Duration.ofMinutes(15)),
            "candidates", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)),
            "evaluations", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)),
            "stripe-subscriptions", defaultCacheConfig.entryTtl(Duration.ofHours(2)),
            "auth-tokens", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)),
            "rate-limits", defaultCacheConfig.entryTtl(Duration.ofMinutes(1))
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .enableStatistics() // Enable cache statistics
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                logger.warn("Cache GET error for cache '{}' and key '{}': {}", 
                           cache.getName(), key, exception.getMessage());
                // Continue without cache - fail gracefully
            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, @Nullable Object value) {
                logger.error("Cache PUT error for cache '{}' and key '{}': {}", 
                            cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                logger.warn("Cache EVICT error for cache '{}' and key '{}': {}", 
                           cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
                logger.error("Cache CLEAR error for cache '{}': {}", 
                            cache.getName(), exception.getMessage());
            }
        };
    }

    // @Bean
    // public CacheMetricsRegistrar cacheMetricsRegistrar() {
    //     return new CacheMetricsRegistrar();
    // }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        return getTemplate(redisConnectionFactory());
    }

    private RedisConnectionFactory redisConnectionFactory() {
        return connectionFactory(REDIS_MAX_ACTIVE, REDIS_MAX_IDLE, REDIS_MIN_IDLE, REDIS_MAX_WAIT, REDIS_HOST,
                REDIS_PASSWORD, REDIS_TIMEOUT, REDIS_PORT, REDIS_DATABASE);
    }

    /**
     * Creates a connection factory for the Redis database
     * 
     * @param maxActive
     * @param maxIdle
     * @param minIdle
     * @param maxWait
     * @param host
     * @param password
     * @param timeout
     * @param port
     * @param database
     * @return
     */
    private RedisConnectionFactory connectionFactory(Integer maxActive,
            Integer maxIdle,
            Integer minIdle,
            Integer maxWait,
            String host,
            String password,
            Integer timeout,
            Integer port,
            Integer database) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setDatabase(database);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWait));
        LettuceClientConfiguration lettucePoolingConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig).shutdownTimeout(Duration.ofMillis(timeout)).build();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration,
                lettucePoolingConfig);
        connectionFactory.afterPropertiesSet();

        return connectionFactory;
    }

    /**
     * Creates a RedisTemplate for the Redis database
     *
     * @param factory
     * @return
     */
    private RedisTemplate<String, Object> getTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper objectMapper = createObjectMapper();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = jackson2JsonRedisSerializer(objectMapper);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setValueSerializer(jsonSerializer);
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // TODO: Enable transaction support?
        // template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Serializes the value to JSON
     *
     * @param objectMapper
     * @return
     */
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer(ObjectMapper objectMapper) {
        // objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
        //         JsonTypeInfo.As.WRAPPER_ARRAY);
        // objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, 
                                    ObjectMapper.DefaultTyping.NON_FINAL, 
                                    JsonTypeInfo.As.WRAPPER_ARRAY);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}