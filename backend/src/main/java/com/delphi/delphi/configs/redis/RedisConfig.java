package com.delphi.delphi.configs.redis;

import java.lang.reflect.Method;
import java.time.Duration;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;

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

    public RedisConfig(@Value("${spring.data.redis.host}") String redisHost, @Value("${spring.data.redis.port}") int redisPort, @Value("${spring.data.redis.database}") int redisDatabase, @Value("${spring.data.redis.password}") String redisPassword, @Value("${spring.data.redis.lettuce.pool.max-active}") int redisMaxActive, @Value("${spring.data.redis.lettuce.pool.max-idle}") int redisMaxIdle, @Value("${spring.data.redis.lettuce.pool.min-idle}") int redisMinIdle, @Value("${spring.data.redis.lettuce.pool.max-wait}") int redisMaxWait, @Value("${spring.data.redis.lettuce.pool.time-between-eviction-runs-ms}") int redisTimeout) {
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

    // @Bean
    // public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    //     return RedisCacheManager.create(redisConnectionFactory);
    // }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    // @Override
    // public CacheResolver cacheResolver() {
    //     return new SimpleCacheResolver(cacheManager(redisConnectionFactory()));
    // }

    // @Override
    // public CacheErrorHandler errorHandler() {
    //     return new SimpleCacheErrorHandler();
    // }

    @Bean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate() {
        return getTemplate(redisConnectionFactory());
    }

    private RedisConnectionFactory redisConnectionFactory() {
        return connectionFactory(REDIS_MAX_ACTIVE, REDIS_MAX_IDLE, REDIS_MIN_IDLE, REDIS_MAX_WAIT, REDIS_HOST, REDIS_PASSWORD, REDIS_TIMEOUT, REDIS_PORT, REDIS_DATABASE);
    }


    /**
     * Creates a connection factory for the Redis database
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
    private RedisTemplate<Object, Object> getTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setValueSerializer(jackson2JsonRedisSerializer(new ObjectMapper()));
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jackson2JsonRedisSerializer(new ObjectMapper()));

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
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

}