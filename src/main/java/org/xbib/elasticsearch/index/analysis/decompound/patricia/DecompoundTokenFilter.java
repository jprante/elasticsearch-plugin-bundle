package org.xbib.elasticsearch.index.analysis.decompound.patricia;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;
import org.xbib.elasticsearch.common.decompound.patricia.Decompounder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Decompound token filter.
 */
public class DecompoundTokenFilter extends TokenFilter {

    private final LinkedList<DecompoundToken> tokens;

    private final Decompounder decomp;

    private final boolean respectKeywords;

    private final boolean subwordsonly;

    private final boolean usePayload;

    private final Map<String, List<String>> cache;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private final PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);

    private AttributeSource.State current;

    private static final byte TOKEN_TYPE = 1;

    private static final byte DECOMP_TOKEN_TYPE = 2;

    protected DecompoundTokenFilter(TokenStream input, Decompounder decomp, boolean respectKeywords,
                                    boolean subwordsonly, boolean usePayload, Map<String, List<String>> cache) {
        super(input);
        this.tokens = new LinkedList<>();
        this.decomp = decomp;
        this.respectKeywords = respectKeywords;
        this.subwordsonly = subwordsonly;
        this.usePayload= usePayload;
        this.cache = cache;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            if (current == null) {
                throw new IllegalArgumentException("current is null");
            }
            DecompoundToken token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token.value);
            offsetAtt.setOffset(token.startOffset, token.endOffset);
            if (!subwordsonly) {
                posIncAtt.setPositionIncrement(0);
            }
            if (usePayload) {
                addPayload(DECOMP_TOKEN_TYPE);
            }
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        if (respectKeywords && keywordAtt.isKeyword()) {
            if (usePayload) {
                addPayload(TOKEN_TYPE);
            }
            return true;
        }
        if (!decompound()) {
            current = captureState();
            if (subwordsonly) {
                DecompoundToken token = tokens.removeFirst();
                restoreState(current);
                termAtt.setEmpty().append(token.value);
                offsetAtt.setOffset(token.startOffset, token.endOffset);
                if (usePayload) {
                    addPayload(DECOMP_TOKEN_TYPE);
                }
                return true;
            }
        }
        return true;
    }

    protected boolean decompound() {
        String term = new String(termAtt.buffer(), 0, termAtt.length());
        List<String> list = (cache != null ?
                cache.computeIfAbsent(term, decomp::decompound) : decomp.decompound(term));
        for (String string : list) {
            DecompoundToken token = new DecompoundToken(string, termAtt, offsetAtt);
            tokens.add(token);
        }
        return tokens.isEmpty();
    }

    private void addPayload(byte tokenType) {
        BytesRef payload;
        switch (tokenType) {
            case TOKEN_TYPE:
                payload = new BytesRef();
                break;
            case DECOMP_TOKEN_TYPE:
            default:
                payload = payloadAtt.getPayload();
                if (payload != null && payload.length > 0) {
                    payload = BytesRef.deepCopyOf(payload);
                } else {
                    payload = new BytesRef(new byte[1]);
                }
                payload.bytes[payload.offset] |= tokenType;
                break;
        }
        payloadAtt.setPayload(payload);
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

    class DecompoundToken {
        final CharSequence value;
        final int startOffset;
        final int endOffset;

        DecompoundToken(CharSequence value, CharTermAttribute termAttribute, OffsetAttribute offsetAttribute) {
            this.value = value;
            if (offsetAttribute.endOffset() - offsetAttribute.startOffset() != termAttribute.length()) {
                this.startOffset = offsetAttribute.startOffset();
                this.endOffset = offsetAttribute.endOffset();
            } else {
                this.startOffset = offsetAttribute.startOffset();
                this.endOffset = offsetAttribute.startOffset() + termAttribute.length();
            }
        }
    }
}
