package io.github.gsmedley213.contentextractor.job;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

@Slf4j
@Component
public class CheckSomething implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Document doc = readBook();
        log.info("Title: " + doc.title());
        walkTree(doc, n -> {
            if (n instanceof Element) {
                log.info(((Element)n).tagName());
            } else if (n instanceof TextNode) {
                TextNode tn = ((TextNode)n);
                log.info("length: {}, start: {}", tn.text().length(),
                        tn.text().substring(0, Math.min(50, tn.text().length())));
            }
        });
    }

    private void walkTree(Node node, Consumer<Node> doSomething) {
        doSomething.accept(node);
        for (Node child : node.childNodes()) {
            walkTree(child, doSomething);
        }
    }

    @SneakyThrows
    private Document readBook() {
        return Jsoup.parse(Book.REJECTED_MEN.path().toFile());
    }

    enum Book {
        GK_CHESTERTON("books", "gk_chesterton", "pg27080-images.html"),
        GOLDEN_AGE("books", "golden_age", "pg32501-images.html"),
        LIVESTOCK_AND_ARMOUR("books", "livestock_and_armour", "pg51244-images.html"),
        NAPOLEON_1("books", "napoleon_1", "pg48837-images.html"),
        REJECTED_MEN("books", "rejected_men", "pg46841-images.html");

        private final Path path;

        Book(String... path) {
            this.path = Paths.get("local", path);
        }

        Path path() {
            return path;
        }
    }
}
