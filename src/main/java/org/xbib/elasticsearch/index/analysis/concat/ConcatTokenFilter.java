package org.xbib.elasticsearch.index.analysis.concat;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ConcatTokenFilter extends TokenFilter {

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
        this.words = new LinkedList<List<String>>();
        this.phrases = new LinkedList<String>();
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String term = new String(termAttr.buffer(), 0, termAttr.length());
            List<String> word = posIncAttr.getPositionIncrement() > 0 ?
                    new LinkedList<String>() : words.removeLast();
            word.add(term);
            words.add(word);
        }
        // now write out as a single token
        if (!concat) {
            makePhrases(words, phrases, 0);
            concat = true;
        }
        if (phrases.size() > 0) {
            String phrase = phrases.removeFirst();
            restoreState(current);
            clearAttributes();
            termAttr.copyBuffer(phrase.toCharArray(), 0, phrase.length());
            termAttr.setLength(phrase.length());
            current = captureState();
            return true;
        }
        concat = false;
        return false;
    }

    private void makePhrases(List<List<String>> words, List<String> phrases, int currPos) {
        for (int i = currPos; i < words.size(); i++) {
            if (phrases.size() == 0) {
                phrases.addAll(words.get(i));
            } else {
                List<String> newPhrases = new LinkedList<String>();
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
}