package com.challenge.forexrate.service;

import com.challenge.forexrate.exception.ExchangeRateDateNotFoundException;
import com.challenge.forexrate.model.ExchangeRateReport;
import com.challenge.forexrate.model.Trend;
import com.challenge.forexrate.validator.ReportRequestDateValidator;
import io.exchangeratesapi.api.client.ForeignExchangeRateService;
import io.exchangeratesapi.api.client.model.DateCurrencyMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ReportRequestDateValidator reportRequestDateValidator;
    private final ForeignExchangeRateService foreignExchangeRateService;
    private int averageDaysAmount;

    public ExchangeRateReport getReport(LocalDate date, String baseCurrency, String targetCurrency) {

        reportRequestDateValidator.validate(date);

        ForeignExchangeRateService.QueryParams queryParams = buildForeignExchangeRateServiceParams(date, baseCurrency, targetCurrency);

        DateCurrencyMapping dateCurrencyMappingHistory = foreignExchangeRateService.getHistory(queryParams);

        Double rate;
        try {
            rate = getRequestedDateExchangeRate(date, targetCurrency, dateCurrencyMappingHistory);
        } catch (NullPointerException e) {
            throw new ExchangeRateDateNotFoundException("Requested exchange rate date could not be found");
        }

        List<Double> ratesSortedByDate = getRatesSortedByDate(targetCurrency, dateCurrencyMappingHistory);

        return ExchangeRateReport.builder()
                .id(baseCurrency + "_" + targetCurrency + "_" + date.toString())
                .rate(rate)
                .averageRate(calculateAverageRate(ratesSortedByDate))
                .trend(calculateTrend(ratesSortedByDate))
                .build();
    }

    private ForeignExchangeRateService.QueryParams buildForeignExchangeRateServiceParams(LocalDate date, String baseCurrency, String targetCurrency) {
        return ForeignExchangeRateService.QueryParams.builder()
                //gets the requested date + days necessary for calculating the average
                .start_at(date.minus(averageDaysAmount + 2, ChronoUnit.DAYS))
                .end_at(date)
                .base(baseCurrency)
                .symbols(targetCurrency)
                .build();
    }

    private Double getRequestedDateExchangeRate(LocalDate date, String targetCurrency, DateCurrencyMapping dateCurrencyMappingHistory) {
        return dateCurrencyMappingHistory.getRates().remove(date.toString()).get(targetCurrency);
    }

    private List<Double> getRatesSortedByDate(String targetCurrency, DateCurrencyMapping dateCurrencyMappingHistory) {
        return dateCurrencyMappingHistory.getRates()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .map(currency -> currency.get(targetCurrency))
                .collect(Collectors.toList());
    }

    private double calculateAverageRate(List<Double> ratesSortedByDate) {
        return ratesSortedByDate.stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    private String calculateTrend(List<Double> ratesSortedByDate) {
        Trend trend = null;
        for (int i = 0; i < ratesSortedByDate.size() - 1; i++) {
            Double current = ratesSortedByDate.get(i);
            Double next = ratesSortedByDate.get(i + 1);

            if (current < next && hasSameTrendOrNoTrend(trend, Trend.ASCENDING)) {
                trend = Trend.ASCENDING;
            } else if (current > next && (hasSameTrendOrNoTrend(trend, Trend.DESCENDING))) {
                trend = Trend.DESCENDING;
            } else if (current.equals(next) && (hasSameTrendOrNoTrend(trend, Trend.CONSTANT))) {
                trend = Trend.CONSTANT;
            } else {
                trend = Trend.UNDEFINED;
                break;
            }
        }

        if (trend != null) {
            return trend.getTrendName();
        }
        return null;
    }

    private boolean hasSameTrendOrNoTrend(Trend previousTrend, Trend currentTrend) {
        return currentTrend == previousTrend || previousTrend == null;
    }

    @Autowired
    void setAverageDaysAmount(@Value("${report.average.days-amount:5}") int averageDaysAmount) {
        this.averageDaysAmount = averageDaysAmount;
    }
}
