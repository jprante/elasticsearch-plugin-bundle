package org.xbib.elasticsearch.plugin.bundle.index.analysis.lemmatize;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.xbib.elasticsearch.plugin.bundle.common.fsa.Dictionary;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.LinkedList;

/**
 * Lemmative token filter.
 */
public class LemmatizeTokenFilter extends TokenFilter {

    private final LinkedList<String> tokens;

    private final Dictionary dictionary;

    private final boolean respectKeywords;

    private final boolean lemmaOnly;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private AttributeSource.State current;

    protected LemmatizeTokenFilter(TokenStream input, Dictionary dictionary,
                                   boolean respectKeywords, boolean lemmaOnly) {
        super(input);
        this.tokens = new LinkedList<>();
        this.dictionary = dictionary;
        this.respectKeywords = respectKeywords;
        this.lemmaOnly = lemmaOnly;
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
            if (!lemmaOnly) {
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
        if (!expand()) {
            current = captureState();
            if (lemmaOnly) {
                String token = tokens.removeFirst();
                restoreState(current);
                termAtt.setEmpty().append(token);
                return true;
            }
        }
        return true;
    }

    private boolean expand() throws CharacterCodingException {
        String term = new String(termAtt.buffer(), 0, termAtt.length());
        CharSequence s = dictionary.lookup(term);
        if (s != null) {
            tokens.add(s.toString());
        }
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
        return object instanceof LemmatizeTokenFilter &&
                tokens.equals(((LemmatizeTokenFilter)object).tokens) &&
                dictionary.equals(((LemmatizeTokenFilter)object).dictionary) &&
                respectKeywords == ((LemmatizeTokenFilter)object).respectKeywords &&
                lemmaOnly == ((LemmatizeTokenFilter)object).lemmaOnly;
    }

    @Override
    public int hashCode() {
        return tokens.hashCode() ^ dictionary.hashCode()
                ^ Boolean.hashCode(respectKeywords) ^ Boolean.hashCode(lemmaOnly);
    }
}
