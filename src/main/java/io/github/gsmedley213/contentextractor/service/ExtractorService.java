package io.github.gsmedley213.contentextractor.service;

import io.github.gsmedley213.contentextractor.model.AnnotationJob;
import io.github.gsmedley213.contentextractor.model.ContentNodes;
import io.github.gsmedley213.contentextractor.strategy.BottomDivP;
import io.github.gsmedley213.contentextractor.strategy.ContentExtractor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

public interface ExtractorService {

    AnnotationJob extractAndMark(Document doc);

    @Slf4j
    @Service
    class ExtractorServiceImpl implements ExtractorService {

        ContentExtractor extractor = new BottomDivP();

        List<Consumer<ContentNodes>> checks = Arrays.asList(
                this::checkForDuplication,
                this::checkForLargeMissedText,
                this::totalMissedText
        );

        @Override
        public AnnotationJob extractAndMark(Document doc) {
            log.info("Extracting contents for: {} using \"{}\" strategy", doc.title(), extractor.strategyDescription());

            ContentNodes content = extractor.extract(doc);

            checks.forEach(check -> check.accept(content)); // Run all checks on the result.

            return null;
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
}
