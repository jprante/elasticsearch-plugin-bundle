package org.xbib.elasticsearch.index.analysis.icu.segmentation;

import org.junit.Test;

import java.text.CharacterIterator;

import static org.junit.Assert.assertEquals;

/**
 */
public class CharArrayIteratorTests {

    @Test
    public void testBasicUsage() {
        CharArrayIterator ci = new CharArrayIterator();
        ci.setText("testing".toCharArray(), 0, "testing".length());
        assertEquals(0, ci.getBeginIndex());
        assertEquals(7, ci.getEndIndex());
        assertEquals(0, ci.getIndex());
        assertEquals('t', ci.current());
        assertEquals('e', ci.next());
        assertEquals('g', ci.last());
        assertEquals('n', ci.previous());
        assertEquals('t', ci.first());
        assertEquals(CharacterIterator.DONE, ci.previous());
    }

    @Test
    public void testFirst() {
        CharArrayIterator ci = new CharArrayIterator();
        ci.setText("testing".toCharArray(), 0, "testing".length());
        ci.next();
        assertEquals('t', ci.first());
        assertEquals(ci.getBeginIndex(), ci.getIndex());
        ci.setText(new char[] {}, 0, 0);
        assertEquals(CharacterIterator.DONE, ci.first());
    }

    @Test
    public void testLast() {
        CharArrayIterator ci = new CharArrayIterator();
        ci.setText("testing".toCharArray(), 0, "testing".length());
        assertEquals('g', ci.last());
        assertEquals(ci.getIndex(), ci.getEndIndex() - 1);
        ci.setText(new char[] {}, 0, 0);
        assertEquals(CharacterIterator.DONE, ci.last());
        assertEquals(ci.getEndIndex(), ci.getIndex());
    }

    @Test
    public void testCurrent() {
        CharArrayIterator ci = new CharArrayIterator();
        ci.setText("testing".toCharArray(), 0, "testing".length());
        assertEquals('t', ci.current());
        ci.last();
        ci.next();
        assertEquals(CharacterIterator.DONE, ci.current());
    }

    @Test
    public void testNext() {
        CharArrayIterator ci = new CharArrayIterator();
        ci.setText("te".toCharArray(), 0, 2);
        assertEquals('e', ci.next());
        assertEquals(1, ci.getIndex());
        assertEquals(CharacterIterator.DONE, ci.next());
        assertEquals(ci.getEndIndex(), ci.getIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIndex() {
        CharArrayIterator ci = new CharArrayIterator();
        ci.setText("test".toCharArray(), 0, "test".length());
        ci.setIndex(5);
    }

    @Test
    public void testClone() {
        char text[] = "testing".toCharArray();
        CharArrayIterator ci = new CharArrayIterator();
        ci.setText(text, 0, text.length);
        ci.next();
        CharArrayIterator ci2 = ci.clone();
        assertEquals(ci.getIndex(), ci2.getIndex());
        assertEquals(ci.next(), ci2.next());
        assertEquals(ci.last(), ci2.last());
    }
}
