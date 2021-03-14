package com.challenge.forexrate.controller;

import com.challenge.forexrate.entity.ApiUsageLog;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "exchangeratesapi.url=http://localhost:${wiremock.server.port}")
class ExchangeRateApiLogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnLogsForDailyUsageReport() throws Exception {
        //Given
        LocalDate today = LocalDate.now();

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
                .andExpect(status().isOk());

        //When
        this.mockMvc.perform(get("/api/exchange-rate/history/daily/{date}", today.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))))
                .andDo(print())

                //Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].attributes.requestedDate", Is.is("2020-02-05")))
                .andExpect(jsonPath("data[0].attributes.baseCurrency", Is.is("USD")))
                .andExpect(jsonPath("data[0].attributes.targetCurrency", Is.is("BRL")));
    }

    @Test
    void shouldReturnBadRequestOnInvalidDateAtDailyHistory() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"400\",\"title\":\"Invalid date\",\"detail\":\"Invalid date 'FEBRUARY 31'\"}]}";

        //When
        this.mockMvc.perform(get("/api/exchange-rate/history/daily/2020/02/31"))
                .andDo(print())

                //Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

    @Test
    void shouldReturnLogsForMonthlyUsageReport() throws Exception {
        //Given
        LocalDate today = LocalDate.now();

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
                .andExpect(status().isOk());

        //When
        this.mockMvc.perform(get("/api/exchange-rate/history/monthly/{date}", today.format(DateTimeFormatter.ofPattern("yyyy/MM"))))
                .andDo(print())

                //Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("data[0].attributes.requestedDate", Is.is("2020-02-05")))
                .andExpect(jsonPath("data[0].attributes.baseCurrency", Is.is("USD")))
                .andExpect(jsonPath("data[0].attributes.targetCurrency", Is.is("BRL")));
    }

    @Test
    void shouldReturnBadRequestOnInvalidDateAtMonthlyHistory() throws Exception {
        //Given
        String expectedResponse = "{\"errors\":[{\"status\":\"400\",\"title\":\"Invalid date\",\"detail\":\"Invalid value for MonthOfYear (valid values 1 - 12): 13\"}]}";

        //When
        this.mockMvc.perform(get("/api/exchange-rate/history/monthly/2020/13"))
                .andDo(print())

                //Then
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedResponse));
    }

}