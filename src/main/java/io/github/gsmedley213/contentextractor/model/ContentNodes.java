package io.github.gsmedley213.contentextractor.model;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

import java.util.List;

public record ContentNodes(List<Element> contents, List<TextNode> missedText) {
}
