package io.github.gsmedley213.contentextractor.strategy;

import io.github.gsmedley213.contentextractor.model.ContentNodes;
import io.github.gsmedley213.contentextractor.toucher.NodeToucher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.util.List;

public interface ContentExtractor {
    ContentNodes extract(Document doc);

    String strategyDescription();

    static void walkTree(Node node, List<NodeToucher> users) {
        for (NodeToucher check : users) {
            if (check.touch(node)) {
                break;
            }
        }
        for (Node child : node.childNodes()) {
            walkTree(child, users);
        }
    }
}
