package org.xbib.elasticsearch.index.analysis.decompound;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.LinkedList;

public class DecompoundTokenFilter extends TokenFilter {

    protected final LinkedList<DecompoundToken> tokens;

    protected final Decompounder decomp;

    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private AttributeSource.State current;

    protected DecompoundTokenFilter(TokenStream input, Decompounder decomp) {
        super(input);
        this.tokens = new LinkedList<DecompoundToken>();
        this.decomp = decomp;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            assert current != null;
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

    protected void decompound() {
        int start = offsetAtt.startOffset();
        CharSequence term = new String(termAtt.buffer(), 0, termAtt.length());
        for (String s : decomp.decompound(term.toString())) {
            int len = s.length();
            tokens.add(new DecompoundToken(s, start, len));
            start += len;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokens.clear();
        current = null;
    }

    protected class DecompoundToken {

        public final CharSequence txt;
        public final int startOffset;
        public final int endOffset;

        public DecompoundToken(CharSequence txt, int offset, int length) {
            this.txt = txt;
            int startOff = DecompoundTokenFilter.this.offsetAtt.startOffset();
            int endOff = DecompoundTokenFilter.this.offsetAtt.endOffset();
            if (endOff - startOff != DecompoundTokenFilter.this.termAtt.length()) {
                this.startOffset = startOff;
                this.endOffset = endOff;
            } else {
                this.startOffset = offset;
                this.endOffset = offset + length;
            }
        }
    }
}
