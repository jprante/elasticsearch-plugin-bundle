package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.NumberFormat;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.text.ParsePosition;

/**
 * ICU number format token filter.
 */
public final class IcuNumberFormatTokenFilter extends TokenFilter {

    private final NumberFormat numberFormat;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    public IcuNumberFormatTokenFilter(TokenStream input, NumberFormat numberFormat) {
        super(input);
        this.numberFormat = numberFormat;
    }

    @Override
    public boolean incrementToken() throws IOException {
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
            typeAtt.setType("<ALPHANUM>");
            return true;
        }
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IcuNumberFormatTokenFilter &&
                numberFormat.equals(((IcuNumberFormatTokenFilter) object).numberFormat);
    }

    @Override
    public int hashCode() {
        return numberFormat.hashCode();
    }
}
