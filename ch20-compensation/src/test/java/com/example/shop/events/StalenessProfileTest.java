package com.example.shop.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

/** staleness プロファイルが、本に載せた 2 つの設定を持つことを確かめる。 */
class StalenessProfileTest {

    @Test
    @DisplayName("application-staleness.yml は配り直しと見張りを有効にする")
    void enablesRepublishAndStaleness() throws IOException {
        List<PropertySource<?>> sources =
                new YamlPropertySourceLoader().load(
                        "staleness",
                        new ClassPathResource(
                                "application-staleness.yml"));
        PropertySource<?> yaml = sources.get(0);

        assertThat(yaml.getProperty(
                "spring.modulith.events.republish-"
                        + "outstanding-events-on-restart"))
                .isEqualTo(true);
        assertThat(yaml.getProperty(
                "spring.modulith.events.staleness.published"))
                .isEqualTo("1m");
        assertThat(yaml.getProperty(
                "spring.modulith.events.staleness."
                        + "check-interval"))
                .isEqualTo("1s");
    }
}
