package io.github.gsmedley213.contentextractor.job;

import lombok.Getter;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CheckSomething implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        Document doc = readBook(Book.LIVESTOCK_AND_ARMOUR);
        log.info("Title: {}", doc.title());

        var limit = new LimitByStartEnd();
        var elements = new Elements();
        var unused = new Unused();
        var users = Arrays.asList(limit, elements, unused);
        walkTree(doc, users);
        log.info("Node counts: Unused - {}, before start - {}, after end - {}", unused.getUnused().size(),
                limit.getBeforeStart().size(), limit.getAfterEnd().size());
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
        return Jsoup.parse(book.path().toFile());
    }

    interface NodeToucher {
        boolean touch(Node n);
    }

    @Getter
    static class Elements implements NodeToucher {

        private final List<Element> elements = new ArrayList<>();

        @Override
        public boolean touch(Node n) {
            if (n instanceof Element e) {
                elements.add(e);
                return true;
            }
            return false;
        }

        public Map<String, List<Element>> byTag() {
            return elements.stream()
                    .collect(Collectors.groupingBy(Element::tagName));
        }

    }

    static class LimitByStartEnd implements NodeToucher {
        private final String START_TEXT = "*** START OF THE PROJECT GUTENBERG EBOOK";
        private final String END_TEXT = "*** END OF THE PROJECT GUTENBERG EBOOK";

        @Getter
        List<Node> beforeStart = new ArrayList<>();

        @Getter
        List<Node> afterEnd = new ArrayList<>();

        private boolean before = true;
        private boolean after = false;

        @Override
        public boolean touch(Node n) {
            if (after) {
                afterEnd.add(n);
                return true;
            }

            if (n instanceof TextNode t) {
                if (before && t.text().startsWith(START_TEXT)) {
                    before = false;
                    beforeStart.add(n); // Want to include the TextNode with the start words in before start
                    return true;
                } else if (!before && t.text().startsWith(END_TEXT)) {
                    after = true;
                    afterEnd.add(n);
                    return true;
                }
            }

            if (before) {
                beforeStart.add(n);
                return true;
            }

            return false;
        }
    }

    @Getter
    static class Unused implements NodeToucher {

        List<Node> unused = new ArrayList<>();

        @Override
        public boolean touch(Node n) {
            unused.add(n);
            return false;
        }
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
