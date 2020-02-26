package com.leoat.urlshorteningapp.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final ReactiveRedisOperations<String, Object> redisOps;

    @Override
    public <T> Mono<T> getFromCacheOrSupplier(String key, Class<T> clazz, Supplier<Mono<T>> orElseGet) {
        return CacheMono.lookup(k -> redisOps.opsForValue().get(buildKey(key, clazz))
                        .map(w -> Signal.next(clazz.cast(w))), key)
                .onCacheMissResume(orElseGet)
                .andWriteWith((k, v) -> Mono.fromRunnable(() ->
                        redisOps.opsForValue().setIfAbsent(buildKey(k, clazz), v.get()).subscribe()));
    }

    private <T> String buildKey(String key, Class<T> clazz) {
        return String.join("::", clazz.getName(), key);
    }
}
