package io.github.gsmedley213.contentextractor.service;

import io.github.gsmedley213.contentextractor.model.Notable;
import org.jsoup.nodes.Document;

import java.util.List;

public interface ExtractorService {

    /**
     * Takes an HTML document and tries to extract the text with content we would care to annotate. Adds attributes to
     * all elements we extracted content from to allow us to associate that content later.
     * @param doc Document to extract content from and mark. WARNING: This modifies the passed document to mark places
     *            text content was extracted from.
     * @return List of Notable object containing extracted text and id to reference element extracted from.
     */
    List<Notable> extractAndMark(Document doc);

}
