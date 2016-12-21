package org.xbib.elasticsearch.index.analysis.standardnumber;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.elasticsearch.common.settings.Settings;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberService;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 */
public class StandardnumberTokenFilter extends TokenFilter {

    private final LinkedList<PackedTokenAttributeImpl> tokens;

    private final StandardnumberService service;

    private final Settings settings;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private State current;

    protected StandardnumberTokenFilter(TokenStream input, StandardnumberService service, Settings settings) {
        super(input);
        this.tokens = new LinkedList<>();
        this.service = service;
        this.settings = settings;
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
        if (input.incrementToken()) {
            detect();
            if (!tokens.isEmpty()) {
                current = captureState();
            }
            return true;
        } else {
            return false;
        }
    }

    private void detect() throws CharacterCodingException {
        CharSequence term = new String(termAtt.buffer(), 0, termAtt.length());
        Collection<CharSequence> variants = service.lookup(settings, term);
        for (CharSequence ch : variants) {
            if (ch != null) {
                PackedTokenAttributeImpl token = new PackedTokenAttributeImpl();
                token.append(ch);
                tokens.add(token);
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
        return object instanceof StandardnumberTokenFilter &&
                service.equals(((StandardnumberTokenFilter)object).service) &&
                settings.equals(((StandardnumberTokenFilter)object).settings);
    }

    @Override
    public int hashCode() {
        return service.hashCode() ^ settings.hashCode();
    }
}
