package com.leoat.urlshorteningapp;

import com.leoat.urlshorteningapp.handler.ErrorHandler;
import com.leoat.urlshorteningapp.handler.UrlInfoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class UrlShorteningRouter {

    @Bean
    public RouterFunction<ServerResponse> route(UrlInfoHandler handler, ErrorHandler errorHandler) {
        return RouterFunctions.route()
                .onError(Exception.class, errorHandler::handleError)
                .GET("/{url}", handler::findLongUrlAndRedirect)
                .POST("/", accept(MediaType.APPLICATION_JSON), handler::generateAndSaveShortUrl)
                .build();
    }


}
