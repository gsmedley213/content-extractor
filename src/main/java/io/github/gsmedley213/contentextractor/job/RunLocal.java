package io.github.gsmedley213.contentextractor.job;

import io.github.gsmedley213.contentextractor.service.DevelopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "runLocal")
public class RunLocal implements CommandLineRunner {

    final DevelopService developService;

    @Value("${directory:}")
    private String directory;

    @Value("${run:-1}")
    private int run;

    public RunLocal(DevelopService developService) {
        this.developService = developService;
    }

    @Override
    public void run(String... stringArgs) throws Exception {
        if (directory == null || directory.isBlank()) {
            throw new IllegalArgumentException("Missing required argument: --directory");
        }

        if (run == -1) {
            run = developService.findRun(directory);
        }

        developService.runExtraction(directory, run);
    }
}
