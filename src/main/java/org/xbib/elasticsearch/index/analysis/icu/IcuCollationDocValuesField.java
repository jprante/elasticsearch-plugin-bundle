package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RawCollationKey;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.util.BytesRef;

/**
 * Indexes collation keys as a single-valued {@link SortedDocValuesField}.
 * This is more efficient that {@link IcuCollationKeyAnalyzer} if the field
 * only has one value: no un-inversion is necessary to sort on the field,
 * locale-sensitive range queries can still work via {@code DocValuesRangeQuery},
 * and the underlying data structures built at index-time are likely more efficient
 * and use less memory than FieldCache.
 */
public final class IcuCollationDocValuesField extends Field {
    private final String name;
    private final Collator collator;
    private final BytesRef bytes = new BytesRef();
    private final RawCollationKey key = new RawCollationKey();

    /**
     * Create a new ICUCollationDocValuesField instance.
     * Do not create a new one for each document, instead
     * just make one and reuse it during your indexing process, setting
     * the value via {@link #setStringValue(String)}.
     *
     * @param name     field name
     * @param collator Collator for generating collation keys.
     */
    public IcuCollationDocValuesField(String name, Collator collator) {
        super(name, SortedDocValuesField.TYPE);
        this.name = name;
        try {
            this.collator = (Collator) collator.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e.getMessage(), e);
        }
        fieldsData = bytes;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void setStringValue(String value) {
        collator.getRawCollationKey(value, key);
        bytes.bytes = key.bytes;
        bytes.offset = 0;
        bytes.length = key.size;
    }
}
