package org.xbib.opensearch.plugin.bundle.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.regex.Pattern;

/**
 * The hyphen token filter removes hyphens in a token and builds an expanded token list
 * with unhyphenated words and the combined word fragments (decomposition).
 * <p>
 * No word fragment will be created if a word fragment length is 1.
 * <p>
 * It works best with the hyphen tokenizer, a tokenizer which preserves hyphens (and other sperataors) in words.
 * <p>
 * This is useful for german language analysis, where words in texts are often composed by adding hyphens between
 * words.
 * <p>
 * See also <a href="http://de.wikipedia.org/wiki/Viertelgeviertstrich">Viertelgeviertstrich</a>
 * <p>
 * Examples:
 * <p>
 * Bindestrich-Wort =&gt;
 * Bindestrich-Wort, BindestrichWort, Wort, Bindestrich
 * <p>
 * E-Book =&gt;
 * E-Book, EBook, Book
 * <p>
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

    static final char[] HYPHEN = {'-'};
    // TODO(jprante) use TypeAttribute, LETTER_COMP or something
    private static final Pattern letter = Pattern.compile("\\p{L}+", Pattern.UNICODE_CHARACTER_CLASS);
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    private final Deque<String> stack;

    private final char[] hyphenchars;

    private final boolean subwords;

    private final boolean respectKeywords;

    private State current;

    protected HyphenTokenFilter(TokenStream input, char[] hyphenchars, boolean subwords, boolean respectKeywords) {
        super(input);
        this.stack = new ArrayDeque<>();
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
                StringBuilder head = new StringBuilder();
                String tail;
                while (pos > 0) {
                    head.append(s.substring(0, pos));
                    tail = s.substring(pos + 1);
                    // only words, no numbers
                    if (letter.matcher(head).matches()) {
                        if (head.length() > 1) {
                            stack.push(head.toString());
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

    @Override
    public boolean equals(Object object) {
        return object instanceof HyphenTokenFilter &&
                Arrays.equals(hyphenchars, ((HyphenTokenFilter) object).hyphenchars) &&
                Boolean.compare(subwords, ((HyphenTokenFilter) object).subwords) == 0 &&
                Boolean.compare(respectKeywords, ((HyphenTokenFilter) object).respectKeywords) == 0;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hyphenchars) ^ Boolean.hashCode(subwords) ^ Boolean.hashCode(respectKeywords);
    }
}
