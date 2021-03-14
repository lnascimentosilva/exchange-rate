package io.exchangeratesapi.api.client;

import io.exchangeratesapi.api.client.model.DateCurrencyMapping;
import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@FeignClient(value = "foreignExchangeRateService",
        url = "${exchangeratesapi.url:https://api.exchangeratesapi.io}",
        path = "history")
public interface ForeignExchangeRateService {

    @GetMapping
    DateCurrencyMapping getHistory(@SpringQueryMap QueryParams params);

    @Data
    @Builder
    class QueryParams {
        private LocalDate start_at;
        private LocalDate end_at;
        private String base;
        private String symbols;
    }
}
