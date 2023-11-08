package org.xbib.opensearch.plugin.bundle.common.decompound.patricia;

import java.util.ArrayList;
import java.util.List;

/**
 * Node.
 */
class Node {

    private String content;

    private int pos;

    private List<String> classes;

    private List<Node> children;

    Node() {
        this.content = "";
        this.classes = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    Node(String content) {
        this.content = content;
        this.classes = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getPos() {
        return pos;
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
