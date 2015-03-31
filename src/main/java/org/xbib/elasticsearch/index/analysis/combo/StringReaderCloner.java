package org.xbib.elasticsearch.index.analysis.combo;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;

/**
 * A ReaderCloner specialized for StringReader.
 * The only efficient mean of retrieving the original content
 * from a StringReader is to use introspection and access the
 * {@code private String str} field.
 * Apart from being efficient, this code is also very sensitive
 * to the used JVM implementation.
 * If the introspection does not work, an {@link IllegalArgumentException}
 * is thrown.
 */
public class StringReaderCloner implements ReaderCloneFactory.ReaderCloner<StringReader> {

    private static Field internalField;

    private StringReader original;
    private String originalContent;

    static {
        try {
            internalField = StringReader.class.getDeclaredField("str");
            internalField.setAccessible(true);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not give accessibility to private \"str\" field of the given StringReader", ex);
        }
    }

    public void init(StringReader originalReader) throws IOException {
        this.originalContent = null;
        try {
            this.original = originalReader;
            this.originalContent = (String) internalField.get(original);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not access private \"str\" field of the given StringReader (actual class: " + original.getClass().getCanonicalName() + ")", ex);
        }
    }

    /**
     * First call will return the original Reader provided.
     */
    @Override
    public Reader giveAClone() {
        if (original != null) {
            Reader rtn = original;
            original = null; // no longer hold a reference
            return rtn;
        }
        return new StringReader(originalContent);
    }

}
