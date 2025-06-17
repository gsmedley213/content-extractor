package io.github.gsmedley213.contentextractor.service;

public interface DevelopService {

    void interrogateBooks();

    void runExtraction(String directory, int run);

    int findRun(String directory);

}
