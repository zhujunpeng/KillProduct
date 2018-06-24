package com.zjp.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 配置RedisTemplate，否则使用默认的RedisTemplate会使key和value乱码。
 * 
 * 也就是 spring-redis.xml
 * 
 * @author zjp
 *
 */
@Configuration
@EnableCaching
public class RedisConfig {

	@Bean
	public CacheManager cacheManager(RedisTemplate<?, ?> redisTemplate) {
		CacheManager cacheManager = new RedisCacheManager(redisTemplate);
		return cacheManager;
	}
	
	// 以下两种redisTemplate自由根据场景选择
	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
		template.setConnectionFactory(connectionFactory);
		Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
	    mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	    serializer.setObjectMapper(mapper);
	    template.setValueSerializer(serializer);
	    // 使用StringRedisSerializer来序列化和反序列化redis的key值
	    template.setKeySerializer(new StringRedisSerializer());
		// 这两句是关键
		template.setHashKeySerializer(new StringRedisSerializer());
		// 进行参数设置
		template.afterPropertiesSet();
		return template;
	}
	
	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory  connectionFactory) {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
		stringRedisTemplate.setConnectionFactory(connectionFactory);
		return stringRedisTemplate;
	}
}
