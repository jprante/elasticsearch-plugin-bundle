package org.xbib.elasticsearch.index.analysis.year;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class GregorianYearTokenFilter extends TokenFilter {

    private static final Pattern pattern = Pattern.compile("(\\d{4})");

    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final String defaultYear;

    protected GregorianYearTokenFilter(TokenStream input, String defaultYear) {
        super(input);
        this.defaultYear = defaultYear;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            Matcher m = pattern.matcher(s);
            termAtt.setEmpty();
            if (!m.matches()) {
                termAtt.append(defaultYear);
            } else {
                while (m.find()) {
                    termAtt.append(m.group());
                }
            }
            return true;
        }
    }
}
