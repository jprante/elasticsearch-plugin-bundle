package org.xbib.elasticsearch.index.analysis.sortform;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class SortformTokenFilter extends TokenFilter {

    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final List<Pattern> patterns;

    protected SortformTokenFilter(TokenStream input, List<Pattern> patterns) {
        super(input);
        this.patterns = patterns;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            for (Pattern pattern : patterns) {
                s = pattern.matcher(s).replaceAll("");
            }
            termAtt.setEmpty().append(s);
            return true;
        }
    }
}
