package io.exchangeratesapi.api.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateCurrencyMapping {
    private Map<String, Map<String, Double>> rates;
}
