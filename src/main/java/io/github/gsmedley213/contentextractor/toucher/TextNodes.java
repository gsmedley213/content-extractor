package io.github.gsmedley213.contentextractor.toucher;

import lombok.Getter;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TextNodes implements NodeToucher {
    List<TextNode> textNodes = new ArrayList<>();

    @Override
    public boolean touch(Node n) {
        if (n instanceof TextNode tn) {
            textNodes.add(tn);
            return true;
        }
        return false;
    }
}