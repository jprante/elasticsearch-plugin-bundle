package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Normalizer;
import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

/**
 * Normalize token text with ICU {@link com.ibm.icu.text.Normalizer2}.
 * <p>
 * With this filter, you can normalize text in the following ways:
 * <ul>
 * <li> NFKC Normalization, Case Folding, and removing Ignorables (the default)
 * <li> Using a standard Normalization mode (NFC, NFD, NFKC, NFKD)
 * <li> Based on rules from a custom normalization mapping.
 * </ul>
 * <p>
 * If you use the defaults, this filter is a simple way to standardize Unicode text
 * in a language-independent way for search:
 * <ul>
 * <li> The case folding that it does can be seen as a replacement for
 * LowerCaseFilter: For example, it handles cases such as the Greek sigma, so that
 * "Μάϊος" and "ΜΆΪΟΣ" will match correctly.
 * <li> The normalization will standardizes different forms of the same
 * character in Unicode. For example, CJK full-width numbers will be standardized
 * to their ASCII forms.
 * <li> Ignorables such as Zero-Width Joiner and Variation Selectors are removed.
 * These are typically modifier characters that affect display.
 * </ul>
 *
 * @see com.ibm.icu.text.Normalizer2
 * @see com.ibm.icu.text.FilteredNormalizer2
 */
public class IcuNormalizerFilter extends TokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final Normalizer2 normalizer;

    private final StringBuilder buffer = new StringBuilder();

    /**
     * Create a new Normalizer2Filter with the specified Normalizer2
     *
     * @param input      stream
     * @param normalizer normalizer to use
     */
    public IcuNormalizerFilter(TokenStream input, Normalizer2 normalizer) {
        super(input);
        this.normalizer = normalizer;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            if (normalizer.quickCheck(termAtt) != Normalizer.YES) {
                buffer.setLength(0);
                normalizer.normalize(termAtt, buffer);
                termAtt.setEmpty().append(buffer);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IcuNormalizerFilter;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
