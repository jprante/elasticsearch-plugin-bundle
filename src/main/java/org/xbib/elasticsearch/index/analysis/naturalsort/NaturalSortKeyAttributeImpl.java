package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.util.BytesRef;

import java.text.Collator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Natural sort key attribute implementation.
 */
public class NaturalSortKeyAttributeImpl extends CharTermAttributeImpl {

    private static final Pattern numberPattern = Pattern.compile("(\\+|\\-)?([0-9]+)");

    private final Collator collator;

    private final int digits;

    private final int maxTokens;

    public NaturalSortKeyAttributeImpl(Collator collator, int digits, int maxTokens) {
        this.collator = collator;
        this.digits = digits;
        this.maxTokens = maxTokens;
    }

    @Override
    public BytesRef getBytesRef() {
        byte[] collationKey = collator.getCollationKey(natural(toString())).toByteArray();
        final BytesRef ref = this.builder.get();
        ref.bytes = collationKey;
        ref.offset = 0;
        ref.length = collationKey.length;
        return ref;
    }

    private String natural(String s) {
        StringBuffer sb = new StringBuffer();
        Matcher m = numberPattern.matcher(s);
        int foundTokens = 0;
        while (m.find()) {
            int len = m.group(2).length();
            String fmt = "%0" + digits + "d";
            String repl = String.format(Locale.ROOT, fmt, len) + m.group();
            m.appendReplacement(sb, repl);
            foundTokens++;
            if (foundTokens >= maxTokens) {
                break;
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NaturalSortKeyAttributeImpl &&
                collator.equals(((NaturalSortKeyAttributeImpl)object).collator) &&
                Integer.compare(digits, ((NaturalSortKeyAttributeImpl)object).digits) == 0 &&
                Integer.compare(maxTokens, ((NaturalSortKeyAttributeImpl)object).maxTokens) == 0;
    }

    @Override
    public int hashCode() {
        return collator.hashCode() ^ Integer.hashCode(digits) ^ Integer.hashCode(maxTokens);
    }
}
