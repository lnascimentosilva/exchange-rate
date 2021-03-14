package com.challenge.forexrate.controller;

import com.challenge.forexrate.entity.ApiUsageLog;
import com.challenge.forexrate.repository.ApiUsageLogRepository;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "exchangeratesapi.url=http://localhost:${wiremock.server.port}")
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiUsageLogRepository apiUsageLogRepository;

    @Test
    void shouldReturnReport() throws Exception {
        //Given
        String expectedResponse = "{\"data\":{\"id\":\"USD_BRL_2020-02-05\",\"type\":\"exchangeratereports\",\"attributes\":{\"rate\":4.2287943391,\"averageRate\":4.250857431175,\"trend\":\"undefined\"}}}";

        ApiUsageLog expectedLogs = ApiUsageLog.builder()
                .baseCurrency("USD")
                .targetCurrency("BRL")
                .requestedDate(LocalDate.of(2020, 2, 5))
                .build();

        stubFor(WireMock.get(urlPathEqualTo("/history"))
                .withQueryParam("start_at", equalTo("2020-01-29"))
                .withQueryParam("end_at", equalTo("2020-02-05"))
                .withQueryParam("base", equalTo("USD"))
                .withQueryParam("symbols", equalTo("BRL"))
                .willReturn(okJson("{\"rates\":{\"2020-02-03\":{\"BRL\":4.2626061811},\"2020-02-05\":{\"BRL\":4.2287943391},\"2020-01-31\":{\"BRL\":4.2668295331},\"2020-01-30\":{\"BRL\":4.2466225406},\"2020-02-04\":{\"BRL\":4.2273714699}},\"start_at\":\"2020-01-30\",\"base\":\"USD\",\"end_at\":\"2020-02-05\"}")));

        //When
        this.mockMvc.perform(get("/api/exchange-rate/2020-02-05/USD/BRL"))
                .andDo(print())

                //Then
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponse));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = LocalDateTime.of(today, LocalTime.of(0, 0));
        LocalDateTime endOfToday = LocalDateTime.of(today, LocalTime.of(23, 59, 59, 999999));
        List<ApiUsageLog> actualLogs = apiUsageLogRepository.findByRequestDateTimeBetween(startOfToday, endOfToday);

        assertThat(actualLogs).usingElementComparatorIgnoringFields("id", "requestDateTime").containsExactlyInAnyOrder(expectedLogs);

    }

    @Test
    void shouldReturnBadRequestOnInvalidDate() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"400\",\"title\":\"Invalid date\",\"detail\":\"Text '2020-02-31' could not be parsed: Invalid date 'FEBRUARY 31'\"}]}";

        //When
        this.mockMvc.perform(get("/api/exchange-rate/2020-02-31/USD/BRL"))
                .andDo(print())

                //Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldReturnBadRequestOnDateOutOfBounds() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"400\",\"title\":\"Date out of bounds\",\"detail\":\"Exchange rate date should be between 2000-01-01 and yesterday\"}]}";

        //When
        this.mockMvc.perform(get("/api/exchange-rate/1999-12-31/USD/BRL"))
                .andDo(print())

                //Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldReturnNotFoundOnExchangeRateDateNotFound() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"404\",\"title\":\"Exchange rate not found\",\"detail\":\"Requested exchange rate date could not be found\"}]}";

        stubFor(WireMock.get(urlPathEqualTo("/history"))
                .withQueryParam("start_at", equalTo("2009-12-25"))
                .withQueryParam("end_at", equalTo("2010-01-01"))
                .withQueryParam("base", equalTo("USD"))
                .withQueryParam("symbols", equalTo("BRL"))
                .willReturn(okJson("{\"rates\":{\"2009-12-28\":{\"BRL\":1.735091982},\"2009-12-30\":{\"BRL\":1.7390152044},\"2009-12-31\":{\"BRL\":1.7432319867},\"2009-12-29\":{\"BRL\":1.7344973325}},\"start_at\":\"2009-12-25\",\"base\":\"USD\",\"end_at\":\"2010-01-01\"}")));

        //When
        this.mockMvc.perform(get("/api/exchange-rate/2010-01-01/USD/BRL"))
                .andDo(print())

                //Then
                .andExpect(status().isNotFound())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldReturnBadRequestOnInvalidBaseCurrency() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"400\",\"title\":\"Not supported currency\",\"detail\":\"Used currency 'USDA' is not supported\"}]}";

        stubFor(WireMock.get(urlPathEqualTo("/history"))
                .withQueryParam("start_at", equalTo("2009-12-25"))
                .withQueryParam("end_at", equalTo("2010-01-01"))
                .withQueryParam("base", equalTo("USDA"))
                .withQueryParam("symbols", equalTo("BRL"))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Base 'USDA' is not supported.\"}")));

        //When
        this.mockMvc.perform(get("/api/exchange-rate/2010-01-01/USDA/BRL"))
                .andDo(print())

                //Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldReturnBadRequestOnInvalidTargetCurrency() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"400\",\"title\":\"Not supported currency\",\"detail\":\"Used currency 'BRLA' is not supported\"}]}";

        stubFor(WireMock.get(urlPathEqualTo("/history"))
                .withQueryParam("start_at", equalTo("2009-12-25"))
                .withQueryParam("end_at", equalTo("2010-01-01"))
                .withQueryParam("base", equalTo("USD"))
                .withQueryParam("symbols", equalTo("BRLA"))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Symbols 'BRLA' are invalid.\"}")));

        //When
        this.mockMvc.perform(get("/api/exchange-rate/2010-01-01/USD/BRLA"))
                .andDo(print())

                //Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldReturnInternalServerErrorOnExternalServiceFailure() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"500\",\"title\":\"Unknown error\",\"detail\":\"Error occurred while fetching data from exchangeratesapi.io\"}]}";

        stubFor(WireMock.get(urlPathEqualTo("/history"))
                .withQueryParam("start_at", equalTo("2009-12-25"))
                .withQueryParam("end_at", equalTo("2010-01-01"))
                .withQueryParam("base", equalTo("USD"))
                .withQueryParam("symbols", equalTo("BRLA"))
                .willReturn(badRequest()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Unexpected error\"}")));

        //When
        this.mockMvc.perform(get("/api/exchange-rate/2010-01-01/USD/BRLA"))
                .andDo(print())

                //Then
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(expectedResponse));
    }

}