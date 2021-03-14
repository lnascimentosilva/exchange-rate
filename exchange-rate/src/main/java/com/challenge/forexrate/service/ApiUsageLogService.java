package com.challenge.forexrate.service;

import com.challenge.forexrate.entity.ApiUsageLog;
import com.challenge.forexrate.repository.ApiUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiUsageLogService {

    private final ApiUsageLogRepository apiUsageLogRepository;

    @Async
    public void save(LocalDate date, String baseCurrency, String targetCurrency) {
        apiUsageLogRepository.save(ApiUsageLog.builder()
                .requestDateTime(LocalDateTime.now())
                .requestedDate(date)
                .baseCurrency(baseCurrency)
                .targetCurrency(targetCurrency)
                .build());
    }

    public List<ApiUsageLog> findByRangeDateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return apiUsageLogRepository.findByRequestDateTimeBetween(startDateTime, endDateTime);
    }
}
