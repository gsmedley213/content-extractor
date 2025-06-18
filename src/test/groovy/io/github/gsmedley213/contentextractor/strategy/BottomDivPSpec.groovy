package io.github.gsmedley213.contentextractor.strategy

import org.jsoup.Jsoup
import spock.lang.Specification

class BottomDivPSpec extends Specification {

    def "extract returns only the content between Gutenberg markers"() {
        given:
        def html =
                '''
<html><body>
<div>Not part of the book</div>
<div><span>*** START OF THE PROJECT GUTENBERG EBOOK THE LIVESTOCK PRODUCER AND ARMOUR ***</span></div>
<div>Hello World!</div>
<div><span>*** END OF THE PROJECT GUTENBERG EBOOK THE LIVESTOCK PRODUCER AND ARMOUR ***</span></div>
<div>After end of the book</div>
</body></html>
'''
        def doc = Jsoup.parse(html)
        def extractor = new BottomDivP()

        when:
        def result = extractor.extract(doc)
        def texts = result.contents()*.text()

        then:
        texts.any { it.contains("Hello World!") }
        !texts.any { it.contains("Not part of the book") }
        !texts.any { it.contains("After end of the book") }
    }
}