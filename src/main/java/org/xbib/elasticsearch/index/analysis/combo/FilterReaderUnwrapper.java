package org.xbib.elasticsearch.index.analysis.combo;

import java.io.FilterReader;
import java.io.Reader;
import java.lang.reflect.Field;

/**
 * A {@link java.io.FilterReader} ReaderUnwrapper that
 * returns the Reader wrapped inside the FilterReader
 * (and all its subclasses)
 */
public class FilterReaderUnwrapper implements ReaderCloneFactory.ReaderUnwrapper<FilterReader> {

    private static Field internalField;

    static {
        try {
            internalField = FilterReader.class.getDeclaredField("in");
            internalField.setAccessible(true);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not give accessibility to private \"in\" field of the given FilterReader", ex);
        }
    }

    public Reader unwrap(FilterReader originalReader) throws IllegalArgumentException {
        try {
            return (Reader) internalField.get(originalReader);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not access private \"in\" field of the given FilterReader (actual class: " + originalReader.getClass().getCanonicalName() + ")", ex);
        }
    }

}
