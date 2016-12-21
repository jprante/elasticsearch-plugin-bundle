package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Replaceable;
import com.ibm.icu.text.Transliterator;
import com.ibm.icu.text.UTF16;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;

/**
 * A {@link TokenFilter} that transforms text with ICU.
 * <p>
 * ICU provides text-transformation functionality via its Transliteration API.
 * Although script conversion is its most common use, a Transliterator can
 * actually perform a more general class of tasks. In fact, Transliterator
 * defines a very general API which specifies only that a segment of the input
 * text is replaced by new text. The particulars of this conversion are
 * determined entirely by subclasses of Transliterator.
 * </p>
 * <p>
 * Some useful transformations for search are built-in:
 * <ul>
 * <li>Conversion from Traditional to Simplified Chinese characters
 * <li>Conversion from Hiragana to Katakana
 * <li>Conversion from Fullwidth to Halfwidth forms.
 * <li>Script conversions, for example Serbian Cyrillic to Latin
 * </ul>
 * <p>
 * Example usage: <blockquote>stream = new ICUTransformFilter(stream,
 * Transliterator.getInstance("Traditional-Simplified"));</blockquote>
 * <br>
 * For more details, see the <a
 * href="http://userguide.icu-project.org/transforms/general">ICU User
 * Guide</a>.
 */
public final class IcuTransformTokenFilter extends TokenFilter {

    private final Transliterator transform;

    private final Transliterator.Position position = new Transliterator.Position();

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final ReplaceableTermAttribute replaceableAttribute = new ReplaceableTermAttribute();

    /**
     * Create a new IcuTransformFilter that transforms text on the given stream.
     *
     * @param input {@link TokenStream} to filter.
     * @param transform Transliterator to transform the text.
     */
    public IcuTransformTokenFilter(TokenStream input, Transliterator transform) {
        super(input);
        this.transform = transform;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            replaceableAttribute.setText(termAtt);
            int length = termAtt.length();
            position.start = 0;
            position.limit = length;
            position.contextStart = 0;
            position.contextLimit = length;
            transform.filteredTransliterate(replaceableAttribute, position, false);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Wrap a {@link CharTermAttribute} with the Replaceable API.
     */
    final class ReplaceableTermAttribute implements Replaceable {
        private char buffer[];
        private int length;
        private CharTermAttribute token;

        void setText(final CharTermAttribute token) {
            this.token = token;
            this.buffer = token.buffer();
            this.length = token.length();
        }

        @Override
        public int char32At(int pos) {
            return UTF16.charAt(buffer, 0, length, pos);
        }

        @Override
        public char charAt(int pos) {
            return buffer[pos];
        }

        @Override
        public void copy(int start, int limit, int dest) {
            char text[] = new char[limit - start];
            getChars(start, limit, text, 0);
            replace(dest, dest, text, 0, limit - start);
        }

        @Override
        public void getChars(int srcStart, int srcLimit, char[] dst, int dstStart) {
            System.arraycopy(buffer, srcStart, dst, dstStart, srcLimit - srcStart);
        }

        @Override
        public boolean hasMetaData() {
            return false;
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public void replace(int start, int limit, String text) {
            int charsLen = text.length();
            final int newLength = shiftForReplace(start, limit, charsLen);
            text.getChars(0, charsLen, buffer, start);
            token.setLength(length = newLength);
        }

        @Override
        public void replace(int start, int limit, char[] text, int charsStart, int charsLen) {
            int newLength = shiftForReplace(start, limit, charsLen);
            System.arraycopy(text, charsStart, buffer, start, charsLen);
            token.setLength(length = newLength);
        }

        private int shiftForReplace(int start, int limit, int charsLen) {
            int replacementLength = limit - start;
            int newLength = length - replacementLength + charsLen;
            if (newLength > length) {
                buffer = token.resizeBuffer(newLength);
            }
            if (replacementLength != charsLen && limit < length) {
                System.arraycopy(buffer, limit, buffer, start + charsLen, length - limit);
            }
            return newLength;
        }
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IcuTransformTokenFilter &&
                transform.equals(((IcuTransformTokenFilter)object).transform);
    }

    @Override
    public int hashCode() {
        return transform.hashCode();
    }
}
