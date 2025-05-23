package io.github.gsmedley213.contentextractor.toucher;

import lombok.Getter;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Unused implements NodeToucher {

    List<Node> unused = new ArrayList<>();

    @Override
    public boolean touch(Node n) {
        unused.add(n);
        return false;
    }
}
