package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;
import java.io.StringReader;

public class HyphenTokenizerTests extends Assert {

    @Test
    public void testOne() throws IOException {

        String source = "Das ist ein Bindestrich-Wort.";

        String[] expected = {
                "Das",
                "ist",
                "ein",
                "Bindestrich-Wort",
                "BindestrichWort",
                "Wort",
                "Bindestrich"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        TokenStream tokenStream = tokenFilter.create(tokenizer);
        assertSimpleTSOutput(tokenStream, expected);
    }

    @Test
    public void testTwo() throws IOException {

        String source = "Das E-Book muss dringend zum Buchbinder.";

        String[] expected = {
                "Das",
                "E-Book",
                "EBook",
                "Book",
                "muss",
                "dringend",
                "zum",
                "Buchbinder"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testThree() throws IOException {

        String source = "Ich will nicht als Service-Center-Mitarbeiterin, sondern 100-prozentig als Dipl.-Ing. arbeiten!";

        String[] expected = {
                "Ich",
                "will",
                "nicht",
                "als",
                "Service-Center-Mitarbeiterin",
                "ServiceCenterMitarbeiterin",
                "Mitarbeiterin",
                "ServiceCenter",
                "ServiceCenter-Mitarbeiterin",
                "Center-Mitarbeiterin",
                "Service",
                "sondern",
                "100-prozentig",
                "als",
                "Dipl.-Ing",
                "arbeiten"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testFour() throws IOException {

        String source = "So wird's was: das Elasticsearch-Buch erscheint beim O'Reilly-Verlag.";

        String[] expected = {
                "So",
                "wird's",
                "was",
                "das",
                "Elasticsearch-Buch",
                "ElasticsearchBuch",
                "Buch",
                "Elasticsearch",
                "erscheint",
                "beim",
                "O'Reilly-Verlag"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
                //AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }


    @Test
    public void testFive() throws IOException {

        String source = "978-1-4493-5854-9";

        String[] expected = {
                "978-1-4493-5854-9"
        };

        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testSix() throws IOException {

        String source = "E-Book";

        String[] expected = {
                "E-Book",
                "EBook",
                "Book"
        };

        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testSeven() throws IOException {
        String source = "Procter & Gamble ist nicht schwarz - weiss";

        String[] expected = {
                "Procter",
                "Gamble",
                "ist",
                "nicht",
                "schwarz",
                "weiss"
        };

        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }


    @Test
    public void testEight() throws IOException {

        String source = "Ich will nicht als Service-Center-Mitarbeiterin, sondern 100-prozentig als Dipl.-Ing. arbeiten!";

        String[] expected = {
                "Ich",
                "will",
                "nicht",
                "als",
                "Service-Center-Mitarbeiterin",
                "ServiceCenterMitarbeiterin",
                "sondern",
                "100-prozentig",
                "100prozentig",
                "als",
                "Dipl.-Ing",
                "Dipl.Ing",
                "arbeiten"
        };
        AnalysisService analysisService =
                MapperTestUtils.analysisService("/org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer_without_subwords.json");
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("my_hyphen_tokenfilter");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
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
