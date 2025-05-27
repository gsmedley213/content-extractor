package io.github.gsmedley213.contentextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.gsmedley213.contentextractor.model.AnnotationJob;
import io.github.gsmedley213.contentextractor.model.Notable;
import io.github.gsmedley213.contentextractor.toucher.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.gsmedley213.contentextractor.strategy.ContentExtractor.walkTree;

public interface DevelopService {

    void interrogateBooks();

    void modifyBook(Book book);

    void runExtraction(Book book);

    @Slf4j
    @Service
    class DevelopServiceImpl implements DevelopService {

        @Autowired
        ObjectMapper mapper;

        @Autowired
        ExtractorService extractorService;

        @Override
        @SneakyThrows
        public void interrogateBooks() {
            for (Book book : Book.values()) {
                Document doc = readBook(book);

                // TODO Code for answering questions about books
            }
        }

        @Override
        @SneakyThrows
        public void modifyBook(Book book) {
            Document doc = readBook(book);
            log.info("Title: {}", doc.title());

            var limit = new LimitByStartEnd();
            var elements = new Elements();
            var textNodes = new TextNodes();
            var unused = new Unused();
            var users = Arrays.asList(limit, elements, textNodes, unused);
            walkTree(doc, users);

            for (Element element : elements.getElements()) {
                if ("p".equals(element.tagName())) {
                    element.attr("annotation-verbosify-notification", "paragraph");
                }
                Element e2 = element.clone();
                e2.attr("another-attribute", "yay");
            }

            Files.writeString(Paths.get("..", "shared", "books", book.getDirectory(), "modified.html"),
                    doc.outerHtml(), StandardCharsets.UTF_8);

            Document d2 = doc.clone();
            List<Element> pageNums = d2.getElementsByClass("pagenum");
            pageNums.forEach(Node::remove);
            Files.writeString(Paths.get("..", "shared", "books", book.getDirectory(), "removed_pagenums.html"),
                    d2.outerHtml(), StandardCharsets.UTF_8);
        }

        @Override
        @SneakyThrows
        public void runExtraction(Book book) {
            Document doc = readBook(book);
            List<Notable> notables = extractorService.extractAndMark(doc);

            String bookPrefix = book.getPgId().split("-")[0];
            int bookRun = findRun(Paths.get("..", "shared", "books", book.getDirectory()));
            Files.writeString(Paths.get("..", "shared", "books", book.getDirectory(),
                            String.format("%s-%d-marked.html", bookPrefix, bookRun)),
                    doc.outerHtml(), StandardCharsets.UTF_8);

            AnnotationJob annotationJob = new AnnotationJob(bookPrefix, bookRun, book.getDescription(), notables);
            Files.writeString(Paths.get("..", "shared", "books", book.getDirectory(),
                            String.format("AnnotationJob-%s-%d.json", bookPrefix, bookRun)),
                    mapper.writeValueAsString(annotationJob), StandardCharsets.UTF_8);

            log.info("Finished extraction.");
        }

        private int findRun(Path path) {
            if (path.toFile().isDirectory()) {
                List<String> filenames = Arrays.stream(path.toFile().listFiles())
                        .map(File::getName)
                        .toList();
                int maxRun = 0;
                for (String filename : filenames) {
                    if (filename.contains("-marked-")) {
                        // Expecting format [Project Gutenberg prefix]-[run number]-marked.html
                        int fileRun = Integer.parseInt(filename.split("-")[1]);
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

        private void writeElementsByTag(Book book, Elements elements) throws IOException {
            Map<String, List<String>> stringRepByTag = elements.byTag().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().stream().map(Element::toString).collect(Collectors.toList())
                            ));

            Files.writeString(Paths.get("..", "shared", "books", book.getDirectory(), "elementsByTag.json"),
                    mapper.writeValueAsString(stringRepByTag));
        }

        @SneakyThrows
        private Document readBook(Book book) {
            return Jsoup.parse(book.getPath().toFile());
        }
    }

    @Getter
    enum Book {
        GK_CHESTERTON("gk_chesterton", "pg27080-images.html",
                "\"G. K. Chesterton: A Critical Study\" by Julius West is a critical analysis written in the early 20th century. The book examines the life, work, and influence of British author G. K. Chesterton, highlighting his literary contributions and the impact of his ideas on contemporary thought. The focus is on Chesterton’s unique style, his approach to various literary genres, and his philosophical perspectives, particularly regarding religion and society."),
        GOLDEN_AGE("golden_age", "pg32501-images.html",
                "\"The Golden Age\" by Kenneth Grahame is a novel written in the late 19th century. It captures the nostalgic reflections of childhood, exploring themes of imagination, innocence, and the contrasting perspective of adults through the eyes of children."),
        LIVESTOCK_AND_ARMOUR("livestock_and_armour", "pg51244-images.html",
                "\"The Livestock Producer and Armour\" by Armour and Company is a scientific publication written in the early 20th century. This book discusses the dynamics of the livestock industry, the evolution of meat packing, and the economic relations between producers and packers, reflecting a time when the industry was adjusting to modern practices and market demands post-World War I."),
        NAPOLEON_1("napoleon_1", "pg48837-images.html",
                "\"Life of Napoleon Bonaparte, Volume I\" by Sir Walter Scott is a historical account written in the early 19th century. The narrative provides an in-depth examination of Napoleon's life amidst the backdrop of the French Revolution and the significant political upheavals of the time."),
        REJECTED_MEN("rejected_men", "pg46841-images.html",
                "\"Rejected of Men: A Story of To-day\" by Howard Pyle is a historical fiction novel written in the early 20th century. The narrative re-examines the biblical story of the crucifixion from the perspective of the scribes, Pharisees, priests, and Romans, offering a unique viewpoint that challenges contemporary interpretations of those events.");

        private final Path path;
        private final String directory;
        private final String pgId;
        private final String description; // Should be helpful context for an LLM

        Book(String directory, String pgId, String description) {
            this.directory = directory;
            this.pgId = pgId;
            this.description = description;
            this.path = Paths.get("..", "shared", "books", directory, pgId);
        }
    }
}
