package com.unicomer.qa.api;

import com.unicomer.qa.config.ConfigManager;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cliente HTTP del servicio Cat Facts. Solo ejecuta solicitudes; las validaciones viven en las pruebas.
 */
public class CatFactsClient {

    private static final Logger LOG = LoggerFactory.getLogger(CatFactsClient.class);
    private static final String FACTS_PATH = "/facts";

    private final RequestSpecification specification;

    public CatFactsClient() {
        this(ConfigManager.apiBaseUrl(), ConfigManager.apiRequestTimeoutMs());
    }

    public CatFactsClient(String baseUrl, int requestTimeoutMs) {
        // Evita que una solicitud bloqueada detenga la ejecución indefinidamente.
        Duration timeout = Duration.ofMillis(requestTimeoutMs);
        HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig()
                .connectTimeout(timeout)
                .readTimeout(timeout);
        this.specification = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setAccept(ContentType.JSON)
                .setConfig(RestAssuredConfig.config().httpClient(httpClientConfig))
                .addFilter(new AllureRestAssured())
                .build();
    }

    public Response getFacts() {
        return getFacts(Map.of());
    }

    @Step("GET /facts con parámetros {queryParams}")
    public Response getFacts(Map<String, ?> queryParams) {
        Response response = RestAssured.given(specification)
                .queryParams(queryParams)
                .when()
                .get(FACTS_PATH)
                .andReturn();
        LOG.info("GET {} {} -> {} en {} ms", FACTS_PATH, queryParams, response.statusCode(),
                response.getTimeIn(TimeUnit.MILLISECONDS));
        return response;
    }
}
