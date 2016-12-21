package org.xbib.elasticsearch.index.analysis.baseform;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.xbib.elasticsearch.common.fsa.Dictionary;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.LinkedList;

/**
 *
 */
public class BaseformTokenFilter extends TokenFilter {

    private final LinkedList<PackedTokenAttributeImpl> tokens;

    private final Dictionary dictionary;

    private final boolean respectKeywords;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private AttributeSource.State current;

    protected BaseformTokenFilter(TokenStream input, Dictionary dictionary, boolean respectKeywords) {
        super(input);
        this.tokens = new LinkedList<>();
        this.dictionary = dictionary;
        this.respectKeywords = respectKeywords;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            if (current == null) {
                throw new IllegalArgumentException("current is null");
            }
            PackedTokenAttributeImpl token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token);
            posIncAtt.setPositionIncrement(0);
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        if (respectKeywords && keywordAtt.isKeyword()) {
            return true;
        }
        baseform();
        if (!tokens.isEmpty()) {
            current = captureState();
        }
        return true;
    }

    private void baseform() throws CharacterCodingException {
        CharSequence term = new String(termAtt.buffer(), 0, termAtt.length());
        CharSequence s = dictionary.lookup(term);
        if (s != null && s.length() > 0) {
            PackedTokenAttributeImpl impl = new PackedTokenAttributeImpl();
            impl.append(s);
            tokens.add(impl);
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
        return object instanceof BaseformTokenFilter &&
                tokens.equals(((BaseformTokenFilter)object).tokens) &&
                dictionary.equals(((BaseformTokenFilter)object).dictionary) &&
                respectKeywords == ((BaseformTokenFilter)object).respectKeywords;
    }

    @Override
    public int hashCode() {
        return tokens.hashCode() ^ dictionary.hashCode() ^ Boolean.hashCode(respectKeywords);
    }

}
