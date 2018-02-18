package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.xbib.elasticsearch.common.decompound.fst.FstDecompounder;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Finite state decompound token filter.
 */
public class FstDecompoundTokenFilter extends TokenFilter {

    protected final LinkedList<DecompoundToken> tokens;

    protected final FstDecompounder fstDecompounder;

    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private State current;

    protected FstDecompoundTokenFilter(TokenStream input, FstDecompounder fstDecompounder) {
        super(input);
        this.tokens = new LinkedList<>();
        this.fstDecompounder = fstDecompounder;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            if (current == null) {
                throw new IllegalArgumentException("current is null");
            }
            DecompoundToken token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token.txt);
            offsetAtt.setOffset(token.startOffset, token.endOffset);
            posIncAtt.setPositionIncrement(0);
            return true;
        }
        if (input.incrementToken()) {
            decompound();
            if (!tokens.isEmpty()) {
                current = captureState();
            }
            return true;
        } else {
            return false;
        }
    }

    protected synchronized void decompound() {
        int start = offsetAtt.startOffset();
        CharSequence term = new String(termAtt.buffer(), 0, termAtt.length());
        for (String suggestions : fstDecompounder.decompound(term.toString())) {
            for (String suggestion : suggestions.split(",")) {
                int off = start;
                int maxlen = -1;
                for (String s : suggestion.split("\\.")) {
                    int len = s.length();
                    tokens.add(new DecompoundToken(s, off, len));
                    if (len > maxlen) {
                        maxlen = len;
                    }
                }
                start += maxlen;
            }
        }
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

    private class DecompoundToken {

        final CharSequence txt;
        final int startOffset;
        final int endOffset;

        DecompoundToken(CharSequence txt, int offset, int length) {
            this.txt = txt;
            //int startOff = FstDecompoundTokenFilter.this.offsetAtt.startOffset();
            //int endOff = FstDecompoundTokenFilter.this.offsetAtt.endOffset();
            //if (endOff - startOff != FstDecompoundTokenFilter.this.termAtt.length()) {
            //    this.startOffset = startOff;
            //    this.endOffset = endOff;
            //} else {
                this.startOffset = offset;
                this.endOffset = offset + length;
            //}
        }
    }
}
