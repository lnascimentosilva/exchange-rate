package com.challenge.forexrate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeRateReport {

    private final String id;
    private final Double rate;
    private final Double averageRate;
    private final String trend;
}
