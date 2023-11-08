package org.xbib.opensearch.plugin.bundle.index.analysis.concat;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * The ConcatTokenFilter is authored by
 * <a href="http://sujitpal.blogspot.de/2011/07/lucene-token-concatenating-tokenfilter_30.html">Sujit Pal</a>.
 */
public final class ConcatTokenFilter extends TokenFilter {

    private CharTermAttribute termAttr;

    private PositionIncrementAttribute posIncAttr;

    private State current;

    private LinkedList<List<String>> words;

    private LinkedList<String> phrases;

    private boolean concat = false;

    protected ConcatTokenFilter(TokenStream input) {
        super(input);
        this.termAttr = addAttribute(CharTermAttribute.class);
        this.posIncAttr = addAttribute(PositionIncrementAttribute.class);
        this.words = new LinkedList<>();
        this.phrases = new LinkedList<>();
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String term = new String(termAttr.buffer(), 0, termAttr.length());
            List<String> word = posIncAttr.getPositionIncrement() > 0 ?
                    new LinkedList<>() : words.removeLast();
            word.add(term);
            words.add(word);
        }
        if (!concat) {
            makePhrases(words, phrases, 0);
            concat = true;
        }
        if (!phrases.isEmpty()) {
            String phrase = phrases.removeFirst();
            restoreState(current);
            clearAttributes();
            termAttr.copyBuffer(phrase.toCharArray(), 0, phrase.length());
            termAttr.setLength(phrase.length());
            current = captureState();
            return true;
        }
        concat = false;
        phrases.clear();
        words.clear();
        return false;
    }

    private void makePhrases(List<List<String>> words, List<String> phrases, int currPos) {
        for (int i = currPos; i < words.size(); i++) {
            if (phrases.isEmpty()) {
                phrases.addAll(words.get(i));
            } else {
                List<String> newPhrases = new LinkedList<>();
                for (String phrase : phrases) {
                    for (String word : words.get(i)) {
                        newPhrases.add(phrase + " " + word);
                    }
                }
                phrases.clear();
                phrases.addAll(newPhrases);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ConcatTokenFilter;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
