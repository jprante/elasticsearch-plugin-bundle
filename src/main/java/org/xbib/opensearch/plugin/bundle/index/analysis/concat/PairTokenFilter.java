package org.xbib.opensearch.plugin.bundle.index.analysis.concat;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Pair token filter.
 */
public final class PairTokenFilter extends TokenFilter {

    private final CharTermAttribute termAttr;

    private final Map<String, String> pairs;

    private final Queue<String> queue;

    protected PairTokenFilter(TokenStream input, Map<String, String> pairs) {
        super(input);
        this.termAttr = addAttribute(CharTermAttribute.class);
        this.pairs = pairs;
        this.queue = new LinkedList<>();
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!queue.isEmpty()) {
            termAttr.append(queue.poll());
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        Deque<String> stack = new ArrayDeque<>();
        while (pairs.containsKey(termAttr.toString())) {
            String term = termAttr.toString();
            stack.push(term);
            if (!input.incrementToken()) {
                break;
            }
            String next = termAttr.toString();
            if (pairs.get(term).equals(next)) {
                stack.pop();
                stack.push(term + " " + next);
                break;
            } else if (!pairs.containsKey(next)) {
                stack.push(next);
            }
        }
        for (String term : stack) {
            queue.add(term);
        }
        if (!queue.isEmpty()) {
            termAttr.setEmpty().append(queue.poll());
        }
        return true;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof PairTokenFilter &&
                pairs.equals( ((PairTokenFilter)object).pairs);
    }

    @Override
    public int hashCode() {
        return pairs.hashCode();
    }
}
