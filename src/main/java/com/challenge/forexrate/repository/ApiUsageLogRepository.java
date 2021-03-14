package com.challenge.forexrate.repository;

import com.challenge.forexrate.entity.ApiUsageLog;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ApiUsageLogRepository extends CrudRepository<ApiUsageLog, Integer> {

    List<ApiUsageLog> findByRequestDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}
