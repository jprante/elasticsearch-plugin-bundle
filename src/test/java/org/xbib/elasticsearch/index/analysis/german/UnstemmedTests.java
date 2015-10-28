package org.xbib.elasticsearch.index.analysis.german;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.elasticsearch.index.analysis.AnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.AnalyzerTestUtils;

import java.io.IOException;
import java.io.StringReader;

public class UnstemmedTests extends Assert {

    @Test
    public void testOne() throws IOException {
        String source = "Ein Tag in Köln im Café an der Straßenecke mit einer Standard-Nummer ISBN 1-4493-5854-3";
        String[] expected = {
                "tag",
                "koln",
                "cafe",
                "caf",
                "strassenecke",
                "strasseneck",
                "standard-nummer",
                "standardnummer",
                "standard-numm",
                "standardnumm",
                "isbn",
                "1-4493-5854-3",
                "1449358543",
                "978-1-4493-5854-9",
                "9781449358549"
        };
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/german/unstemmed.json");
        Analyzer analyzer = analysisService.analyzer("default");
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    @Test
    public void testTwo() throws IOException {
        String source = "So wird's was: das Elasticsearch-Buch erscheint beim O'Reilly-Verlag.";
        String[] expected = {
                "wird's",
                "elasticsearch-buch",
                "elasticsearchbuch",
                "erscheint",
                "o'reilly-verlag",
                "o'reillyverlag"
        };
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/german/unstemmed.json");
        Analyzer analyzer = analysisService.analyzer("default");
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    @Test
    public void testThree() throws IOException {
        String source = "978-1-4493-5854-9";
        String[] expected = {
             "978-1-4493-5854-9",
             "9781449358549"
        };
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/german/unstemmed.json");
        Analyzer analyzer = analysisService.analyzer("default");
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    @Test
    public void testFour() throws IOException {
        String source = "Prante, Jörg";
        String[] expected = {
                "prante",
                "jorg"
        };
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/german/unstemmed.json");
        Analyzer analyzer = analysisService.analyzer("unstemmed");
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    @Test
    public void testFive() throws IOException {
        String source = "Schroeder";
        String[] expected = {
                "schroder"
        };
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/german/unstemmed.json");
        Analyzer analyzer = analysisService.analyzer("unstemmed");
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    @Test
    public void testSix() throws IOException {
        String source = "Programmieren in C++ für Einsteiger";
        String[] expected = {
                "programmieren",
                "programmi",
                "c++",
                "einsteiger",
                "einsteig"
        };
        AnalysisService analysisService = AnalyzerTestUtils.createAnalysisService("/org/xbib/elasticsearch/index/analysis/german/unstemmed.json");
        Analyzer analyzer = analysisService.analyzer("default");
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
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