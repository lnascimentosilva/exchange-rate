package com.challenge.forexrate.service;

import com.challenge.forexrate.entity.ApiUsageLog;
import com.challenge.forexrate.repository.ApiUsageLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiUsageLogServiceTest {

    @Mock
    private ApiUsageLogRepository apiUsageLogRepository;
    private ApiUsageLogService apiUsageLogService;

    @BeforeEach
    void setUp() {
        apiUsageLogService = new ApiUsageLogService(apiUsageLogRepository);
    }

    @Test
    void shouldSave() {
        //Given
        LocalDate date = LocalDate.of(2020, 1, 1);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";

        ApiUsageLog expectedApiUsageLog = ApiUsageLog.builder()
                .requestDateTime(LocalDateTime.now())
                .requestedDate(date)
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .build();

        //When
        apiUsageLogService.save(date, baseCurrency, targetCurrency);

        //Then
        verify(apiUsageLogRepository).save(expectedApiUsageLog);
    }

    @Test
    void shouldSearchByRangeDate() {
        //Given
        LocalDate date = LocalDate.of(2020, 1, 1);
        String baseCurrency = "USD";
        String targetCurrency = "BRL";
        LocalDateTime startSearchDateTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime endSearchDateTime = LocalDateTime.of(2020, 1, 31, 23, 59, 59, 999999);
        LocalDateTime eventDateTime = LocalDateTime.of(2020, 1, 15, 0, 0);
        ;

        ApiUsageLog expectedLog = ApiUsageLog.builder()
                .requestDateTime(eventDateTime)
                .requestedDate(date)
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .build();

        when(apiUsageLogRepository.findByRequestDateTimeBetween(startSearchDateTime, endSearchDateTime)).thenReturn(Collections.singletonList(expectedLog));

        //When
        List<ApiUsageLog> actualLogs = apiUsageLogService.findByRangeDateTime(startSearchDateTime, endSearchDateTime);

        //Then
        assertThat(actualLogs).isEqualTo(Collections.singletonList(expectedLog));
    }
}