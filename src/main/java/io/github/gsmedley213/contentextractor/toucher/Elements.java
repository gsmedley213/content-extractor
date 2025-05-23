package io.github.gsmedley213.contentextractor.toucher;

import lombok.Getter;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class Elements implements NodeToucher {

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
