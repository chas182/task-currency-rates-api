package com.task.spribetask.resource;

import com.task.spribetask.dto.CurrencyRates;
import com.task.spribetask.resource.exception.ApiError;
import com.task.spribetask.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("currency")
@RequiredArgsConstructor
public class CurrencyResource {

    private final CurrencyService currencyService;

    @Operation(summary = "Get list of currencies used in application")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of currencies used in application",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class), examples = @ExampleObject(value = """
                            ["USD","EUR"]
                            """)) }),
            @ApiResponse(responseCode = "500", description = "Unknown Error",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(value = """
                            {
                              "message": "Unknown error",
                              "detailedMessage": "Exception message",
                              "timestamp": "2024-04-02T08:23:00"
                            }
                            """)) })
    })
    @GetMapping("list")
    public List<String> currenciesList() {
        log.info("Entering::currenciesList");
        return currencyService.getAllCurrencies();
    }

    @Operation(summary = "Get exchange rate for a currency")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exchange rate for a currency",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CurrencyRates.class), examples = @ExampleObject(value = """
                            {
                              "currency": "USD",
                              "dateTime": "2024-04-02T08:23:00",
                              "rates": {
                                "EUR": 0.72007,
                                "JPY": 107.346001
                              }
                            }
                            """)) }),
            @ApiResponse(responseCode = "400", description = "Input currency was not found in application",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(value = """
                            {
                              "message": "Currency not found",
                              "detailedMessage": "Currency was not registered or we can't retrieve rates from external API",
                              "timestamp": "2024-04-02T08:23:00"
                            }
                            """)) }),
            @ApiResponse(responseCode = "500", description = "Unknown Error",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(value = """
                            {
                              "message": "Unknown error",
                              "detailedMessage": "Exception message",
                              "timestamp": "2024-04-02T08:23:00"
                            }
                            """)) })
    })
    @GetMapping("latest")
    public Optional<CurrencyRates> getLatestRate(@Parameter(description = "Currency to be retrieved") @RequestParam String currency) {
        log.info("Entering::latest");
        return Optional.ofNullable(currencyService.getCurrencyRate(currency));
    }

    @Operation(summary = "Add new currency for getting exchange rates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Currency added to application"),
            @ApiResponse(responseCode = "400", description = "Input currency was already added to application",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(value = """
                            {
                              "message": "Duplicated currency",
                              "detailedMessage": "Currency was already registered",
                              "timestamp": "2024-04-02T08:23:00"
                            }
                            """)) }),
            @ApiResponse(responseCode = "500", description = "Unknown Error",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class), examples = @ExampleObject(value = """
                            {
                              "message": "Unknown error",
                              "detailedMessage": "Exception message",
                              "timestamp": "2024-04-02T08:23:00"
                            }
                            """)) })
    })
    @PostMapping("add")
    public void addCurrency(@Parameter(description = "Currency to be added") @RequestParam String currency) {
        log.info("Entering::addCurrency");
        currencyService.addCurrency(currency);
    }
}
