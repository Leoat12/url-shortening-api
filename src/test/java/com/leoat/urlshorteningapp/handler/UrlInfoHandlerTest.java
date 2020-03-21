package com.leoat.urlshorteningapp.handler;

import com.leoat.urlshorteningapp.ConstrainedFields;
import com.leoat.urlshorteningapp.TestConfig;
import com.leoat.urlshorteningapp.UrlShorteningRouter;
import com.leoat.urlshorteningapp.model.UrlGenerateRequest;
import com.leoat.urlshorteningapp.model.UrlInfo;
import com.leoat.urlshorteningapp.service.CacheService;
import com.leoat.urlshorteningapp.service.UrlInfoService;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration;


@WebFluxTest
@Import(TestConfig.class)
@ExtendWith(RestDocumentationExtension.class)
@ContextConfiguration(classes = {UrlShorteningRouter.class, UrlInfoHandler.class, ErrorHandler.class})
public class UrlInfoHandlerTest {

    @Autowired
    private Environment environment;

    @MockBean
    private UrlInfoService urlInfoService;

    @MockBean
    private CacheService cacheService;

    private WebTestClient webTestClient;

    public static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    public void setUpEach(ApplicationContext applicationContext,
                      RestDocumentationContextProvider restDocumentation) {

        this.webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
                .configureClient()
                .baseUrl("https://api.example.com")
                .filter(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    public void shouldRedirectToLongUrl() {
        doReturn(Mono.just(urlInfo())).when(cacheService)
                .getFromCacheOrSupplier(eq("AA"), eq(UrlInfo.class), any(Supplier.class));

        this.webTestClient.get().uri("/{url}", "AA").exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", urlInfo().getLongUrl())
                .expectBody()
                .consumeWith(document("redirect-long-url",
                        pathParameters(
                                parameterWithName("url").description("The hash representing the short URL.")
                        )));
    }

    @Test
    public void shouldGenerateShortUrlFromRequest() {
        doReturn(Mono.just(urlInfo())).when(urlInfoService).saveIfNotExist(urlGenerateRequest());

        String shortUrl = environment.getProperty("application.short-url-domain") + "/" + urlInfo().getShortUrl();

        ConstrainedFields fields = new ConstrainedFields(UrlGenerateRequest.class);

        this.webTestClient.post().uri("/").bodyValue(urlGenerateRequest()).exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("id").isEqualTo(1L)
                .jsonPath("longUrl").isEqualTo("http://www.example.com")
                .jsonPath("shortUrl").isEqualTo(shortUrl)
                .consumeWith(document("generate-short-url",
                        requestFields(
                                fields.withPath("longUrl").description("Long URL to generate a short URL from."),
                                fields.withPath("expiryAt").optional()
                                        .description("Expiration date of the short URL. If not given the URL will never expire.")
                        ),
                        responseFields(
                                fieldWithPath("id").description("Identifier number fo the URL."),
                                fieldWithPath("longUrl").description("Long URL that the short URL represents."),
                                fieldWithPath("shortUrl").description("Short URL that represents the long one."),
                                fieldWithPath("expiryAt").optional().description("Denotes when the URL expires.")
                        )));
    }

    private UrlInfo urlInfo() {
        return UrlInfo.builder()
                .id(1)
                .longUrl("http://www.example.com")
                .shortUrl("AA")
                .expiryAt(LocalDate.of(2020, 2, 24))
                .build();
    }

    private UrlGenerateRequest urlGenerateRequest() {
        return UrlGenerateRequest.builder()
                .longUrl("http://www.example.com")
                .expiryAt(LocalDate.of(2020, 2, 24))
                .build();
    }

}
