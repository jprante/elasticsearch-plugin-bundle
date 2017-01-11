package org.xbib.elasticsearch.index.analysis.icu;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.*;
import static org.xbib.elasticsearch.MapperTestUtils.tokenFilterFactory;
import static org.xbib.elasticsearch.MapperTestUtils.tokenizerFactory;

/**
 */
public class IcuTransformFilterTests {

    @Test
    public void testTransformTraditionalSimplified() throws IOException {
        String source = "簡化字";
        String[] expected =  new String[] { "简化", "字" };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_ch").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_ch");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTransformHanLatin() throws IOException {
        String source = "中国";
        String[] expected =  new String[] { "zhōng guó" };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_han").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_han");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTransformKatakanaHiragana() throws IOException {
        String source = "ヒラガナ";
        String[] expected =  new String[] { "ひらがな" };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_katakana").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_katakana");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTransformCyrillicLatin() throws IOException {
        String source = "Российская Федерация";
        String[] expected = new String[] { "Rossijskaâ", "Federaciâ" };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_cyr").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_cyr");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTransformCyrillicLatinReverse() throws IOException {
        String source = "Rossijskaâ Federaciâ";
        String[] expected = new String[] { "Российская", "Федерация"};
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_cyr").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_cyr_reverse");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTransformAnyLatin() throws IOException {
        String source = "Αλφαβητικός Κατάλογος";
        String[] expected = new String[] { "Alphabētikós", "Katálogos" };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_any_latin").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_any_latin");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTransformNFD() throws IOException {
        String source = "Alphabētikós Katálogos";
        String[] expected = new String[] { "Alphabetikos", "Katalogos" };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_nfd").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_nfd");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTransformRules() throws IOException {
        String source = "abacadaba";
        String[] expected = new String[] { "bcbcbdbcb" };
        String resource = "org/xbib/elasticsearch/index/analysis/icu/icu_transform.json";
        Tokenizer tokenizer = tokenizerFactory(resource,"my_icu_tokenizer_rules").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = tokenFilterFactory(resource, "my_icu_transformer_rules");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            assertTrue(i < expected.length);
            assertEquals(expected[i], termAttr.toString());
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }
}
