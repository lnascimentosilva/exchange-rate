package com.challenge.forexrate.validator;

import com.challenge.forexrate.exception.ExchangeRateDateOutOfBoundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ReportRequestDateValidatorTest {

    private ReportRequestDateValidator reportRequestDateValidator;
    private LocalDate supportedStartingDate;

    @BeforeEach
    void setUp() {
        reportRequestDateValidator = new ReportRequestDateValidator();
        supportedStartingDate = LocalDate.of(2020, 1, 1);
        reportRequestDateValidator.setSupportedStartingDate(supportedStartingDate);
    }

    @Test
    void shouldExceedLowerDateThreshold() {
        //Given
        LocalDate date = LocalDate.of(1999, 12, 31);

        //When
        Throwable throwable = catchThrowable(() -> reportRequestDateValidator.validate(date));

        //Then
        assertThat(throwable)
                .isInstanceOf(ExchangeRateDateOutOfBoundsException.class)
                .hasMessage(String.format("Exchange rate date should be between %s and yesterday", supportedStartingDate));
    }

    @Test
    void shouldExceedUpperDateThreshold() {
        //Given
        LocalDate date = LocalDate.now();

        //When
        Throwable throwable = catchThrowable(() -> reportRequestDateValidator.validate(date));

        //Then
        assertThat(throwable)
                .isInstanceOf(ExchangeRateDateOutOfBoundsException.class)
                .hasMessage(String.format("Exchange rate date should be between %s and yesterday", supportedStartingDate));
    }

    @Test
    void shouldAcceptValidDate() {
        //Given
        LocalDate date = LocalDate.now().minus(1, ChronoUnit.DAYS);

        //When
        Throwable throwable = catchThrowable(() -> reportRequestDateValidator.validate(date));

        //Then
        assertThat(throwable)
                .isNull();
    }
}