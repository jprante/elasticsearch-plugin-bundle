package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.StringReader;

/**
 * ICU transform filter tests.
 */
public class IcuTransformFilterTests extends OpenSearchTokenStreamTestCase {

    public void testTransformTraditionalSimplified() throws Exception {
        String source = "簡化字";
        String[] expected =  new String[] { "简化", "字" };
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_ch").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_ch");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTransformHanLatin() throws Exception {
        String source = "中国";
        String[] expected =  new String[] { "zhōng guó" };
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_han").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_han");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTransformKatakanaHiragana() throws Exception {
        String source = "ヒラガナ";
        String[] expected =  new String[] { "ひらがな" };
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_katakana").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_katakana");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTransformCyrillicLatin() throws Exception {
        String source = "Российская Федерация";
        String[] expected = new String[] { "Rossijskaâ", "Federaciâ" };
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_cyr").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_cyr");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTransformCyrillicLatinReverse() throws Exception {
        String source = "Rossijskaâ Federaciâ";
        String[] expected = new String[] { "Российская", "Федерация"};
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_cyr").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_cyr_reverse");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTransformAnyLatin() throws Exception {
        String source = "Αλφαβητικός Κατάλογος";
        String[] expected = new String[] { "Alphabētikós", "Katálogos" };
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_any_latin").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_any_latin");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTransformNFD() throws Exception {
        String source = "Alphabētikós Katálogos";
        String[] expected = new String[] { "Alphabetikos", "Katalogos" };
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_nfd").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_nfd");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }

    public void testTransformRules() throws Exception {
        String source = "abacadaba";
        String[] expected = new String[] { "bcbcbdbcb" };
        String resource = "icu_transform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = analysis.tokenizer.get("my_icu_tokenizer_rules").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("my_icu_transformer_rules");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertTokenStreamContents(tokenStream, expected);
    }
}
