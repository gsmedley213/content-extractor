package io.github.gsmedley213.contentextractor.toucher;

import lombok.Getter;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

public class LimitByStartEnd implements NodeToucher {
    private final String START_TEXT = "*** START OF THE PROJECT GUTENBERG EBOOK";
    private final String END_TEXT = "*** END OF THE PROJECT GUTENBERG EBOOK";

    @Getter
    List<Node> beforeStart = new ArrayList<>();

    @Getter
    List<Node> afterEnd = new ArrayList<>();

    private boolean before = true;
    private boolean after = false;

    @Override
    public boolean touch(Node n) {
        if (after) {
            afterEnd.add(n);
            return true;
        }

        if (n instanceof TextNode t
                && before
                && t.text().startsWith(START_TEXT)) {
            before = false;
            beforeStart.add(n); // Want to include the TextNode with the start words in before start
            return true;
        }

        // If we wait until we find the TextNode with the end text we'll have already added its span element parent
        if (n instanceof Element e
                && !before
                && e.text().startsWith(END_TEXT)) {
            after = true;
            afterEnd.add(n);
            return true;
        }

        if (before) {
            beforeStart.add(n);
            return true;
        }

        return false;
    }
}
