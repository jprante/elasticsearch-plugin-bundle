package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.MeasureFormat;
import com.ibm.icu.util.Measure;
import com.ibm.icu.util.MeasureUnit;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.text.ParsePosition;

/**
 *
 */
public final class IcuMeasureFormatTokenFilter extends TokenFilter {

    private final CharTermAttribute termAtt;

    private final MeasureFormat measureFormat;

    public IcuMeasureFormatTokenFilter(TokenStream input, MeasureFormat measureFormat) {
        super(input);
        this.termAtt = addAttribute(CharTermAttribute.class);
        this.measureFormat = measureFormat;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        } else {
            String s = termAtt.toString();
            ParsePosition parsePosition = new ParsePosition(0);
            Measure measure = measureFormat.parseObject(s, parsePosition);
            if (parsePosition.getIndex() > 0) {
                Number number = measure.getNumber();
                MeasureUnit unit = measure.getUnit();
                if (unit == MeasureUnit.KILOBYTE) {
                    number = number.doubleValue() * 1024;
                } else if (unit == MeasureUnit.MEGABYTE) {
                    number = number.doubleValue() * 1024 * 1024;
                } else if (unit == MeasureUnit.GIGABYTE) {
                    number = number.doubleValue() * 1024 * 1024 * 1024;
                } else if (unit == MeasureUnit.TERABYTE) {
                    number = number.doubleValue() * 1024 * 1024 * 1024 * 1024;
                }
                termAtt.setEmpty().append(Long.toString(number.longValue()));
            } else {
                termAtt.setEmpty().append(s);
            }
            return true;
        }
    }
}
