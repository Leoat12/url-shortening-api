package com.leoat.urlshorteningapp.service;

import reactor.core.publisher.Mono;

import java.util.function.Supplier;

public interface CacheService {

    <T> Mono<T> getFromCacheOrSupplier(String key, Class<T> clazz, Supplier<Mono<T>> orElseGet);

}
