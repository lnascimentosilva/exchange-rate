package com.challenge.forexrate.controller;

import com.challenge.forexrate.model.ExchangeRateReport;
import com.challenge.forexrate.service.ApiUsageLogService;
import com.challenge.forexrate.service.ExchangeRateService;
import com.toedter.spring.hateoas.jsonapi.MediaTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/exchange-rate", produces = MediaTypes.JSON_API_VALUE)
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;
    private final ApiUsageLogService apiUsageLogService;

    @GetMapping("/{date:^\\d{4}-\\d{2}-\\d{2}$}/{baseCurrency}/{targetCurrency}")
    public EntityModel<ExchangeRateReport> getReport(@PathVariable String date,
                                                     @PathVariable String baseCurrency,
                                                     @PathVariable String targetCurrency) {
        LocalDate parsedDate = LocalDate.parse(date);
        EntityModel<ExchangeRateReport> exchangeRateReport = EntityModel.of(exchangeRateService.getReport(parsedDate, baseCurrency, targetCurrency));
        apiUsageLogService.save(parsedDate, baseCurrency, targetCurrency);
        return exchangeRateReport;
    }
}
