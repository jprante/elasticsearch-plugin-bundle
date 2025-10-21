package org.xbib.opensearch.plugin.bundle.test.index.analysis.sortform;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.index.Index;
import org.opensearch.test.OpenSearchTestCase;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;
import org.xbib.opensearch.plugin.bundle.test.MultiMap;
import org.xbib.opensearch.plugin.bundle.test.TreeMultiMap;

import java.util.Iterator;
import java.util.Set;

/**
 * Sort form tests.
 */
public class SortFormTests extends OpenSearchTokenStreamTestCase {

    public void testBasicUsage() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .put("index.analysis.analyzer.myanalyzer.filter", "sortform")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer myanalyzer = analysis.indexAnalyzers.get("myanalyzer");
        assertAnalyzesTo(myanalyzer, "<<Der>> Titel des Buches", new String[]{"Titel des Buches"});
    }

    public void testUnicodeUsage() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myanalyzer.type", "sortform")
                .put("index.analysis.analyzer.myanalyzer.filter", "sortform")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer myanalyzer = analysis.indexAnalyzers.get("myanalyzer");
        // Unicode 0098: START OF STRING
        // Unicode 009C: STRING TERMINATOR
        assertAnalyzesTo(myanalyzer, "\u0098Der\u009c Titel des Buches", new String[]{"Titel des Buches"});
    }

    public void testFromJson() throws Exception {
        String resource = "sortform.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("german_phonebook_with_sortform");

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
            TokenStream ts = analyzer.tokenStream("test", s);
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

    private BytesRef sortKeyFromTokenStream(TokenStream stream) throws Exception {
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
