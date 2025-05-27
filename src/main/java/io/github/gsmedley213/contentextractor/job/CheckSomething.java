package io.github.gsmedley213.contentextractor.job;

import io.github.gsmedley213.contentextractor.service.DevelopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static io.github.gsmedley213.contentextractor.service.DevelopService.Book;

@Slf4j
@Component
public class CheckSomething implements CommandLineRunner {

    @Autowired
    DevelopService developService;

    @Override
    public void run(String... args) throws Exception {
        developService.runExtraction(Book.LIVESTOCK_AND_ARMOUR);
    }
}
