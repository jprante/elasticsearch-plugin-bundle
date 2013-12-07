
package org.xbib.elasticsearch.index.analysis.combo;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;

/**
 * A ReaderCloner specialized for CharArrayReader.
 * <p/>
 * The only efficient mean of retrieving the original content
 * from a CharArrayReader is to use introspection and access the
 * {@code private String str} field.
 * <p/>
 * Apart from being efficient, this code is also very sensitive
 * to the used JVM implementation.
 * If the introspection does not work, an {@link IllegalArgumentException}
 * is thrown.
 */
public class CharArrayReaderCloner implements ReaderCloneFactory.ReaderCloner<CharArrayReader> {

    private static Field internalField;

    private CharArrayReader original;
    private char[] originalContent;

    static {
        try {
            internalField = CharArrayReader.class.getDeclaredField("buf");
            internalField.setAccessible(true);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not give accessibility to private \"buf\" field of the given CharArrayReader", ex);
        }
    }

    public void init(CharArrayReader originalReader) throws IOException {
        this.original = originalReader;
        this.originalContent = null;
        try {
            this.originalContent = (char[]) internalField.get(original);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not access private \"buf\" field of the given CharArrayReader (actual class: " + original.getClass().getCanonicalName() + ")", ex);
        }
    }

    /**
     * First call will return the original Reader provided.
     */
    public Reader giveAClone() {
        if (original != null) {
            Reader rtn = original;
            original = null; // no longer hold a reference
            return rtn;
        }
        return new CharArrayReader(originalContent);
    }

}
