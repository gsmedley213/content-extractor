package io.github.gsmedley213.contentextractor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gsmedley213.contentextractor.config.FileBooks;
import io.github.gsmedley213.contentextractor.model.AnnotationJob;
import io.github.gsmedley213.contentextractor.model.Notable;
import io.github.gsmedley213.contentextractor.service.DevelopService;
import io.github.gsmedley213.contentextractor.service.ExtractorService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DevelopServiceImpl implements DevelopService {

    private final ObjectMapper mapper;
    private final ExtractorService extractorService;
    private final FileBooks fileBooks;

    @Autowired
    public DevelopServiceImpl(ObjectMapper mapper, ExtractorService extractorService, FileBooks fileBooks) {
        this.mapper = mapper;
        this.extractorService = extractorService;
        this.fileBooks = fileBooks;
    }

    @Override
    @SneakyThrows
    public void interrogateBooks() {
        for (FileBooks.FileBook book : fileBooks.getBooks()) {
            Document doc = readBook(book);

            // Used this method when running checks on structure of some example HTML docs from Project Gutenberg.
            // Leaving the unused empty method for now in case I need to do further checks later.
        }
    }

    @Override
    @SneakyThrows // This method is only intended to be run during local development so SneakyThrows is OK.
    public void runExtraction(String directory, int run) {
        FileBooks.FileBook book = fileBooks.getBooks().stream()
                .filter(fb -> fb.directory().equals(directory))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("\"%s\" is not configured as a" +
                        "a possible directory. Please add required configurations under \"files\" in " +
                        "application.yml. See application.example.yml for an example.", directory)));

        Document doc = readBook(book);
        List<Notable> notables = extractorService.extractAndMark(doc);

        Files.writeString(getPath(book.directory(), markedHtml(book.prefix(), run)),
                doc.outerHtml(), StandardCharsets.UTF_8);

        AnnotationJob annotationJob = new AnnotationJob(book.prefix(), run, book.description(), notables);
        Files.writeString(getPath(book.directory(), annotationFile(run, book)),
                mapper.writeValueAsString(annotationJob), StandardCharsets.UTF_8);

        log.info("Finished extraction.");
    }

    private static String markedHtml(String prefix, int run) {
        return String.format("%s-%d-marked.html", prefix, run);
    }

    private static String annotationFile(int run, FileBooks.FileBook book) {
        return String.format("AnnotationJob-%s-%d.json", book.prefix(), run);
    }

    public int findRun(String directory) {
        Path path = Paths.get(fileBooks.getShared(), directory);
        // Expecting format [Project Gutenberg prefix]-[run number]-marked.html
        Pattern markedPatter = Pattern.compile("^[^-]+-(\\d+)-marked\\.html$");
        if (path.toFile().isDirectory()) {
            List<String> filenames = Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
                    .map(File::getName)
                    .toList();
            int maxRun = 0;
            for (String filename : filenames) {
                Matcher m = markedPatter.matcher(filename);
                if (m.matches()) {
                    int fileRun = Integer.parseInt(m.group(1));
                    if (fileRun > maxRun) {
                        maxRun = fileRun;
                    }
                }
            }
            return maxRun + 1;
        } else {
            throw new IllegalArgumentException(String.format("'%s' is not a directory.", path));
        }
    }

    @SneakyThrows
    private Document readBook(FileBooks.FileBook book) {
        return Jsoup.parse(getPath(book.directory(), originalHtml(book.prefix())).toFile());
    }

    private Path getPath(String directory, String filename) {
        return Paths.get(fileBooks.getShared(), directory, filename);
    }

    private static String originalHtml(String prefix) {
        return prefix + "-images.html";
    }
}
