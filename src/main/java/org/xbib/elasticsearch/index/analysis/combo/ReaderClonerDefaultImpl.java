package org.xbib.elasticsearch.index.analysis.combo;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Default, memory costly but generic implementation of a {@link java.io.Reader} duplicator.
 * This implementation makes no assumption on the initial Reader.
 * Therefore, only the read() functions are available to figure out
 * what was the original content provided to the initial Reader.
 * After having read and filled a buffer with the whole content,
 * a String-based Reader implementation will be used and returned.
 * This implementation is memory costly because the initial content is
 * forcefully duplicated once. Moreover, buffer size growth may cost
 * some more memory too.
 */
public class ReaderClonerDefaultImpl implements ReaderCloneFactory.ReaderCloner<Reader> {

    public static final int DEFAULT_INITIAL_CAPACITY = 64 * 1024;
    public static final int DEFAULT_READ_BUFFER_SIZE = 16 * 1024;

    protected int initialCapacity;
    protected int readBufferSize;

    private String originalContent;

    public ReaderClonerDefaultImpl() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_READ_BUFFER_SIZE);
    }

    public ReaderClonerDefaultImpl(int initialCapacity) {
        this(initialCapacity, DEFAULT_READ_BUFFER_SIZE);
    }

    /**
     * Extracts the original content from a generic Reader instance
     * by repeatedly calling {@link java.io.Reader#read(char[])} on it,
     * feeding a {@link StringBuilder}.
     *
     * @param initialCapacity Initial StringBuilder capacity
     * @param readBufferSize  Size of the char[] read buffer at each read() call
     */
    public ReaderClonerDefaultImpl(int initialCapacity, int readBufferSize) {
        this.initialCapacity = initialCapacity;
        this.readBufferSize = readBufferSize;
    }

    public void init(Reader originalReader) throws IOException {
        this.originalContent = null;
        StringBuilder sb;
        if (initialCapacity < 0) {
            sb = new StringBuilder();
        } else {
            sb = new StringBuilder(initialCapacity);
        }
        char[] buffer = new char[readBufferSize];
        int read;
        while ((read = originalReader.read(buffer)) != -1) {
            sb.append(buffer, 0, read);
        }
        this.originalContent = sb.toString();
        originalReader.close();
    }

    /**
     * Returns a new {@link java.io.StringReader} instance,
     * directly based on the extracted original content.
     *
     * @return A {@link java.io.StringReader}
     */
    @Override
    public Reader giveAClone() {
        return new StringReader(originalContent);
    }

}
