package org.xbib.elasticsearch.index.analysis.sortform;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;
import org.xbib.util.MultiMap;
import org.xbib.util.TreeMultiMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class SortFormTests extends BaseTokenStreamTest {

    @Test
    public void testBasicUsage() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .put("index.analysis.analyzer.myanalyzer.filter", "sortform")
                .build();
        AnalysisService analysisService = MapperTestUtils.analysisService(settings);
        NamedAnalyzer myanalyzer = analysisService.analyzer("myanalyzer");
        assertAnalyzesTo(myanalyzer, "<<Der>> Titel des Buches", new String[]{"Titel des Buches"});
    }

    @Test
    public void testUnicodeUsage() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .put("index.analysis.analyzer.myanalyzer.filter", "sortform")
                .build();
        AnalysisService analysisService = MapperTestUtils.analysisService(settings);
        Analyzer myanalyzer = analysisService.analyzer("myanalyzer");
        // Unicode 0098: START OF STRING
        // Unicode 009C: STRING TERMINATOR
        assertAnalyzesTo(myanalyzer, "\u0098Der\u009c Titel des Buches", new String[]{"Titel des Buches"});
    }

    @Test
    public void testFromJson() throws Exception {
        AnalysisService analysisService =
                MapperTestUtils.analysisService("org/xbib/elasticsearch/index/analysis/sortform/sortform.json");
        Analyzer analyzer = analysisService.analyzer("german_phonebook_with_sortform").analyzer();

        String[] words = new String[]{
                "¬Frau¬ Göbel",
                "Goethe",
                "¬Dr.¬ Goldmann",
                "Göthe",
                "¬Herr¬ Götz",
                "Groß",
                "Gross"
        };

        MultiMap<BytesRef,String> map = new TreeMultiMap<>();
        for (String s : words) {
            TokenStream ts = analyzer.tokenStream(null, s);
            BytesRef sortKey = sortKeyFromTokenStream(ts);
            map.put(sortKey, s);
        }
        // strength "quaternary" orders without punctuation and ensures unique entries
        Iterator<Set<String>> it = map.values().iterator();
        assertEquals("[¬Frau¬ Göbel]",it.next().toString());
        assertEquals("[Goethe]",it.next().toString());
        assertEquals("[Göthe]",it.next().toString());
        assertEquals("[¬Herr¬ Götz]",it.next().toString());
        assertEquals("[¬Dr.¬ Goldmann]",it.next().toString());
        assertEquals("[Gross]",it.next().toString());
        assertEquals("[Groß]",it.next().toString());
    }

    private BytesRef sortKeyFromTokenStream(TokenStream stream) throws IOException {
        TermToBytesRefAttribute termAttr = stream.getAttribute(TermToBytesRefAttribute.class);
        BytesRefBuilder b = new BytesRefBuilder();
        stream.reset();
        while (stream.incrementToken()) {
            b.append(termAttr.getBytesRef());
        }
        stream.close();
        return b.get();
    }
}
