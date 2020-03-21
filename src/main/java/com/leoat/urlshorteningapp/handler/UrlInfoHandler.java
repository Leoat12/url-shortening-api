package com.leoat.urlshorteningapp.handler;

import com.leoat.urlshorteningapp.model.ErrorCode;
import com.leoat.urlshorteningapp.model.ErrorResponse;
import com.leoat.urlshorteningapp.model.UrlGenerateRequest;
import com.leoat.urlshorteningapp.model.UrlInfo;
import com.leoat.urlshorteningapp.service.CacheService;
import com.leoat.urlshorteningapp.service.UrlInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlInfoHandler {

    @Value("${application.short-url-domain}")
    private String shortUrlDomain;

    private final UrlInfoService urlInfoService;
    private final CacheService cacheService;
    private final WebClient webClient;

    public Mono<ServerResponse> findLongUrlAndRedirect(ServerRequest serverRequest) {
        String shortUrl = serverRequest.pathVariable("url");
        return cacheService.getFromCacheOrSupplier(shortUrl, UrlInfo.class, () -> urlInfoService.findByShortUrl(shortUrl))
                .doOnNext(urlInfo -> log.info("UrlInfo found: {}", urlInfo))
                .flatMap(urlInfo -> permanentRedirect(URI.create(urlInfo.getLongUrl())).build())
                .switchIfEmpty(status(HttpStatus.NOT_FOUND)
                        .bodyValue(ErrorResponse.notFound(shortUrl, ErrorCode.URL_INFO_NOT_FOUND)));
    }

    public Mono<ServerResponse> generateAndSaveShortUrl(ServerRequest serverRequest) {
        return serverRequest.body(BodyExtractors.toMono(UrlGenerateRequest.class))
                .flatMap(urlInfoService::saveIfNotExist)
                .doOnNext(urlInfo -> log.info("UrlInfo created: {}", urlInfo))
                .flatMap(urlInfo -> status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(appendUrlDomain(urlInfo)))
                .switchIfEmpty(status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .bodyValue(ErrorResponse.unexpectedError("Unexpected error occurred while generating URL.")));
    }

    private UrlInfo appendUrlDomain(UrlInfo urlInfo) {
        if(shortUrlDomain.endsWith("/"))
            urlInfo.setShortUrl(shortUrlDomain.concat(urlInfo.getShortUrl()));
        else
            urlInfo.setShortUrl(String.join("/", shortUrlDomain, urlInfo.getShortUrl()));

        return urlInfo;
    }
}
