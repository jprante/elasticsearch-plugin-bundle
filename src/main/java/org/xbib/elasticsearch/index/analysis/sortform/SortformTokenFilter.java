package org.xbib.elasticsearch.index.analysis.sortform;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Sort form token filter.
 */
public class SortformTokenFilter extends TokenFilter {

    private static final Pattern[] patterns = {
            Pattern.compile("\\s*<<.*?>>\\s*"),
            Pattern.compile("\\s*<.*?>\\s*"),
            Pattern.compile("\\s*\u0098.*?\u009C\\s*"),
            Pattern.compile("\\s*\u02BE.*?\u02BB\\s*"),
            Pattern.compile("\\s*\u00AC.*?\u00AC\\s*")
    };
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    protected SortformTokenFilter(TokenStream input) {
        super(input);
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

    @Override
    public boolean equals(Object object) {
        return object instanceof SortformTokenFilter;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
