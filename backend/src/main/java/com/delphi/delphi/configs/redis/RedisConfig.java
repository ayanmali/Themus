package com.delphi.delphi.configs.redis;

import java.lang.reflect.Method;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {
	
	@Bean
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

    // @Bean
    // public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    //     RedisTemplate<String, Object> template = new RedisTemplate<>();
    //     template.setConnectionFactory(redisConnectionFactory);
    //     return template;
    // }

    // @Override
    // public CacheResolver cacheResolver() {
    //     return new SimpleCacheResolver(cacheManager(redisConnectionFactory()));
    // }

    // @Override
    // public CacheErrorHandler errorHandler() {
    //     return new SimpleCacheErrorHandler();
    // }

    // @Bean
    // public RedisConnectionFactory redisConnectionFactory() {
    //     return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    // }

}