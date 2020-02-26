package com.leoat.urlshorteningapp.service;

import com.leoat.urlshorteningapp.model.UrlGenerateRequest;
import com.leoat.urlshorteningapp.model.UrlIdRange;
import com.leoat.urlshorteningapp.model.UrlInfo;
import com.leoat.urlshorteningapp.repository.UrlInfoRepository;
import lombok.RequiredArgsConstructor;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UrlInfoServiceImpl implements UrlInfoService {

    @Value("${application.url-range-key}")
    private String urlRangeKey;

    private final UrlInfoRepository urlInfoRepository;
    private final SharedConfigurationService sharedConfigurationService;
    private final UrlIdRange urlIdRange;
    private final Hashids hashids;

    @Override
    public Mono<UrlInfo> findByLongUrl(String longUrl) {
        return urlInfoRepository.findByLongUrl(longUrl);
    }

    @Override
    public Mono<UrlInfo> findByShortUrl(String shortUrl) {
        return urlInfoRepository.findByShortUrl(shortUrl);
    }

    @Override
    public Mono<UrlInfo> saveIfNotExist(UrlGenerateRequest request) {
        return findByLongUrl(request.getLongUrl()).switchIfEmpty(save(request));
    }

    private Mono<UrlInfo> save(UrlGenerateRequest request) {
        Integer id = getUrlId();
        String hash = hashids.encode(id);
        return urlInfoRepository.save(
                UrlInfo.builder()
                        .longUrl(request.getLongUrl())
                        .shortUrl(hash)
                        .expiryAt(request.getExpiryAt())
                        .build()
        );
    }

    private Integer getUrlId() {
        if (!urlIdRange.hasNext()) {
            Integer counter = sharedConfigurationService.getSharedCounter(urlRangeKey);
            urlIdRange.calculateRange(counter);
        }
        return urlIdRange.getCurrentValue();
    }
}
