package com.example.shop.order.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.example.shop.order.application.PlaceOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 版を増やしたときに、同じ URL でも版ごとに別の処理へ振り分けられることを確かめる。 */
@WebMvcTest
@Import(ApiVersionRoutingProbeTest.VersionedController.class)
class ApiVersionRoutingProbeTest {

    @Autowired private MockMvc mvc;

    @MockitoBean private PlaceOrderService placeOrderService;

    @Test
    @DisplayName("同じ URL でも、版ごとに別の処理が呼ばれる")
    void routesByVersion() throws Exception {
        assertThat(mvc.perform(get("/probe").header("X-API-Version", "1"))
                        .andReturn().getResponse().getContentAsString())
                .isEqualTo("v1");
        assertThat(mvc.perform(get("/probe").header("X-API-Version", "2"))
                        .andReturn().getResponse().getContentAsString())
                .isEqualTo("v2");
    }

    @RestController
    @RequestMapping("/probe")
    static class VersionedController {

        @GetMapping(version = "1")
        String v1() {
            return "v1";
        }

        @GetMapping(version = "2")
        String v2() {
            return "v2";
        }
    }
}
