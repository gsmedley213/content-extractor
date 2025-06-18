package io.github.gsmedley213.contentextractor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gsmedley213.contentextractor.strategy.BottomDivP;
import io.github.gsmedley213.contentextractor.strategy.ContentExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentExtractorConfig {

    @Bean
    ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    ContentExtractor contentExtractor() {
        return new BottomDivP();
    }
}
