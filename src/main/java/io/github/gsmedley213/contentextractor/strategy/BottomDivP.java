package io.github.gsmedley213.contentextractor.strategy;

import io.github.gsmedley213.contentextractor.model.ContentNodes;
import io.github.gsmedley213.contentextractor.toucher.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.gsmedley213.contentextractor.strategy.ContentExtractor.walkTree;

/**
 * Extract all nodes with a tag name of "div" or "p" that do not have a child that is a "div" or a "p".
 */
public class BottomDivP implements ContentExtractor {

    public static int MIN_LENGTH = 25;

    @Override
    public ContentNodes extract(Document doc) {
        var limit = new LimitByStartEnd();
        var elements = new Elements();
        var textNodes = new TextNodes();
        var unused = new Unused();
        var touchers = Arrays.asList(limit, elements, textNodes, unused);
        walkTree(doc, touchers);

        var consideredElements = Set.of("p", "div");

        List<Element> limited = elements.getElements().stream()
                .filter(e -> consideredElements.contains(e.tagName()))
                .filter(e -> !hasChildWithTag(e, consideredElements))
                .filter(e -> !e.text().isBlank())
                .toList();

        TextNodes limitedText = new TextNodes();
        limited.forEach(e -> walkTree(e, List.of(limitedText)));

        Set<TextNode> limitedNodes = new HashSet<>(limitedText.getTextNodes());
        List<TextNode> missedNodes =
                textNodes.getTextNodes().stream().filter(tn -> !limitedNodes.contains(tn)).toList();

        return new ContentNodes(limited, missedNodes);
    }

    @Override
    public String strategyDescription() {
        return "All P and DIV elements that do not have a nested P or DIV element.";
    }

    private static boolean hasChildWithTag(Element element, Set<String> tags) {
        return element.getAllElements().stream()
                .filter(e -> e != element)
                .anyMatch(e -> tags.contains(e.tagName()));
    }
}
