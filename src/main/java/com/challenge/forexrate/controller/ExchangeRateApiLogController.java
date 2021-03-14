package com.challenge.forexrate.controller;

import com.challenge.forexrate.entity.ApiUsageLog;
import com.challenge.forexrate.service.ApiUsageLogService;
import com.toedter.spring.hateoas.jsonapi.MediaTypes;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping(value = "/api/exchange-rate/history", produces = MediaTypes.JSON_API_VALUE)
@RequiredArgsConstructor
public class ExchangeRateApiLogController {

    private static final LocalTime BOD_LOCAL_TIME = LocalTime.of(0, 0);
    private static final LocalTime EOD_LOCAL_TIME = LocalTime.of(23, 59, 59, 999999);
    private final ApiUsageLogService apiUsageLogService;

    @GetMapping("/daily/{year:^\\d{4}$}/{month:^\\d{2}$}/{day:^\\d{2}$}")
    public CollectionModel<ApiUsageLog> getLogs(@PathVariable Integer year,
                                                @PathVariable Integer month,
                                                @PathVariable Integer day) {
        LocalDate date = LocalDate.of(year, month, day);
        LocalDateTime startDateTime = LocalDateTime.of(date, BOD_LOCAL_TIME);
        LocalDateTime endDateTime = LocalDateTime.of(date, EOD_LOCAL_TIME);
        return CollectionModel.of(apiUsageLogService.findByRangeDateTime(startDateTime, endDateTime));
    }

    @GetMapping("/monthly/{year:^\\d{4}$}/{month:^\\d{2}$}")
    public CollectionModel<ApiUsageLog> getLogs(@PathVariable Integer year,
                                                @PathVariable Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, startDate.lengthOfMonth());
        LocalDateTime startDateTime = LocalDateTime.of(startDate, BOD_LOCAL_TIME);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, EOD_LOCAL_TIME);
        return CollectionModel.of(apiUsageLogService.findByRangeDateTime(startDateTime, endDateTime));
    }
}
