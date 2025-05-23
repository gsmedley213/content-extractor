package io.github.gsmedley213.contentextractor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;

public class ContentExtractorConfig {

    @Bean
    ObjectMapper mapper() {
        return new ObjectMapper();
    }
}
