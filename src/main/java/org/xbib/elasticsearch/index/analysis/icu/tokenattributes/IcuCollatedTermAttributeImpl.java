package org.xbib.elasticsearch.index.analysis.icu.tokenattributes;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RawCollationKey;

import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.util.BytesRef;

/**
 * Extension of {@link CharTermAttributeImpl} that encodes the term
 * text as a binary Unicode collation key instead of as UTF-8 bytes.
 */
public class IcuCollatedTermAttributeImpl extends CharTermAttributeImpl {
    private final Collator collator;
    private final RawCollationKey key = new RawCollationKey();

    /**
     * Create a new ICUCollatedTermAttributeImpl
     * @param collator Collation key generator
     */
    public IcuCollatedTermAttributeImpl(Collator collator) {
        // clone the collator: see http://userguide.icu-project.org/collation/architecture
        try {
            this.collator = (Collator) collator.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public BytesRef getBytesRef() {
        collator.getRawCollationKey(toString(), key);
        final BytesRef ref = this.builder.get();
        ref.bytes = key.bytes;
        ref.offset = 0;
        ref.length = key.size;
        return ref;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof IcuCollatedTermAttributeImpl &&
                ((IcuCollatedTermAttributeImpl) other).collator == collator;
    }

    @Override
    public int hashCode() {
        return collator.hashCode();
    }

}
