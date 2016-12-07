package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
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
 * It works best with the hyphen tokenizer, a tokenizer which preserves hyphens (and other sperataors) in words.
 *
 * This is useful for german language analysis, where words in texts are often composed by adding hyphens between
 * words.
 *
 * See also <a href="http://de.wikipedia.org/wiki/Viertelgeviertstrich">Viertelgeviertstrich</a>
 *
 * Examples:
 *
 * Bindestrich-Wort =&gt;
 * Bindestrich-Wort, BindestrichWort, Wort, Bindestrich
 *
 * E-Book =&gt;
 * E-Book, EBook, Book
 *
 * Service-Center-Mitarbeiterin =&gt;
 * Service-Center-Mitarbeiterin,
 * ServiceCenterMitarbeiterin,
 * Mitarbeiterin,
 * ServiceCenter,
 * ServiceCenter-Mitarbeiterin,
 * Center-Mitarbeiterin,
 * Service
 */
public class HyphenTokenFilter extends TokenFilter {

    final static char[] HYPHEN = new char[]{'-'};
    // TODO use TypeAttribute, LETTER_COMP or something
    private final static Pattern letter = Pattern.compile("\\p{L}+", Pattern.UNICODE_CHARACTER_CLASS);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    private final Stack<String> stack;

    private final char[] hyphenchars;

    private final boolean subwords;

    private final boolean respectKeywords;

    private AttributeSource.State current;

    protected HyphenTokenFilter(TokenStream input, char[] hyphenchars, boolean subwords, boolean respectKeywords) {
        super(input);
        this.stack = new Stack<>();
        this.hyphenchars = hyphenchars;
        this.subwords = subwords;
        this.respectKeywords = respectKeywords;
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
        if (respectKeywords && keywordAtt.isKeyword()) {
            return true;
        }
        if (addToStack()) {
            current = captureState();
        }
        return true;
    }

    private boolean addToStack() throws IOException {
        for (char ch : hyphenchars) {
            String s = termAtt.toString();
            int pos = s.indexOf(ch);
            if (pos <= 0) {
                continue;
            }
            if (subwords) {
                String head = "";
                String tail;
                while (pos > 0) {
                    head = head + s.substring(0, pos);
                    tail = s.substring(pos + 1);
                    // only words, no numbers
                    if (letter.matcher(head).matches()) {
                        if (head.length() > 1) {
                            stack.push(head);
                        }
                        stack.push(tail);
                        stack.push(head + tail);
                    }
                    s = tail;
                    pos = s.indexOf(ch);
                }
            } else {
                stack.push(s.replaceAll(Pattern.quote(Character.toString(ch)), ""));
            }
        }
        return !stack.isEmpty();
    }

}