package com.leoat.urlshorteningapp.service;

import com.leoat.urlshorteningapp.model.UrlGenerateRequest;
import com.leoat.urlshorteningapp.model.UrlInfo;
import reactor.core.publisher.Mono;

public interface UrlInfoService {

    Mono<UrlInfo> findByLongUrl(String longUrl);
    Mono<UrlInfo> findByShortUrl(String shortUrl);
    Mono<UrlInfo> saveIfNotExist(UrlGenerateRequest request);

}
