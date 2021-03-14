package com.challenge.forexrate.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ApiUsageLog {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDateTime requestDateTime;
    private LocalDate requestedDate;
    private String baseCurrency;
    private String targetCurrency;

}