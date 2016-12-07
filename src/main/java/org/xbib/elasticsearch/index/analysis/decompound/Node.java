package org.xbib.elasticsearch.index.analysis.decompound;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
class Node {

    public String content;

    public int pos;

    private List<String> classes;

    private List<Node> children;

    public Node() {
        this.content = "";
        this.classes = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public Node(String content) {
        this.content = content;
        this.classes = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public Node classes(List<String> classes) {
        this.classes = classes;
        return this;
    }

    public List<String> classes() {
        return classes;
    }

    public Node children(List<Node> children) {
        this.children = children;
        return this;
    }

    public List<Node> children() {
        return children;
    }

    @Override
    public String toString() {
        return "[" + content + ',' + classes + ']';
    }
}