package org.xbib.elasticsearch.index.analysis.decompound;

import java.util.ArrayList;
import java.util.List;

class Node {

    private List<String> classes;
    public String content;
    private List<Node> children;
    public int pos;

    public Node() {
        this.content = "";
        this.classes = new ArrayList();
        this.children = new ArrayList();
    }

    public Node(String content) {
        this.content = content;
        this.classes = new ArrayList();
        this.children = new ArrayList();
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
        StringBuilder sb = new StringBuilder()
                .append('[')
                .append(content)
                .append(',')
                .append(classes)
                .append(']');
        return sb.toString();
    }
}