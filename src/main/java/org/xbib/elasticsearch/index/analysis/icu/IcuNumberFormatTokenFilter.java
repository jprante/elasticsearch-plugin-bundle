package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.NumberFormat;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.text.ParsePosition;

/**
 *
 */
public final class IcuNumberFormatTokenFilter extends TokenFilter {

    private final NumberFormat numberFormat;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public IcuNumberFormatTokenFilter(TokenStream input, NumberFormat numberFormat) {
        super(input);
        this.numberFormat = numberFormat;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            ParsePosition parsePosition = new ParsePosition(0);
            Number result = numberFormat.parse(s, parsePosition);
            if (parsePosition.getIndex() > 0) {
                // zehn-tausend -> zehntausend
                // one hundred thousand -> onehundredthousand
                s = numberFormat.format(result).replaceAll("[\u00AD\u0020]", "");
            }
            termAtt.setEmpty().append(s);
            return true;
        }
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IcuNumberFormatTokenFilter &&
                numberFormat.equals(((IcuNumberFormatTokenFilter)object).numberFormat);
    }

    @Override
    public int hashCode() {
        return numberFormat.hashCode();
    }
}
