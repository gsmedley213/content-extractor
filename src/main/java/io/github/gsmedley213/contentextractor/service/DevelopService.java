package io.github.gsmedley213.contentextractor.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.gsmedley213.contentextractor.toucher.Elements;
import io.github.gsmedley213.contentextractor.toucher.LimitByStartEnd;
import io.github.gsmedley213.contentextractor.toucher.NodeToucher;
import io.github.gsmedley213.contentextractor.toucher.Unused;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public interface DevelopService {

    void interrogateBooks();

    void modifyBook(Book book);

    @Slf4j
    @Service
    class DevelopServiceImpl implements DevelopService {

        @Autowired
        ObjectMapper mapper;

        @Override
        @SneakyThrows
        public void interrogateBooks() {
            for (Book book : Book.values()) {
                Document doc = readBook(book);
                log.info("Title: {}", doc.title());

                var limit = new LimitByStartEnd();
                var elements = new Elements();
                var textNodes = new TextNodes();
                var unused = new Unused();
                var users = Arrays.asList(limit, elements, textNodes, unused);
                walkTree(doc, users);

                var consideredElements = Set.of("p", "div");

                List<Element> nested = elements.getElements().stream()
                        .filter(e -> consideredElements.contains(e.tagName()))
                        .filter(e -> hasChildWithTag(e, consideredElements))
                        .toList();

                List<Element> limited = elements.getElements().stream()
                        .filter(e -> consideredElements.contains(e.tagName()))
                        .filter(e -> !hasChildWithTag(e, consideredElements))
                        .toList();

                TextNodes limitedText = new TextNodes();
                limited.forEach(e -> walkTree(e, List.of(limitedText)));

                Set<TextNode> limitedNodes = new HashSet<>(limitedText.textNodes);
                List<TextNode> missedNodes =
                        textNodes.textNodes.stream().filter(tn -> !limitedNodes.contains(tn)).toList();

                if (!nested.isEmpty()) {
                    log.info("Nested div and p elements exits in {}", book);
                }
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

            Files.writeString(Paths.get("local", "books", book.getDirectory(), "modified.html"),
                    doc.outerHtml(), StandardCharsets.UTF_8);

            Document d2 = doc.clone();
            List<Element> pageNums = d2.getElementsByClass("pagenum");
            pageNums.forEach(Node::remove);
            Files.writeString(Paths.get("local", "books", book.getDirectory(), "removed_pagenums.html"),
                    d2.outerHtml(), StandardCharsets.UTF_8);
        }

        private static boolean hasChildWithTag(Element element, Set<String> tags) {
            return element.getAllElements().stream()
                    .filter(e -> e != element)
                    .anyMatch(e -> tags.contains(e.tagName()));
        }

        @Getter
        private static class TextNodes implements NodeToucher {
            List<TextNode> textNodes = new ArrayList<>();

            @Override
            public boolean touch(Node n) {
                if (n instanceof TextNode tn) {
                    textNodes.add(tn);
                    return true;
                }
                return false;
            }
        }

        private void writeElementsByTag(Book book, Elements elements) throws IOException {
            Map<String, List<String>> stringRepByTag = elements.byTag().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> entry.getValue().stream().map(Element::toString).collect(Collectors.toList())
                            ));

            Files.writeString(Paths.get("local", "books", book.getDirectory(), "elementsByTag.json"),
                    mapper.writeValueAsString(stringRepByTag));
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
