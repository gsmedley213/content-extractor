package io.github.gsmedley213.contentextractor.job;

import io.github.gsmedley213.contentextractor.service.DevelopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RunLocal implements CommandLineRunner {

    @Autowired
    DevelopService developService;

    @Value("${directory:}")
    private String directory;

    @Value("${run:-1}")
    private int run;

    @Override
    public void run(String... args) throws Exception {
        if (directory == null || directory.isBlank()) {
            throw new IllegalArgumentException("Missing required argument: --directory");
        }

        if (run == -1) {
            run = developService.findRun(directory);
        }

        developService.runExtraction(directory, run);
    }
}
