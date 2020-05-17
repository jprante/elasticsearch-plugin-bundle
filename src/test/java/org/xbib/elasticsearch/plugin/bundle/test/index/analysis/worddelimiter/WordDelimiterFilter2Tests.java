package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.worddelimiter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.LuceneTestCase;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.worddelimiter.WordDelimiterFilter2;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.worddelimiter.WordDelimiterIterator;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.apache.lucene.analysis.standard.ClassicAnalyzer.STOP_WORDS_SET;

/**
 * Word delimiter filter 2 tests.
 */
@LuceneTestCase.AwaitsFix(bugUrl = "fix term order for not going back in offset")
public class WordDelimiterFilter2Tests extends ESTokenStreamTestCase {

    public void testOffsets() throws Exception {
        String resource = "worddelimiter.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = keywordMockTokenizer(new StringReader("foo-bar"));
        TokenStream ts = analysis.tokenFilter.get("wd").create(tokenizer);
        assertTokenStreamContents(ts,
                new String[]{"foo", "bar", "foobar"},
                new int[]{0, 4, 0},
                new int[]{3, 7, 7},
                null, null, null, null, false);
    }

    public void testOffsetChange() throws Exception {
        String resource = "worddelimiter.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Tokenizer tokenizer = keywordMockTokenizer(new StringReader("übelkeit"));
        TokenStream ts = analysis.tokenFilter.get("wd").create(tokenizer);
        assertTokenStreamContents(ts,
                new String[]{"übelkeit" },
                new int[]{0},
                new int[]{8});
    }

    public void doSplit(final String input, String... output) throws Exception {
        int flags = WordDelimiterFilter2.GENERATE_WORD_PARTS |
                WordDelimiterFilter2.GENERATE_NUMBER_PARTS |
                WordDelimiterFilter2.SPLIT_ON_CASE_CHANGE |
                WordDelimiterFilter2.SPLIT_ON_NUMERICS |
                WordDelimiterFilter2.STEM_ENGLISH_POSSESSIVE;
        Tokenizer tokenizer = new MockTokenizer(MockTokenizer.KEYWORD, false);
        tokenizer.setReader(new StringReader(input));
        WordDelimiterFilter2 wdf = new WordDelimiterFilter2(tokenizer,
                WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE, flags, null);
        assertTokenStreamContents(wdf, output);
    }

    public void testSplits() throws Exception {
        doSplit("basic-split", "basic", "split");
        doSplit("camelCase", "camel", "Case");

        // non-space marking symbol shouldn't cause split
        // this is an example in Thai
        doSplit("\u0e1a\u0e49\u0e32\u0e19", "\u0e1a\u0e49\u0e32\u0e19");
        // possessive followed by delimiter
        doSplit("test's'", "test");

        // some russian upper and lowercase
        doSplit("Роберт", "Роберт");
        // now cause a split (russian camelCase)
        doSplit("РобЕрт", "Роб", "Ерт");

        // a composed titlecase character, don't split
        doSplit("aǅungla", "aǅungla");

        // a modifier letter, don't split
        doSplit("ســـــــــــــــــلام", "ســـــــــــــــــلام");

        // enclosing mark, don't split
        doSplit("test⃝", "test⃝");

        // combining spacing mark (the virama), don't split
        doSplit("हिन्दी", "हिन्दी");

        // don't split non-ascii digits
        doSplit("١٢٣٤", "١٢٣٤");

        // don't split supplementaries into unpaired surrogates
        doSplit("𠀀𠀀", "𠀀𠀀");
    }

    public void doSplitPossessive(int stemPossessive, final String input, final String... output) throws Exception {
        int flags = WordDelimiterFilter2.GENERATE_WORD_PARTS |
                WordDelimiterFilter2.GENERATE_NUMBER_PARTS |
                WordDelimiterFilter2.SPLIT_ON_CASE_CHANGE |
                WordDelimiterFilter2.SPLIT_ON_NUMERICS;
        flags |= (stemPossessive == 1) ? WordDelimiterFilter2.STEM_ENGLISH_POSSESSIVE : 0;
        Tokenizer tokenizer = new MockTokenizer(MockTokenizer.KEYWORD, false);
        tokenizer.setReader(new StringReader(input));
        WordDelimiterFilter2 wdf = new WordDelimiterFilter2(tokenizer, flags, null);
        assertTokenStreamContents(wdf, output);
    }

    /*
     * Test option that allows disabling the special "'s" stemming, instead treating the single quote like other delimiters.
     */
    public void testPossessives() throws Exception {
        doSplitPossessive(1, "ra's", "ra");
        doSplitPossessive(0, "ra's", "ra", "s");
    }

    /*
     * Set a large position increment gap of 10 if the token is "largegap" or "/"
     */
    private final class LargePosIncTokenFilter extends TokenFilter {
        private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
        private PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

