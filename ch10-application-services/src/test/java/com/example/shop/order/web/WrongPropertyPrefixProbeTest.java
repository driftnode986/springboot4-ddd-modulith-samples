// #region test-imports
package com.example.shop.order.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shop.order.application.PlaceOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.mockito.Mockito;
// #endregion

// #region test-head
class WrongPropertyPrefixProbeTest {

    private final WebApplicationContextRunner runner =
            new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                WebMvcAutoConfiguration.class,
                HttpMessageConvertersAutoConfiguration.class,
                JacksonAutoConfiguration.class))
            .withUserConfiguration(
                ControllerConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    static class ControllerConfiguration {

        @Bean
        OrderController orderController() {
            return new OrderController(
                    Mockito.mock(PlaceOrderService.class));
        }
    }
// #endregion

    // #region wrong-prefix
    @Test
    @DisplayName("前置きを間違えると、"
            + "版の解決が組み立てられず起動に失敗する")
    void wrongPrefixFailsToStart() {
        String expected = "API version specified, "
                + "but no ApiVersionStrategy configured";
        runner.withPropertyValues(
                        "spring.webflux.apiversion.use.header="
                                + "X-API-Version",
                        "spring.webflux.apiversion.required=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining(expected);
                });
    }
    // #endregion

    @Test
    @DisplayName("正しい前置きなら版の解決が組み立てられる")
    void correctPrefixBuildsStrategy() {
        runner.withPropertyValues(
                        "spring.mvc.apiversion.use.header=X-API-Version",
                        "spring.mvc.apiversion.required=true")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    @DisplayName("版の設定を一部だけ書くと、取り出す手段がないため起動に失敗する")
    void partialConfigurationFailsToStart() {
        runner.withPropertyValues("spring.mvc.apiversion.required=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining(
                                    "API version config customized, but no ApiVersionResolver provided");
                });
    }

    @Test
    @DisplayName("版の設定を書かなければ、版を付けた対応づけ自体が成立しない")
    void noConfigurationRejectsVersionedMapping() {
        runner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                    .hasStackTraceContaining(
                            "API version specified, but no ApiVersionStrategy configured");
        });
    }
}
