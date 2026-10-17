package com.gorillaescape.backend.http;

import java.net.URI;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class HttpConfiguration {
    @Bean
    org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer strictJsonTypes() {
        return builder -> builder.withCoercionConfig(tools.jackson.databind.type.LogicalType.Textual, config -> {
            config.setCoercion(tools.jackson.databind.cfg.CoercionInputShape.Integer, tools.jackson.databind.cfg.CoercionAction.Fail);
            config.setCoercion(tools.jackson.databind.cfg.CoercionInputShape.Float, tools.jackson.databind.cfg.CoercionAction.Fail);
            config.setCoercion(tools.jackson.databind.cfg.CoercionInputShape.Boolean, tools.jackson.databind.cfg.CoercionAction.Fail);
        });
    }

    @Bean
    org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer boundedJson() {
        return builder -> builder.enable(tools.jackson.core.StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                .streamReadConstraints(tools.jackson.core.StreamReadConstraints.builder()
                        .maxNestingDepth(8).maxStringLength(256).maxDocumentLength(4096).build());
    }

    @Bean
    WebMvcConfigurer cors(@Value("${gorilla.http.allowed-origins:}") String configured) {
        String[] origins = Arrays.stream(configured.split(",")).map(String::strip).filter(s -> !s.isEmpty()).toArray(String[]::new);
        for (String origin : origins) {
            URI uri = URI.create(origin);
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) || uri.getHost() == null
                    || origin.contains("*") || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || (uri.getPath() != null && !uri.getPath().isEmpty())) {
                throw new IllegalArgumentException("ALLOWED_ORIGIN_MUST_BE_EXACT_HTTP_ORIGIN");
            }
        }
        return new WebMvcConfigurer() {
            @Override public void addCorsMappings(CorsRegistry registry) {
                if (origins.length > 0) registry.addMapping("/api/players").allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "OPTIONS").allowedHeaders("Content-Type")
                        .exposedHeaders("X-Request-Id", "Server-Timing").maxAge(600);
            }
        };
    }
}