        LargePosIncTokenFilter(TokenStream input) {
            super(input);
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (input.incrementToken()) {
                if (termAtt.toString().equals("largegap") || termAtt.toString().equals("/")) {
                    posIncAtt.setPositionIncrement(10);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public void testPositionIncrements() throws Exception {
        final int flags = WordDelimiterFilter2.GENERATE_WORD_PARTS |
                WordDelimiterFilter2.GENERATE_NUMBER_PARTS |
                WordDelimiterFilter2.CATENATE_ALL |
                WordDelimiterFilter2.SPLIT_ON_CASE_CHANGE |
                WordDelimiterFilter2.SPLIT_ON_NUMERICS |
                WordDelimiterFilter2.STEM_ENGLISH_POSSESSIVE;
        final Set<String> protWords = new HashSet<String>(Collections.singletonList("NUTCH"));

    /* analyzer that uses whitespace + wdf */
        Analyzer a = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(
                        tokenizer,
                        flags, protWords));
            }
        };

    /* in this case, works as expected. */
        assertAnalyzesTo(a, "LUCENE / SOLR", new String[]{"LUCENE", "SOLR" },
                new int[]{0, 9},
                new int[]{6, 13},
                null,
                new int[]{1, 1},
                null,
                false);

    /* only in this case, posInc of 2 ?! */
        assertAnalyzesTo(a, "LUCENE / solR", new String[]{"LUCENE", "sol", "R", "solR" },
                new int[]{0, 9, 12, 9},
                new int[]{6, 12, 13, 13},
                null,
                new int[]{1, 1, 1, 0},
                null,
                false);

        assertAnalyzesTo(a, "LUCENE / NUTCH SOLR", new String[]{"LUCENE", "NUTCH", "SOLR" },
                new int[]{0, 9, 15},
                new int[]{6, 14, 19},
                null,
                new int[]{1, 1, 1},
                null,
                false);

        assertAnalyzesTo(a, "LUCENE4.0.0", new String[]{"LUCENE", "4", "0", "0", "LUCENE400" },
                new int[]{0, 6, 8, 10, 0},
                new int[]{6, 7, 9, 11, 11},
                null,
                new int[]{1, 1, 1, 1, 0},
                null,
                false);

    /* analyzer that will consume tokens with large position increments */
        Analyzer a2 = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(
                        new LargePosIncTokenFilter(tokenizer),
                        flags, protWords));
            }
        };

    /* increment of "largegap" is preserved */
        assertAnalyzesTo(a2, "LUCENE largegap SOLR", new String[]{"LUCENE", "largegap", "SOLR" },
                new int[]{0, 7, 16},
                new int[]{6, 15, 20},
                null,
                new int[]{1, 10, 1},
                null,
                false);

    /* the "/" had a position increment of 10, where did it go?!?!! */
        assertAnalyzesTo(a2, "LUCENE / SOLR", new String[]{"LUCENE", "SOLR" },
                new int[]{0, 9},
                new int[]{6, 13},
                null,
                new int[]{1, 11},
                null,
                false);

    /* in this case, the increment of 10 from the "/" is carried over */
        assertAnalyzesTo(a2, "LUCENE / solR", new String[]{"LUCENE", "sol", "R", "solR" },
                new int[]{0, 9, 12, 9},
                new int[]{6, 12, 13, 13},
                null,
                new int[]{1, 11, 1, 0},
                null,
                false);

        assertAnalyzesTo(a2, "LUCENE / NUTCH SOLR", new String[]{"LUCENE", "NUTCH", "SOLR" },
                new int[]{0, 9, 15},
                new int[]{6, 14, 19},
                null,
                new int[]{1, 11, 1},
                null,
                false);

        Analyzer a3 = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                StopFilter filter = new StopFilter(tokenizer, STOP_WORDS_SET);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(filter, flags, protWords));
            }
        };

        assertAnalyzesTo(a3, "lucene.solr",
                new String[]{"lucene", "solr", "lucenesolr" },
                new int[]{0, 7, 0},
                new int[]{6, 11, 11},
                null,
                new int[]{1, 1, 0},
                null,
                false);

    /* the stopword should add a gap here */
        assertAnalyzesTo(a3, "the lucene.solr",
                new String[]{"lucene", "solr", "lucenesolr" },
                new int[]{4, 11, 4},
                new int[]{10, 15, 15},
                null,
                new int[]{2, 1, 0},
                null,
                false);

        final int flags4 = flags | WordDelimiterFilter2.CATENATE_WORDS;
        Analyzer a4 = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                StopFilter filter = new StopFilter(tokenizer, STOP_WORDS_SET);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(filter, flags4, protWords));
            }
        };
        assertAnalyzesTo(a4, "LUCENE4.0.0", new String[]{"LUCENE", "4", "0", "0", "LUCENE400" },
                new int[]{0, 6, 8, 10, 0},
                new int[]{6, 7, 9, 11, 11},
                null,
                new int[]{1, 1, 1, 1, 0},
                null,
                false);
    }

    public void testPositionIncrementsCollapsePositions() throws Exception {
        final int flags = WordDelimiterFilter2.GENERATE_WORD_PARTS |
                WordDelimiterFilter2.GENERATE_NUMBER_PARTS |
                WordDelimiterFilter2.CATENATE_ALL |
                WordDelimiterFilter2.SPLIT_ON_CASE_CHANGE |
                WordDelimiterFilter2.SPLIT_ON_NUMERICS |
                WordDelimiterFilter2.STEM_ENGLISH_POSSESSIVE |
                WordDelimiterFilter2.ALL_PARTS_AT_SAME_POSITION;
        final Set<String> protWords = new HashSet<>(Collections.singletonList("NUTCH"));

    /* analyzer that uses whitespace + wdf */
        Analyzer a = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(
                        tokenizer,
                        flags, protWords));
            }
        };

    /* in this case, works as expected. */
        assertAnalyzesTo(a, "LUCENE / SOLR", new String[]{"LUCENE", "SOLR" },
                new int[]{0, 9},
                new int[]{6, 13},
                null,
                new int[]{1, 1});

    /* only in this case, posInc of 2 ?! */
        assertAnalyzesTo(a, "LUCENE / solR", new String[]{"LUCENE", "sol", "R", "solR" },
                new int[]{0, 9, 12, 9},
                new int[]{6, 12, 13, 13},
                null,
                new int[]{1, 1, 0, 0},
                null,
                false);

        assertAnalyzesTo(a, "LUCENE / NUTCH SOLR", new String[]{"LUCENE", "NUTCH", "SOLR" },
                new int[]{0, 9, 15},
                new int[]{6, 14, 19},
                null,
                new int[]{1, 1, 1});

        assertAnalyzesTo(a, "LUCENE4.0.0", new String[]{"LUCENE", "4", "0", "0", "LUCENE400" },
                new int[]{0, 6, 8, 10, 0},
                new int[]{6, 7, 9, 11, 11},
                null,
                new int[]{1, 0, 0, 0, 0},
                null,
                false);

    /* analyzer that will consume tokens with large position increments */
        Analyzer a2 = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(
                        new LargePosIncTokenFilter(tokenizer),
                        flags, protWords));
            }
        };

    /* increment of "largegap" is preserved */
        assertAnalyzesTo(a2, "LUCENE largegap SOLR", new String[]{"LUCENE", "largegap", "SOLR" },
                new int[]{0, 7, 16},
                new int[]{6, 15, 20},
                null,
                new int[]{1, 10, 1});

    /* the "/" had a position increment of 10, where did it go?!?!! */
        assertAnalyzesTo(a2, "LUCENE / SOLR", new String[]{"LUCENE", "SOLR" },
                new int[]{0, 9},
                new int[]{6, 13},
                null,
                new int[]{1, 11});

    /* in this case, the increment of 10 from the "/" is carried over */
        assertAnalyzesTo(a2, "LUCENE / solR", new String[]{"LUCENE", "sol", "R", "solR" },
                new int[]{0, 9, 12, 9},
                new int[]{6, 12, 13, 13},
                null,
                new int[]{1, 11, 0, 0},
                null,
                false);

        assertAnalyzesTo(a2, "LUCENE / NUTCH SOLR", new String[]{"LUCENE", "NUTCH", "SOLR" },
                new int[]{0, 9, 15},
                new int[]{6, 14, 19},
                null,
                new int[]{1, 11, 1});

        Analyzer a3 = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                StopFilter filter = new StopFilter(tokenizer, STOP_WORDS_SET);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(filter, flags, protWords));
            }
        };

        assertAnalyzesTo(a3, "lucene.solr",
                new String[]{"lucene", "solr", "lucenesolr" },
                new int[]{0, 7, 0},
                new int[]{6, 11, 11},
                null,
                new int[]{1, 0, 0},
                null,
                false);

    /* the stopword should add a gap here */
        assertAnalyzesTo(a3, "the lucene.solr",
                new String[]{"lucene", "solr", "lucenesolr" },
                new int[]{4, 11, 4},
                new int[]{10, 15, 15},
                null,
                new int[]{2, 0, 0},
                null,
                false);

        final int flags4 = flags | WordDelimiterFilter2.CATENATE_WORDS;
        Analyzer a4 = new Analyzer() {
            @Override
            public TokenStreamComponents createComponents(String field) {
                Tokenizer tokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
                StopFilter filter = new StopFilter(tokenizer, STOP_WORDS_SET);
                return new TokenStreamComponents(tokenizer, new WordDelimiterFilter2(filter, flags4, protWords));
            }
        };
        assertAnalyzesTo(a4, "LUCENE4.0.0", new String[]{"LUCENE", "4", "0", "0", "LUCENE400" },
                new int[]{0, 6, 8, 10, 0},
                new int[]{6, 7, 9, 11, 11},
                null,
                new int[]{1, 0, 0, 0, 0},
                null,
                false);
    }
}
