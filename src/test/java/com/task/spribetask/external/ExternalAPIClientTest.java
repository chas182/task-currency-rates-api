package com.task.spribetask.external;

import com.task.spribetask.dto.external.APIError;
import com.task.spribetask.dto.external.APIResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalAPIClientTest {

    private static final String PATH_GET_LATEST_RATES = "/latest?access_key=%s";
    private static final String TEST_ACCESS_KEY = "testkey";
    private static final String TEST_BASE = "USD";
    private static final long TEST_RATE_TIMESTAMP = 1519296206;
    private static final LocalDate TEST_RATE_DATE = LocalDate.of(2024, 4, 2);
    private static final Map<String, Double> TEST_RATES = Map.of("GBP", 0.72007, "JPY", 107.346001, "EUR", 0.813399, "BTC", 1.6295132e-5);
    private static final int TEST_CODE = 105;
    private static final String TEST_TYPE = "testtype";
    private static final String TEST_INFO = "testinfo";

    private static final String JSON_200_LATEST_RATES = "payload/external/200_latest_rates.json";
    private static final String JSON_200_LATEST_RATES_WITH_ERROR = "payload/external/200_latest_rates_with_error.json";


    private MockRestServiceServer server;
    private ExternalAPIClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        client = new ExternalAPIClient(TEST_ACCESS_KEY, restTemplate);
    }

    @AfterEach
    void tearDown() {
        server.verify();
    }

    @Test
    void shouldReturnAPIResponseOnGetLatestRates() {
        server.expect(requestTo(PATH_GET_LATEST_RATES.formatted(TEST_ACCESS_KEY)))
                .andExpect(method(GET))
                .andRespond(withSuccess(getJson200LatestRates(), MediaType.APPLICATION_JSON));

        Optional<APIResponse> latestRates = client.getLatestRates();

        assertThat(latestRates).isNotEmpty();
        assertThat(latestRates.get()).isEqualTo(buildExpectedApiResponse());
    }

    @Test
    void shouldReturnAPIResponseOnGetLatestRatesWhenResponseWasNotSuccessful() {
        server.expect(requestTo(PATH_GET_LATEST_RATES.formatted(TEST_ACCESS_KEY)))
                .andExpect(method(GET))
                .andRespond(withSuccess(getJson200LatestRatesWithError(), MediaType.APPLICATION_JSON));

        Optional<APIResponse> latestRates = client.getLatestRates();

        assertThat(latestRates).isNotEmpty();
        assertThat(latestRates.get()).isEqualTo(buildExpectedApiResponseWithError());
    }

    @Test
    void shouldReturnEmptyOptionalOnGetLatestRatesWhenServiceIsUnavailable() {
        server.expect(requestTo(PATH_GET_LATEST_RATES.formatted(TEST_ACCESS_KEY)))
                .andExpect(method(GET))
                .andRespond(withServiceUnavailable());

        Optional<APIResponse> latestRates = client.getLatestRates();

        assertThat(latestRates).isEmpty();
    }


    private APIResponse buildExpectedApiResponse() {
        return new APIResponse(true, TEST_RATE_TIMESTAMP, TEST_BASE, TEST_RATE_DATE, TEST_RATES, null);
    }

    private APIResponse buildExpectedApiResponseWithError() {
        return new APIResponse(false, 0, null, null, null, new APIError(TEST_CODE, TEST_TYPE, TEST_INFO));
    }

    @SneakyThrows
    private String getJson200LatestRates() {
        return Files.readString(Paths.get("src/test/resources", JSON_200_LATEST_RATES));
    }

    @SneakyThrows
    private String getJson200LatestRatesWithError() {
        return Files.readString(Paths.get("src/test/resources", JSON_200_LATEST_RATES_WITH_ERROR));
    }
}