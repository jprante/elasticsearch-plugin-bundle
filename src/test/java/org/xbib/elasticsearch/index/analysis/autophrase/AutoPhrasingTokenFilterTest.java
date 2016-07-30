package org.xbib.elasticsearch.index.analysis.autophrase;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;

public class AutoPhrasingTokenFilterTest extends Assert {

    @Test
    public void testAutoPhrase() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(Arrays.asList("income tax", "tax refund", "property tax"), false);

        final String input = "what is my income tax refund this year now that my property tax is so high";

        StringReader reader = new StringReader(input);
        final WhitespaceTokenizer in = new WhitespaceTokenizer();
        in.setReader(reader);
        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter(in, phraseSets, false);
        aptf.setReplaceWhitespaceWith('_');
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();
        assertTrue(aptf.incrementToken());
        assertEquals("what", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("income_tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("tax_refund", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("this", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("year", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("now", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("that", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("property_tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("so", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("high", term.toString());
    }

    @Test
    public void testAutoPhraseEmitSingle() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(Arrays.asList(
                "income tax", "tax refund", "property tax"), false);

        final String input = "what is my income tax refund this year now that my property tax is so high";

        StringReader reader = new StringReader(input);
        final WhitespaceTokenizer in = new WhitespaceTokenizer();
        in.setReader(reader);

        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter(in, phraseSets, true);
        aptf.setReplaceWhitespaceWith('_');
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        assertTrue(aptf.incrementToken());
        assertEquals("what", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("income", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("income_tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("tax_refund", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("refund", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("this", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("year", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("now", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("that", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("property", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("property_tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("so", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("high", term.toString());
    }

    @Test
    public void testOverlappingAtBeginning() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(Arrays.asList(
                "new york", "new york city", "city of new york"), false);

        final String input = "new york city is great";

        StringReader reader = new StringReader(input);
        final WhitespaceTokenizer in = new WhitespaceTokenizer();
        in.setReader(reader);

        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter(in, phraseSets, false);
        aptf.setReplaceWhitespaceWith('_');
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        assertTrue(aptf.incrementToken());
        assertEquals("new_york_city", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("great", term.toString());
    }

    @Test
    public void testOverlappingAtBeginningEmitSingle() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(Arrays.asList(
                "new york", "new york city", "city of new york"), false);

        final String input = "new york city is great";

        StringReader reader = new StringReader(input);
        final WhitespaceTokenizer in = new WhitespaceTokenizer();
        in.setReader(reader);

        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter(in, phraseSets, true);
        aptf.setReplaceWhitespaceWith('_');
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        assertTrue(aptf.incrementToken());
        assertEquals("new", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("york", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("new_york", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("new_york_city", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("city", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("great", term.toString());
    }

    @Test
    public void testOverlappingAtEndEmitSingle() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(Arrays.asList(
                "new york", "new york city", "city of new york"), false);

        final String input = "the great city of new york";

        StringReader reader = new StringReader(input);
        final WhitespaceTokenizer in = new WhitespaceTokenizer();
        in.setReader(reader);

        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter(in, phraseSets, true);
        aptf.setReplaceWhitespaceWith('_');
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        assertTrue(aptf.incrementToken());
        assertEquals("the", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("great", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("city", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("of", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("new", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("york", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("city_of_new_york", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("new_york", term.toString());
    }

    @Test
    public void testOverlappingAtEnd() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(Arrays.asList(
                "new york", "new york city", "city of new york"), false);

        final String input = "the great city of new york";

        StringReader reader = new StringReader(input);
        final WhitespaceTokenizer in = new WhitespaceTokenizer();
        in.setReader(reader);

        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter(in, phraseSets, false);
        aptf.setReplaceWhitespaceWith('_');
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        assertTrue(aptf.incrementToken());
        assertEquals("the", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("great", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("city_of_new_york", term.toString());
    }

    @Test
    public void testIncompletePhrase() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"), false);

        final String input = "some new york";

        StringReader reader = new StringReader(input);
        final WhitespaceTokenizer in = new WhitespaceTokenizer();
        in.setReader(reader);

        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter(in, phraseSets, false);
        aptf.setReplaceWhitespaceWith('_');
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        assertTrue(aptf.incrementToken());
        assertEquals("some", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("new", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals("york", term.toString());
    }

}