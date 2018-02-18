package org.xbib.elasticsearch.index.analysis.decompound.patricia;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.xbib.elasticsearch.common.decompound.patricia.Decompounder;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Decompound token filter.
 */
public class DecompoundTokenFilter extends TokenFilter {

    private final LinkedList<String> tokens;

    private final Decompounder decomp;

    private final boolean respectKeywords;

    private final boolean subwordsonly;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private AttributeSource.State current;

    protected DecompoundTokenFilter(TokenStream input, Decompounder decomp, boolean respectKeywords, boolean subwordsonly) {
        super(input);
        this.tokens = new LinkedList<>();
        this.decomp = decomp;
        this.respectKeywords = respectKeywords;
        this.subwordsonly = subwordsonly;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            if (current == null) {
                throw new IllegalArgumentException("current is null");
            }
            String token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token);
            if (!subwordsonly) {
                posIncAtt.setPositionIncrement(0);
            }
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        if (respectKeywords && keywordAtt.isKeyword()) {
            return true;
        }
        if (!decompound()) {
            current = captureState();
            if (subwordsonly) {
                String token = tokens.removeFirst();
                restoreState(current);
                termAtt.setEmpty().append(token);
                return true;
            }
        }
        return true;
    }

    protected boolean decompound() {
        String term = new String(termAtt.buffer(), 0, termAtt.length());
        tokens.addAll(decomp.decompound(term));
        return tokens.isEmpty();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokens.clear();
        current = null;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof DecompoundTokenFilter &&
                tokens.equals(((DecompoundTokenFilter)object).tokens) &&
                respectKeywords == ((DecompoundTokenFilter)object).respectKeywords &&
                subwordsonly == ((DecompoundTokenFilter)object).subwordsonly;
    }

    @Override
    public int hashCode() {
        return tokens.hashCode() ^ Boolean.hashCode(respectKeywords) ^ Boolean.hashCode(subwordsonly);
    }
}
