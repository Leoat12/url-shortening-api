package com.leoat.urlshorteningapp.repository;

import com.leoat.urlshorteningapp.model.UrlInfo;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UrlInfoRepository extends ReactiveCrudRepository<UrlInfo, Long> {

    @Query("SELECT * FROM url_info WHERE long_url = :longUrl")
    Mono<UrlInfo> findByLongUrl(String longUrl);

    @Query("SELECT * FROM url_info WHERE short_url = :shortUrl")
    Mono<UrlInfo> findByShortUrl(String shortUrl);

}
