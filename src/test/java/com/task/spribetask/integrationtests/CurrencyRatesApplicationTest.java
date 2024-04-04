package com.task.spribetask.integrationtests;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.task.spribetask.repository.CurrencyRatesRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class CurrencyRatesApplicationTest {

    private static final String TEST_CURRENCY_USD = "USD";
    private static final String TEST_CURRENCY_EUR = "EUR";
    private static final LocalDate TEST_RATE_DATE = LocalDate.of(2024, 4, 2);
    private static final LocalDateTime TEST_RATE_DATE_TIME = LocalDateTime.of(TEST_RATE_DATE, LocalTime.MAX);
    private static final long TEST_TIMESTAMP = TEST_RATE_DATE_TIME.atZone(ZoneId.systemDefault()).toEpochSecond();
    private static final long TEST_NEXT_TIMESTAMP = TEST_RATE_DATE_TIME.plusSeconds(30).atZone(ZoneId.systemDefault()).toEpochSecond();

    private static final String JSON_200_LATEST_RATES = "payload/integrationtests/wiremock/200_latest_rates.json";

    @LocalServerPort
    private Integer port;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @Autowired
    private CurrencyRatesRepository currencyRatesRepository;

    @Value("${currency.service.access.key}")
    private String accessKey;

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
        wireMock.stubFor(WireMock.get(urlPathMatching("/latest"))
                        .withQueryParam("access_key", WireMock.equalTo(accessKey))
                .willReturn(okJson(getJson200LatestRates()
                        .formatted(TEST_TIMESTAMP, TEST_CURRENCY_EUR, TEST_RATE_DATE.format(DateTimeFormatter.ISO_DATE)))));
    }

    @AfterEach
    void tearDown() {
        wireMock.resetAll();
    }

    @Test
    @Order(1)
    void shouldReceiveEmptyListOnGetCurrenciesListWhenThereAreNoRecordsInDB() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/currency/list")
                .then()
                .statusCode(200)
                .body(".", hasSize(0));
    }

    @ParameterizedTest
    @Order(2)
    @ValueSource(strings = {TEST_CURRENCY_EUR, TEST_CURRENCY_USD})
    void shouldReceiveAPIErrorOnGetLatestRatesWhenThereAreNoRecordsInDB(String currency) {
        given()
                .queryParam("currency", currency)
                .contentType(ContentType.JSON)
                .when()
                .get("/currency/latest")
                .then()
                .statusCode(400)
                .body("message", is("Currency not found"))
                .body("detailedMessage", is("Currency was not registered or we can't retrieve rates from external API"));
    }

    @Test
    @Order(3)
    void shouldAddOneCurrencyAndThenGetRates() {
        given()
                .queryParam("currency", TEST_CURRENCY_EUR)
                .contentType(ContentType.JSON)
                .when()
                .post("/currency/add")
                .then()
                .statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS)
                .until(() -> currencyRatesRepository.findFirstByCurrencyOrderByDateTimeDesc(TEST_CURRENCY_EUR).isPresent());

        given()
                .queryParam("currency", TEST_CURRENCY_EUR)
                .contentType(ContentType.JSON)
                .when()
                .get("/currency/latest")
                .then()
                .statusCode(200)
                .body("currency", is(TEST_CURRENCY_EUR))
                .body("rates", aMapWithSize(2));
    }

    @Test
    @Order(4)
    void shouldAddSecondCurrency() {
        given()
                .queryParam("currency", TEST_CURRENCY_USD)
                .contentType(ContentType.JSON)
                .when()
                .post("/currency/add")
                .then()
                .statusCode(200);
        await().atMost(10, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS)
                .until(() -> currencyRatesRepository.findFirstByCurrencyOrderByDateTimeDesc(TEST_CURRENCY_USD).isPresent());

        given()
                .queryParam("currency", TEST_CURRENCY_USD)
                .contentType(ContentType.JSON)
                .when()
                .get("/currency/latest")
                .then()
                .statusCode(200)
                .body("currency", is(TEST_CURRENCY_USD))
                .body("rates", aMapWithSize(2));
    }

    @Test
    @Order(5)
    void shouldReceiveAPIErrorOnAddSecondCurrencyWhenAddingAlreadyExistingCurrency() {
        given()
                .queryParam("currency", TEST_CURRENCY_USD)
                .contentType(ContentType.JSON)
                .when()
                .post("/currency/add")
                .then()
                .statusCode(400)
                .body("message", is("Duplicated currency"))
                .body("detailedMessage", is("Currency was already registered"));
    }

    @Test
    @Order(6)
    void shouldReceiveListOnGetCurrenciesListWhenThereAreRecordsInDB() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/currency/list")
                .then()
                .statusCode(200)
                .body(".", hasSize(2))
                .body(".", containsInAnyOrder(TEST_CURRENCY_USD, TEST_CURRENCY_EUR));
    }

    @Test
    @Order(7)
    void shouldWaitTillScheduleTaskRetrievesNextRates() {
        var eurRatesOld = currencyRatesRepository.findFirstByCurrencyOrderByDateTimeDesc(TEST_CURRENCY_EUR);
        var usdRatesOld = currencyRatesRepository.findFirstByCurrencyOrderByDateTimeDesc(TEST_CURRENCY_USD);
        wireMock.stubFor(WireMock.get(urlPathMatching("/latest"))
                .withQueryParam("access_key", WireMock.equalTo(accessKey))
                .willReturn(okJson(getJson200LatestRates()
                        .formatted(TEST_NEXT_TIMESTAMP, TEST_CURRENCY_EUR, TEST_RATE_DATE.format(DateTimeFormatter.ISO_DATE)))));

        await().atMost(40, TimeUnit.SECONDS).pollInterval(10, TimeUnit.SECONDS)
                .until(() -> currencyRatesRepository.findAll().size() > 3);

        var eurRatesNew = currencyRatesRepository.findFirstByCurrencyOrderByDateTimeDesc(TEST_CURRENCY_EUR);
        var usdRatesNew = currencyRatesRepository.findFirstByCurrencyOrderByDateTimeDesc(TEST_CURRENCY_USD);

        assertThat(eurRatesOld.get().getDateTime()).isBefore(eurRatesNew.get().getDateTime());
        assertThat(usdRatesOld.get().getDateTime()).isBefore(usdRatesNew.get().getDateTime());
    }

    @SneakyThrows
    private static String getJson200LatestRates() {
        return Files.readString(Paths.get("src/test/resources", JSON_200_LATEST_RATES));
    }
}
