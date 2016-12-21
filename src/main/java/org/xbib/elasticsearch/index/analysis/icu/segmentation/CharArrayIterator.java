package org.xbib.elasticsearch.index.analysis.icu.segmentation;

import java.text.CharacterIterator;

/**
 * Wraps a char[] as CharacterIterator for processing with a BreakIterator
 */
final class CharArrayIterator implements CharacterIterator {

    private char[] array;

    private int start;

    private int index;

    private int length;

    private int limit;

    public char[] getText() {
        return array;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    /**
     * Set a new region of text to be examined by this iterator
     *
     * @param array  text buffer to examine
     * @param start  offset into buffer
     * @param length maximum length to examine
     */
    void setText(final char[] array, int start, int length) {
        this.array = array;
        this.start = start;
        this.index = start;
        this.length = length;
        this.limit = start + length;
    }

    @Override
    public char current() {
        return (index == limit) ? DONE : array[index];
    }

    @Override
    public char first() {
        index = start;
        return current();
    }

    @Override
    public int getBeginIndex() {
        return 0;
    }

    @Override
    public int getEndIndex() {
        return length;
    }

    @Override
    public int getIndex() {
        return index - start;
    }

    @Override
    public char last() {
        index = (limit == start) ? limit : limit - 1;
        return current();
    }

    @Override
    public char next() {
        if (++index >= limit) {
            index = limit;
            return DONE;
        } else {
            return current();
        }
    }

    @Override
    public char previous() {
        if (--index < start) {
            index = start;
            return DONE;
        } else {
            return current();
        }
    }

    @Override
    public char setIndex(int position) {
        if (position < getBeginIndex() || position > getEndIndex()) {
            throw new IllegalArgumentException("Illegal Position: " + position);
        }
        index = start + position;
        return current();
    }

    @Override
    public CharArrayIterator clone() {
        CharArrayIterator clone = new CharArrayIterator();
        clone.setText(array, start, length);
        clone.index = index;
        return clone;
    }
}
