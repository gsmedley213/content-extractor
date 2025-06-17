package io.github.gsmedley213.contentextractor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("file")
@Data
public class FileBooks {
    private String shared;
    private List<FileBook> books;

    public record FileBook(String directory, String prefix, String description) {
    }
}
