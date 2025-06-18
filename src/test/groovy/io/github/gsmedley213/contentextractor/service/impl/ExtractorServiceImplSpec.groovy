package io.github.gsmedley213.contentextractor.service.impl

import io.github.gsmedley213.contentextractor.model.ContentNodes
import io.github.gsmedley213.contentextractor.model.Notable
import io.github.gsmedley213.contentextractor.strategy.ContentExtractor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import spock.lang.Specification

class ExtractorServiceImplSpec extends Specification {

    def "extractAndMark delegates to ContentExtractor and returns List of Notable objects with text content extracted elements."() {
        given:
        def extractor = Mock(ContentExtractor)
        extractor.strategyDescription() >> "Test Strategy"
        def div = new Element("div").text("Hello Test!")
        def expectedContent = new ContentNodes([div], [])
        extractor.extract(_ as Document) >> expectedContent

        def service = new ExtractorServiceImpl(extractor)

        and:
        def html = "<html><head><title>Test</title></head><body><div>Hello Test!</div></body></html>"
        def doc = Jsoup.parse(html)

        when:
        def result = service.extractAndMark(doc)

        then:
        1 * extractor.extract(doc) >> expectedContent
        result == [new Notable(1, "Hello Test!")]
    }
}