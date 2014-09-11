package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.IOException;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * The hyphen token filter removes hyphens in a token and builds an expanded token list
 * with unhyphenated words and the combined word fragments (decomposition).
 *
 * No word fragment will be created if a word fragment length is 1.
 *
 * It works best with ICU tokenizer on Latin-dont-break-on-hyphens.rbbi, a tokenizer which preserves hyphens in words.
 *
 * This is useful for german language analysis, where words in texts are often composed by adding hyphens between
 * words.
 *
 * See also <a href="http://de.wikipedia.org/wiki/Viertelgeviertstrich">Viertelgeviertstrich</a>
 *
 * Examples:
 *
 * Bindestrich-Wort =>
 *     Bindestrich-Wort, BindestrichWort, Wort, Bindestrich
 *
 * E-Book =>
 *     E-Book, EBook, Book
 *
 * Service-Center-Mitarbeiterin =>
 *    Service-Center-Mitarbeiterin,
 *    ServiceCenterMitarbeiterin,
 *    Mitarbeiterin,
 *    ServiceCenter,
 *    ServiceCenter-Mitarbeiterin,
 *    Center-Mitarbeiterin,
 *    Service
 *
 * Auf-die-lange-Bank-Schieben =>
 *     Auf, Aufdie-lange-Bank-Schieben,
 *     Aufdie, Aufdielange-Bank-Schieben,
 *     Aufdielange, AufdielangeBank-Schieben,
 *     AufdielangeBank, AufdielangeBankSchieben
 *
 */
public class HyphenTokenFilter extends TokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    private final static Pattern punctPattern = Pattern.compile("[\\p{IsPunct}]", Pattern.UNICODE_CHARACTER_CLASS);

    private Stack<String> stack;

    private AttributeSource.State current;

    protected HyphenTokenFilter(TokenStream input) {
        super(input);
        stack = new Stack<String>();
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!stack.isEmpty()) {
            String synonym = stack.pop();
            restoreState(current);
            termAtt.setEmpty().append(synonym);
            posIncrAtt.setPositionIncrement(0);
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        if (addToStack()) {
            current = captureState();
        }
        return true;
    }

    private boolean addToStack() throws IOException {
        String s = termAtt.toString();
        int pos = s.indexOf('-');
        if (pos <= 0) {
            return false;
        }
        String head = "";
        String tail;
        while (pos > 0) {
            head = head + s.substring(0, pos);
            tail = s.substring(pos+1);
            String cleanHead = punctPattern.matcher(head).replaceAll("");
            if (cleanHead.length() > 1) {
                stack.push(cleanHead);
            }
            stack.push(tail);
            stack.push(cleanHead + tail);
            s = tail;
            pos = s.indexOf('-');
        }
        return true;
    }

}