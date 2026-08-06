package com.example.shop.order.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.order.application.PlaceOrderCommand;
import com.example.shop.order.application.PlaceOrderService;
import com.example.shop.order.domain.OrderId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private PlaceOrderService placeOrderService;

    @Test
    @DisplayName("注文を受け付けると 201 と識別子を返す")
    void returnsCreated() throws Exception {
        given(placeOrderService.place(any(PlaceOrderCommand.class)))
                .willReturn(new OrderId("O-001"));

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Version", "1")
                        .content(
                                """
                                {"customerId":"C-001",
                                 "lines":[{"productId":"P-001","quantity":2,"unitPriceYen":1500}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value("O-001"));
    }

    @Test
    @DisplayName("規則に反する注文は 422 で返す")
    void returnsUnprocessableEntity() throws Exception {
        willThrow(new IllegalStateException("1 注文の上限を超えます: 800000 > 500000"))
                .given(placeOrderService)
                .place(any(PlaceOrderCommand.class));

        mvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Version", "1")
                        .content(
                                """
                                {"customerId":"C-001",
                                 "lines":[{"productId":"P-001","quantity":1,"unitPriceYen":800000}]}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("1 注文の上限を超えます: 800000 > 500000"));
    }
}
