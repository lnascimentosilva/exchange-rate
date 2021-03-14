package com.challenge.forexrate.validator;

import com.challenge.forexrate.exception.ExchangeRateDateOutOfBoundsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ReportRequestDateValidator {

    private LocalDate supportedStartingDate;

    public void validate(LocalDate date) {
        if (date.isBefore(supportedStartingDate)
                || date.isAfter(LocalDate.now().minus(1, ChronoUnit.DAYS))) {
            throw new ExchangeRateDateOutOfBoundsException(String.format("Exchange rate date should be between %s and yesterday", supportedStartingDate));
        }
    }

    @Autowired
    void setSupportedStartingDate(@Value("${report.supported.starting-date:1/1/00}") LocalDate supportedStartingDate) {
        this.supportedStartingDate = supportedStartingDate;
    }
}
