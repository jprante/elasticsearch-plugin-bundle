package org.xbib.elasticsearch.index.analysis.combo;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.reflect.Field;

/**
 * A {@link java.io.BufferedReader} ReaderUnwrapper that
 * returns the Reader wrapped inside the BufferReader.
 */
public class BufferedReaderUnwrapper implements ReaderCloneFactory.ReaderUnwrapper<BufferedReader> {

    private static Field internalField;

    static {
        try {
            internalField = BufferedReader.class.getDeclaredField("in");
            internalField.setAccessible(true);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not give accessibility to private \"in\" field of the given BufferedReader", ex);
        }
    }

    public Reader unwrap(BufferedReader originalReader) throws IllegalArgumentException {
        try {
            return (Reader) internalField.get(originalReader);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not access private \"in\" field of the given BufferedReader (actual class: " + originalReader.getClass().getCanonicalName() + ")", ex);
        }
    }

}
