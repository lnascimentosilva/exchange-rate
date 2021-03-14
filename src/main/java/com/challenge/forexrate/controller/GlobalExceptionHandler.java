package com.challenge.forexrate.controller;

import com.challenge.forexrate.exception.ExchangeRateDateNotFoundException;
import com.challenge.forexrate.exception.ExchangeRateDateOutOfBoundsException;
import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import com.toedter.spring.hateoas.jsonapi.MediaTypes;
import feign.FeignException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.DateTimeException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern FAULTY_CURRENCY_PATTERN = Pattern.compile("\\{\"error\":\"(?:Base|Symbols) '(.+)' (?:is not supported|are invalid)\\.\"}");

    @ExceptionHandler(DateTimeException.class)
    public ResponseEntity<JsonApiErrors> handleInvalidDate(DateTimeException exception) {
        return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE)
                .body(
                        JsonApiErrors.create().withError(
                                JsonApiError.create()
                                        .withTitle("Invalid date")
                                        .withStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                                        .withDetail(exception.getMessage())));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<JsonApiErrors> handleFeignExceptionInvalidDate(FeignException exception) {
        Matcher matcher = FAULTY_CURRENCY_PATTERN.matcher(exception.contentUTF8());
        if (HttpStatus.BAD_REQUEST.value() == exception.status()
                && matcher.find()) {

            return ResponseEntity.badRequest()
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE)
                    .body(
                            JsonApiErrors.create().withError(
                                    JsonApiError.create()
                                            .withTitle("Not supported currency")
                                            .withStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                                            .withDetail(String.format("Used currency '%s' is not supported", matcher.group(1)))));
        }

        return handleUnknownError(new RuntimeException("Error occurred while fetching data from exchangeratesapi.io"));
    }

    @ExceptionHandler(ExchangeRateDateOutOfBoundsException.class)
    public ResponseEntity<JsonApiErrors> handleDateOutOfBounds(ExchangeRateDateOutOfBoundsException exception) {
        return ResponseEntity.badRequest()
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE)
                .body(
                        JsonApiErrors.create().withError(
                                JsonApiError.create()
                                        .withTitle("Date out of bounds")
                                        .withStatus(String.valueOf(HttpStatus.BAD_REQUEST.value()))
                                        .withDetail(exception.getMessage())));
    }

    @ExceptionHandler(ExchangeRateDateNotFoundException.class)
    public ResponseEntity<JsonApiErrors> handleExchangeRateDateNotFound(ExchangeRateDateNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE)
                .body(
                        JsonApiErrors.create().withError(
                                JsonApiError.create()
                                        .withTitle("Exchange rate not found")
                                        .withStatus(String.valueOf(HttpStatus.NOT_FOUND.value()))
                                        .withDetail(exception.getMessage())));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<JsonApiErrors> handleUnknownError(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, MediaTypes.JSON_API_VALUE)
                .body(
                        JsonApiErrors.create().withError(
                                JsonApiError.create()
                                        .withTitle("Unknown error")
                                        .withStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                        .withDetail(exception.getMessage())));
    }
}
