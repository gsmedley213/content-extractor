package io.github.gsmedley213.contentextractor.job;

import io.github.gsmedley213.contentextractor.service.DevelopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class RunLocal implements CommandLineRunner {

    @Autowired
    DevelopService developService;

    @Override
    public void run(String... args) throws Exception {
        Optional<String> directory = extractArg(args, "directory");
        if (directory.isEmpty()) {
            log.error("Argument \"directory:[directory_name]\" required.");
            return;
        }

        Optional<String> runArg = extractArg(args, "run");
        int run = runArg
                .map(Integer::parseInt)
                .orElseGet(() -> developService.findRun(directory.get()));

        developService.runExtraction(directory.get(), run);
    }

    private Optional<String> extractArg(String[] args, String argName) {
        return Arrays.stream(args)
                .filter(arg -> arg.startsWith(argName + ":"))
                .map(arg -> arg.split(":")[1])
                .findFirst();
    }
}
