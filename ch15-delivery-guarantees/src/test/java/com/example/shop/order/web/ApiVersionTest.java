package com.example.shop.order.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.application.PlaceOrderService;
import com.example.shop.order.domain.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class ApiVersionTest {

    private static final String BODY =
            """
            {"customerId":"C-001",
             "lines":[{"productId":"P-001","quantity":2,"unitPriceYen":1500}]}
            """;

    @Autowired private MockMvc mvc;

    @MockitoBean private PlaceOrderService placeOrderService;

    @BeforeEach
    void setUp() {
        given(placeOrderService.place(any(PlaceOrderCommand.class)))
                .willReturn(new OrderId("O-001"));
    }

    @Test
    @DisplayName("版を載せない要求は 400 になる")
    void missingVersion() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage())
                        .isEqualTo("API version is required."));
    }

    @Test
    @DisplayName("対応していない版は 400 になり、版は意味的な形に直して報告される")
    void invalidVersion() throws Exception {
        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Version", "9")
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage())
                        .isEqualTo("Invalid API version: '9.0.0'."));
    }

    @Test
    @DisplayName("版に関する 400 には本文が付かない")
    void errorHasNoBody() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEmpty());
    }
}
