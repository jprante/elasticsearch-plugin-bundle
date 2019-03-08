package org.xbib.elasticsearch.plugin.bundle.index.analysis.decompound.fst;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.xbib.elasticsearch.plugin.bundle.common.decompound.fst.FstDecompounder;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Finite state decompound token filter.
 */
public class FstDecompoundTokenFilter extends TokenFilter {

    private final LinkedList<String> tokens;

    private final FstDecompounder fstDecompounder;

    private final boolean respectKeywords;

    private final boolean subwordsonly;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private AttributeSource.State current;

    protected FstDecompoundTokenFilter(TokenStream input, FstDecompounder fstDecompounder,
                                       boolean respectKeywords, boolean subwordsonly) {
        super(input);
        this.tokens = new LinkedList<>();
        this.fstDecompounder = fstDecompounder;
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
        tokens.addAll(doDecompound(term));
        return tokens.isEmpty();
    }

    private List<String> doDecompound(String term) {
        List<String> list = new LinkedList<>();
        for (String suggestions : fstDecompounder.decompound(term)) {
            for (String suggestion : suggestions.split(",")) {
                list.addAll(Arrays.asList(suggestion.split("\\.")));
            }
        }
        return list;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokens.clear();
        current = null;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof FstDecompoundTokenFilter &&
                fstDecompounder.equals( ((FstDecompoundTokenFilter)object).fstDecompounder);
    }

    @Override
    public int hashCode() {
        return fstDecompounder.hashCode();
    }
}
