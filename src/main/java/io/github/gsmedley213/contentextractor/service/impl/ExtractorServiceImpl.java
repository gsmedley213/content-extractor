package io.github.gsmedley213.contentextractor.service.impl;

import io.github.gsmedley213.contentextractor.model.ContentNodes;
import io.github.gsmedley213.contentextractor.model.Notable;
import io.github.gsmedley213.contentextractor.service.ExtractorService;
import io.github.gsmedley213.contentextractor.strategy.ContentExtractor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
public
class ExtractorServiceImpl implements ExtractorService {

    final ContentExtractor extractor;

    List<Consumer<ContentNodes>> checks = Arrays.asList(
            this::checkForDuplication,
            this::checkForLargeMissedText,
            this::totalMissedText
    );

    public ExtractorServiceImpl(ContentExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public List<Notable> extractAndMark(Document doc) {
        log.info("Extracting contents for: {} using \"{}\" strategy", doc.title(), extractor.strategyDescription());

        ContentNodes content = extractor.extract(doc);

        checks.forEach(check -> check.accept(content)); // Run all checks on the result.

        List<Notable> result = new ArrayList<>();
        int elementId = 0;
        for (Element element : content.contents()) {
            Optional<String> cleanedText = clean(element);
            if (cleanedText.isPresent()) {
                elementId++;
                element.attr("annotation-id", String.valueOf(elementId));
                result.add(new Notable(elementId, cleanedText.get()));
            }
        }

        return result;
    }

    private Optional<String> clean(Element e) {
        Element copy = e.clone();

        copy.getElementsByClass("pagenum").forEach(Node::remove); // Remove pagenum spans

        return copy.text().trim().isBlank() ? Optional.empty() : Optional.of(copy.text());
    }

    private void checkForLargeMissedText(ContentNodes nodes) {
        List<TextNode> largeNode = nodes.missedText().stream().filter(tn -> tn.text().length() > 100).toList();

        largeNode.forEach(tn -> log.warn("Extraction strategy excluded text of length {}: {}", tn.text().length(),
                tn.text())); // TODO raise alert
    }

    private void checkForDuplication(ContentNodes nodes) {
        var contentSet = new HashSet<>(nodes.contents());
        List<Element> childElements = nodes.contents().stream()
                .flatMap(e -> e.getAllElements().stream().filter(child -> child != e))
                .toList();

        List<Element> duplicates = childElements.stream()
                .filter(contentSet::contains)
                .toList();

        duplicates.forEach(e -> log.warn("Warning: element with text \"{}\" is a child of another element",
                e.text())); // TODO raise alert
    }

    private void totalMissedText(ContentNodes content) {
        int totalMissedText = content.missedText().stream()
                .map(tn -> tn.text().trim())
                .mapToInt(String::length)
                .sum();

        if (totalMissedText > 500) {
            log.warn("Extracted {} elements. This method missed total of {} characters of text.",
                    content.contents().size(), totalMissedText);
        }
    }
}
