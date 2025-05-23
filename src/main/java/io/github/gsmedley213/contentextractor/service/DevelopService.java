package io.github.gsmedley213.contentextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.gsmedley213.contentextractor.toucher.Elements;
import io.github.gsmedley213.contentextractor.toucher.LimitByStartEnd;
import io.github.gsmedley213.contentextractor.toucher.NodeToucher;
import io.github.gsmedley213.contentextractor.toucher.Unused;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface DevelopService {

    void interogateBooks();

    @Slf4j
    @Service
    class DevelopServiceImpl implements DevelopService {

        @Autowired
        ObjectMapper mapper;

        @Override
        @SneakyThrows
        public void interogateBooks() {
            for (Book book : Book.values()) {
                Document doc = readBook(book);
                log.info("Title: {}", doc.title());

                var limit = new LimitByStartEnd();
                var elements = new Elements();
                var unused = new Unused();
                var users = Arrays.asList(limit, elements, unused);
                walkTree(doc, users);

                Map<String, List<String>> stringRepByTag = elements.byTag().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> entry.getValue().stream().map(Element::toString).collect(Collectors.toList())
                                ));

                Files.writeString(Paths.get("local", "books", book.getDirectory(), "elementsByTag.json"),
                        mapper.writeValueAsString(stringRepByTag));
            }
        }

        private void walkTree(Node node, List<NodeToucher> users) {
            for (NodeToucher check : users) {
                if (check.touch(node)) {
                    break;
                }
            }
            for (Node child : node.childNodes()) {
                walkTree(child, users);
            }
        }

        @SneakyThrows
        private Document readBook(Book book) {
            return Jsoup.parse(book.getPath().toFile());
        }

        @Getter
        enum Book {
            GK_CHESTERTON("gk_chesterton", "pg27080-images.html"),
            GOLDEN_AGE("golden_age", "pg32501-images.html"),
            LIVESTOCK_AND_ARMOUR("livestock_and_armour", "pg51244-images.html"),
            NAPOLEON_1("napoleon_1", "pg48837-images.html"),
            REJECTED_MEN("rejected_men", "pg46841-images.html");

            private final Path path;
            private final String directory;
            private final String pgId;

            Book(String directory, String pgId) {
                this.directory = directory;
                this.pgId = pgId;
                this.path = Paths.get("local", "books", directory, pgId);
            }
        }
    }
}
