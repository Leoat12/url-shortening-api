package com.leoat.urlshorteningapp;

import com.leoat.urlshorteningapp.model.UrlIdRange;
import com.leoat.urlshorteningapp.service.SharedConfigurationService;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@SpringBootApplication
public class UrlShorteningAppApplication {

    @Value("${application.hashid-salt}")
    private String hashIdsSalt;

    @Value("${application.url-range-key}")
    private String urlRangeKey;

    public static void main(String[] args) {
        SpringApplication.run(UrlShorteningAppApplication.class, args);
    }

    @Bean
    public Hashids hashids() {
        return new Hashids(hashIdsSalt);
    }

    @Bean
    public ReactiveRedisOperations<String, Object> redisOperations(ReactiveRedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        RedisSerializationContext<String, Object> context =
        RedisSerializationContext.<String, Object>newSerializationContext(new StringRedisSerializer())
                .value(serializer).build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public HttpTraceRepository httpTraceRepository() {
        return new InMemoryHttpTraceRepository();
    }

    @Bean
    public UrlIdRange urlIdRange(SharedConfigurationService sharedConfigurationService) {
        Integer counter = sharedConfigurationService.getSharedCounter(urlRangeKey);
        return new UrlIdRange(counter);
    }
}
