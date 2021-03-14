package com.challenge.forexrate.service;

import com.challenge.forexrate.exception.ExchangeRateDateNotFoundException;
import com.challenge.forexrate.model.ExchangeRateReport;
import com.challenge.forexrate.model.Trend;
import com.challenge.forexrate.validator.ReportRequestDateValidator;
import io.exchangeratesapi.api.client.ForeignExchangeRateService;
import io.exchangeratesapi.api.client.model.DateCurrencyMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private ExchangeRateService exchangeRateService;

    @Mock
    private ReportRequestDateValidator reportRequestDateValidator;
    @Mock
    private ForeignExchangeRateService foreignExchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new ExchangeRateService(reportRequestDateValidator, foreignExchangeRateService);
        exchangeRateService.setAverageDaysAmount(5);
    }

    @Test
    void shouldFailOnRequestValidation() {
        //Given
        LocalDate date = LocalDate.of(1999, 1, 1);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        doThrow(RuntimeException.class).when(reportRequestDateValidator).validate(date);

        //When
        Throwable throwable = catchThrowable(() -> exchangeRateService.getReport(date, baseCurrency, targetCurrency));

        //Then
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailOnExchangeRateNotFound() {
        //Given
        LocalDate date = LocalDate.of(1999, 1, 1);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        ForeignExchangeRateService.QueryParams queryParams = ForeignExchangeRateService.QueryParams.builder()
                .base(baseCurrency)
                .symbols(targetCurrency)
                .start_at(date.minus(7, ChronoUnit.DAYS))
                .end_at(date)
                .build();

        DateCurrencyMapping dateCurrencyMapping = DateCurrencyMapping.builder()
                .rates(Collections.emptyMap())
                .build();

        when(foreignExchangeRateService.getHistory(queryParams)).thenReturn(dateCurrencyMapping);

        //When
        Throwable throwable = catchThrowable(() -> exchangeRateService.getReport(date, baseCurrency, targetCurrency));

        //Then
        assertThat(throwable).isInstanceOf(ExchangeRateDateNotFoundException.class)
                .hasMessage("Requested exchange rate date could not be found");
    }

    @Test
    void shouldGetNoTrendReportExchangeRate() {
        //Given
        LocalDate date = LocalDate.of(2020, 2, 8);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        ForeignExchangeRateService.QueryParams queryParams = ForeignExchangeRateService.QueryParams.builder()
                .base(baseCurrency)
                .symbols(targetCurrency)
                .start_at(date.minus(7, ChronoUnit.DAYS))
                .end_at(date)
                .build();

        Map<String, Map<String, Double>> rates = new HashMap<>();
        rates.put("2020-02-08", Collections.singletonMap(targetCurrency, 3.45));

        DateCurrencyMapping dateCurrencyMapping = DateCurrencyMapping.builder()
                .rates(rates)
                .build();

        ExchangeRateReport expectedReport = ExchangeRateReport.builder()
                .id("USD_BRL_2020-02-08")
                .averageRate(0.0)
                .rate(3.45)
                .build();

        when(foreignExchangeRateService.getHistory(queryParams)).thenReturn(dateCurrencyMapping);

        //When
        ExchangeRateReport actualReport = exchangeRateService.getReport(date, baseCurrency, targetCurrency);

        //Then
        assertThat(actualReport).isEqualTo(expectedReport);
    }

    @Test
    void shouldGetAscendingTrendReportExchangeRate() {
        //Given
        LocalDate date = LocalDate.of(2020, 2, 8);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        ForeignExchangeRateService.QueryParams queryParams = ForeignExchangeRateService.QueryParams.builder()
                .base(baseCurrency)
                .symbols(targetCurrency)
                .start_at(date.minus(7, ChronoUnit.DAYS))
                .end_at(date)
                .build();

        Map<String, Map<String, Double>> rates = new HashMap<>();
        rates.put("2020-02-08", Collections.singletonMap(targetCurrency, 3.45));
        rates.put("2020-02-07", Collections.singletonMap(targetCurrency, 3.44));
        rates.put("2020-02-06", Collections.singletonMap(targetCurrency, 3.43));
        rates.put("2020-02-05", Collections.singletonMap(targetCurrency, 3.42));
        rates.put("2020-02-02", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-01", Collections.singletonMap(targetCurrency, 3.40));

        DateCurrencyMapping dateCurrencyMapping = DateCurrencyMapping.builder()
                .rates(rates)
                .build();

        ExchangeRateReport expectedReport = ExchangeRateReport.builder()
                .id("USD_BRL_2020-02-08")
                .averageRate(3.4200000000000004)
                .rate(3.45)
                .trend(Trend.ASCENDING.getTrendName())
                .build();

        when(foreignExchangeRateService.getHistory(queryParams)).thenReturn(dateCurrencyMapping);

        //When
        ExchangeRateReport actualReport = exchangeRateService.getReport(date, baseCurrency, targetCurrency);

        //Then
        assertThat(actualReport).isEqualTo(expectedReport);
    }

    @Test
    void shouldGetDescendingTrendReportExchangeRate() {
        //Given
        LocalDate date = LocalDate.of(2020, 2, 8);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        ForeignExchangeRateService.QueryParams queryParams = ForeignExchangeRateService.QueryParams.builder()
                .base(baseCurrency)
                .symbols(targetCurrency)
                .start_at(date.minus(7, ChronoUnit.DAYS))
                .end_at(date)
                .build();

        Map<String, Map<String, Double>> rates = new HashMap<>();
        rates.put("2020-02-08", Collections.singletonMap(targetCurrency, 3.40));
        rates.put("2020-02-07", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-06", Collections.singletonMap(targetCurrency, 3.42));
        rates.put("2020-02-05", Collections.singletonMap(targetCurrency, 3.43));
        rates.put("2020-02-02", Collections.singletonMap(targetCurrency, 3.44));
        rates.put("2020-02-01", Collections.singletonMap(targetCurrency, 3.45));

        DateCurrencyMapping dateCurrencyMapping = DateCurrencyMapping.builder()
                .rates(rates)
                .build();

        ExchangeRateReport expectedReport = ExchangeRateReport.builder()
                .id("USD_BRL_2020-02-08")
                .averageRate(3.4299999999999997)
                .rate(3.40)
                .trend(Trend.DESCENDING.getTrendName())
                .build();

        when(foreignExchangeRateService.getHistory(queryParams)).thenReturn(dateCurrencyMapping);

        //When
        ExchangeRateReport actualReport = exchangeRateService.getReport(date, baseCurrency, targetCurrency);

        //Then
        assertThat(actualReport).isEqualTo(expectedReport);
    }

    @Test
    void shouldGetConstantTrendReportExchangeRate() {
        //Given
        LocalDate date = LocalDate.of(2020, 2, 8);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        ForeignExchangeRateService.QueryParams queryParams = ForeignExchangeRateService.QueryParams.builder()
                .base(baseCurrency)
                .symbols(targetCurrency)
                .start_at(date.minus(7, ChronoUnit.DAYS))
                .end_at(date)
                .build();

        Map<String, Map<String, Double>> rates = new HashMap<>();
        rates.put("2020-02-08", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-07", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-06", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-05", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-02", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-01", Collections.singletonMap(targetCurrency, 3.41));

        DateCurrencyMapping dateCurrencyMapping = DateCurrencyMapping.builder()
                .rates(rates)
                .build();

        ExchangeRateReport expectedReport = ExchangeRateReport.builder()
                .id("USD_BRL_2020-02-08")
                .averageRate(3.41)
                .rate(3.41)
                .trend(Trend.CONSTANT.getTrendName())
                .build();

        when(foreignExchangeRateService.getHistory(queryParams)).thenReturn(dateCurrencyMapping);

        //When
        ExchangeRateReport actualReport = exchangeRateService.getReport(date, baseCurrency, targetCurrency);

        //Then
        assertThat(actualReport).isEqualTo(expectedReport);
    }

    @Test
    void shouldGetUndefinedTrendReportExchangeRate() {
        //Given
        LocalDate date = LocalDate.of(2020, 2, 8);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        ForeignExchangeRateService.QueryParams queryParams = ForeignExchangeRateService.QueryParams.builder()
                .base(baseCurrency)
                .symbols(targetCurrency)
                .start_at(date.minus(7, ChronoUnit.DAYS))
                .end_at(date)
                .build();

        Map<String, Map<String, Double>> rates = new HashMap<>();
        rates.put("2020-02-08", Collections.singletonMap(targetCurrency, 3.41));
        rates.put("2020-02-07", Collections.singletonMap(targetCurrency, 3.43));
        rates.put("2020-02-06", Collections.singletonMap(targetCurrency, 3.42));
        rates.put("2020-02-05", Collections.singletonMap(targetCurrency, 3.45));
        rates.put("2020-02-02", Collections.singletonMap(targetCurrency, 3.40));
        rates.put("2020-02-01", Collections.singletonMap(targetCurrency, 3.39));

        DateCurrencyMapping dateCurrencyMapping = DateCurrencyMapping.builder()
                .rates(rates)
                .build();

        ExchangeRateReport expectedReport = ExchangeRateReport.builder()
                .id("USD_BRL_2020-02-08")
                .averageRate(3.418)
                .rate(3.41)
                .trend(Trend.UNDEFINED.getTrendName())
                .build();

        when(foreignExchangeRateService.getHistory(queryParams)).thenReturn(dateCurrencyMapping);

        //When
        ExchangeRateReport actualReport = exchangeRateService.getReport(date, baseCurrency, targetCurrency);

        //Then
        assertThat(actualReport).isEqualTo(expectedReport);
    }
}