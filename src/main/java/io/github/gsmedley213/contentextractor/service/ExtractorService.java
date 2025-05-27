package io.github.gsmedley213.contentextractor.service;

import io.github.gsmedley213.contentextractor.model.AnnotationJob;
import io.github.gsmedley213.contentextractor.model.ContentNodes;
import io.github.gsmedley213.contentextractor.strategy.BottomDivP;
import io.github.gsmedley213.contentextractor.strategy.ContentExtractor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ExtractorService {

    AnnotationJob extractAndMark(Document doc);

    @Slf4j
    @Service
    class ExtractorServiceImpl implements ExtractorService {

        ContentExtractor extractor = new BottomDivP();

        @Override
        public AnnotationJob extractAndMark(Document doc) {
            log.info("Extracting contents for: {} using \"{}\" strategy", doc.title(), extractor.strategyDescription());

            ContentNodes content = extractor.extract(doc);

            checkForDuplication(content.contents());

            log.info("Extracted {} elements. This method missed total of {} characters of text.",
                    content.contents().size(), content.missedText().stream()
                            .map(tn -> tn.text().trim())
                            .mapToInt(String::length)
                            .sum());

            return null;
        }

        private void checkForDuplication(List<Element> contents) {

        }
    }
}
