package com.memozy.memozy_back.global.config;

import com.memozy.memozy_back.domain.gpt.dto.ChatMessage;
import com.memozy.memozy_back.domain.memory.domain.Memory;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

    @Bean
    public RedisTemplate<String, Memory> memoryRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Memory> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 또는 Jackson2JsonRedisSerializer<>(Memory.class)
        return template;
    }

    @Bean
    public RedisTemplate<String, List<ChatMessage>> chatMessageRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, List<ChatMessage>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 타입 보존
        return template;
    }
}