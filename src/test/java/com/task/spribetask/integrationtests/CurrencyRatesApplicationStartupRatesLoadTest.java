package com.task.spribetask.integrationtests;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class CurrencyRatesApplicationStartupRatesLoadTest {

    private static final String TEST_CURRENCY_USD = "USD";
    private static final String TEST_CURRENCY_EUR = "EUR";

    @LocalServerPort
    private Integer port;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    static PostgreSQLContainer<?> postgres;
    static {
        postgres = new PostgreSQLContainer<>(
                "postgres:latest"
        );
        postgres.withInitScript("initial_rated.sql");
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("currency.service.url", wireMock::baseUrl);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        wireMock.resetAll();
    }

    @Test
    void shouldReceiveListOnGetCurrenciesListWhenThereAreRecordsPopulatedFromDB() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/currency/list")
                .then()
                .statusCode(200)
                .body(".", hasSize(2))
                .body(".", containsInAnyOrder(TEST_CURRENCY_USD, TEST_CURRENCY_EUR));
    }

}
