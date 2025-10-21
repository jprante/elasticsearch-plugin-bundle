package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;
import com.ibm.icu.util.ULocale;
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
 * ICU collation analyzer tests.
 */
public class IcuCollationAnalyzerTests extends OpenSearchTokenStreamTestCase {

    /*
    * Turkish has some funny casing.
    * This test shows how you can solve this kind of thing easily with collation.
    * Instead of using LowerCaseFilter, use a turkish collator with primary strength.
    * Then things will sort and match correctly.
    */
    public void testBasicUsage() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "tr")
                .put("index.analysis.analyzer.myAnalyzer.strength", "primary")
                .put("index.analysis.analyzer.myAnalyzer.decomposition", "canonical")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");
        TokenStream tsUpper = analyzer.tokenStream(null, "I WİLL USE TURKİSH CASING");
        BytesRef b1 = bytesFromTokenStream(tsUpper);
        TokenStream tsLower = analyzer.tokenStream(null, "ı will use turkish casıng");
        BytesRef b2 = bytesFromTokenStream(tsLower);
        assertTrue(compare(b1.bytes, b2.bytes) == 0);
    }

    /*
    * Test usage of the decomposition option for unicode normalization.
    */
    public void testNormalization() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "tr")
                .put("index.analysis.analyzer.myAnalyzer.strength", "primary")
                .put("index.analysis.analyzer.myAnalyzer.decomposition", "canonical")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");
        TokenStream tsUpper = analyzer.tokenStream(null, "I W\u0049\u0307LL USE TURKİSH CASING");
        BytesRef b1 = bytesFromTokenStream(tsUpper);
        TokenStream tsLower = analyzer.tokenStream(null, "ı will use turkish casıng");
        BytesRef b2 = bytesFromTokenStream(tsLower);
        assertTrue(compare(b1.bytes, b2.bytes) == 0);
    }

    /*
    * Test secondary strength, for english case is not significant.
    */
    public void testSecondaryStrength() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "en")
                .put("index.analysis.analyzer.myAnalyzer.strength", "secondary")
                .put("index.analysis.analyzer.myAnalyzer.decomposition", "no")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");
        TokenStream tsUpper = analyzer.tokenStream("content", "TESTING");
        BytesRef b1 = bytesFromTokenStream(tsUpper);
        TokenStream tsLower = analyzer.tokenStream("content", "testing");
        BytesRef b2 = bytesFromTokenStream(tsLower);
        assertTrue(compare(b1.bytes, b2.bytes) == 0);
    }

    /*
    * Setting alternate=shifted to shift whitespace, punctuation and symbols
    * to quaternary level
    */
    public void testIgnorePunctuation() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "en")
                .put("index.analysis.analyzer.myAnalyzer.strength", "primary")
                .put("index.analysis.analyzer.myAnalyzer.alternate", "shifted")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");
        TokenStream tsPunctuation = analyzer.tokenStream("content", "foo-bar");
        BytesRef b1 = bytesFromTokenStream(tsPunctuation);
        TokenStream tsWithoutPunctuation = analyzer.tokenStream("content", "foo bar");
        BytesRef b2 = bytesFromTokenStream(tsWithoutPunctuation);
        assertTrue(compare(b1.bytes, b2.bytes) == 0);
    }

    /*
    * Setting alternate=shifted and variableTop to shift whitespace, but not
    * punctuation or symbols, to quaternary level
    */
    public void testIgnoreWhitespace() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "en")
                .put("index.analysis.analyzer.myAnalyzer.strength", "primary")
                .put("index.analysis.analyzer.myAnalyzer.alternate", "shifted")
                .put("index.analysis.analyzer.myAnalyzer.variableTop", 4096) // SPACE
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");
        TokenStream tsWithoutSpace = analyzer.tokenStream(null, "foobar");
        BytesRef b1 = bytesFromTokenStream(tsWithoutSpace);
        TokenStream tsWithSpace = analyzer.tokenStream(null, "foo bar");
        BytesRef b2 = bytesFromTokenStream(tsWithSpace);
        assertTrue(compare(b1.bytes, b2.bytes) == 0);

        // now check that punctuation still matters: foo-bar < foo bar
        TokenStream tsWithPunctuation = analyzer.tokenStream(null, "foo-bar");
        BytesRef b3 = bytesFromTokenStream(tsWithPunctuation);
        assertTrue(compare(b3.bytes, b1.bytes) < 0);
    }

    /*
    * Setting numeric to encode digits with numeric value, so that
    * foobar-9 sorts before foobar-10
    */
    public void testNumerics() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "en")
                .put("index.analysis.analyzer.myAnalyzer.numeric", true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");
        TokenStream tsNine = analyzer.tokenStream(null, "foobar-9");
        BytesRef b1 = bytesFromTokenStream(tsNine);
        TokenStream tsTen = analyzer.tokenStream(null, "foobar-10");
        BytesRef b2 = bytesFromTokenStream(tsTen);
        assertTrue(compare(b1.bytes, b2.bytes) == -1);
    }

    /*
    * Setting caseLevel=true to create an additional case level between
    * secondary and tertiary
    */
    public void testIgnoreAccentsButNotCase() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "en")
                .put("index.analysis.analyzer.myAnalyzer.strength", "primary")
                .put("index.analysis.analyzer.myAnalyzer.caseLevel", "true")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");

        String withAccents = "résumé";
        String withoutAccents = "resume";
        String withAccentsUpperCase = "Résumé";
        String withoutAccentsUpperCase = "Resume";

        TokenStream tsWithAccents = analyzer.tokenStream(null, withAccents);
        BytesRef b1 = bytesFromTokenStream(tsWithAccents);
        TokenStream tsWithoutAccents = analyzer.tokenStream(null, withoutAccents);
        BytesRef b2 = bytesFromTokenStream(tsWithoutAccents);
        assertTrue(compare(b1.bytes, b2.bytes) == 0);

        TokenStream tsWithAccentsUpperCase = analyzer.tokenStream(null, withAccentsUpperCase);
        BytesRef b3 = bytesFromTokenStream(tsWithAccentsUpperCase);
        TokenStream tsWithoutAccentsUpperCase = analyzer.tokenStream(null, withoutAccentsUpperCase);
        BytesRef b4 = bytesFromTokenStream(tsWithoutAccentsUpperCase);
        assertTrue(compare(b3.bytes, b4.bytes) == 0);

        // now check that case still matters: resume < Resume
        TokenStream tsLower = analyzer.tokenStream(null, withoutAccents);
        BytesRef b5 = bytesFromTokenStream(tsLower);
        TokenStream tsUpper = analyzer.tokenStream(null, withoutAccentsUpperCase);
        BytesRef b6 = bytesFromTokenStream(tsUpper);
        assertTrue(compare(b5.bytes, b6.bytes) < 0);
    }

    /*
    * Setting caseFirst=upper to cause uppercase strings to sort
    * before lowercase ones.
    */
    public void testUpperCaseFirst() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.language", "en")
                .put("index.analysis.analyzer.myAnalyzer.strength", "tertiary")
                .put("index.analysis.analyzer.myAnalyzer.caseFirst", "upper")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");
        String lower = "resume";
        String upper = "Resume";
        TokenStream tsLower = analyzer.tokenStream(null, lower);
        BytesRef b1 = bytesFromTokenStream(tsLower);
        TokenStream tsUpper = analyzer.tokenStream(null, upper);
        BytesRef b2 = bytesFromTokenStream(tsUpper);
        assertTrue(compare(b2.bytes, b1.bytes) < 0);
    }

    /*
    * For german, you might want oe to sort and match with o umlaut.
    * This is not the default, but you can make a customized ruleset to do this.
    *
    * The default is DIN 5007-1, this shows how to tailor a collator to get DIN 5007-2 behavior.
    *  http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4423383
    */
    public void testCustomRules() throws Exception {
        RuleBasedCollator baseCollator = (RuleBasedCollator) Collator.getInstance(new ULocale("de_DE"));
        String DIN5007_2_tailorings =
                "& ae , a\u0308 & AE , A\u0308& oe , o\u0308 & OE , O\u0308& ue , u\u0308 & UE , u\u0308";

        RuleBasedCollator tailoredCollator = new RuleBasedCollator(baseCollator.getRules() + DIN5007_2_tailorings);
        String tailoredRules = tailoredCollator.getRules();

        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myAnalyzer.type", "icu_collation")
                .put("index.analysis.analyzer.myAnalyzer.rules", tailoredRules)
                .put("index.analysis.analyzer.myAnalyzer.strength", "primary")
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("myAnalyzer");

        String germanUmlaut = "Töne";
        TokenStream tsUmlaut = analyzer.tokenStream(null, germanUmlaut);
        BytesRef b1 = bytesFromTokenStream(tsUmlaut);

        String germanExpandedUmlaut = "Toene";
        TokenStream tsExpanded = analyzer.tokenStream(null, germanExpandedUmlaut);
        BytesRef b2 = bytesFromTokenStream(tsExpanded);

        assertTrue(compare(b1.bytes, b2.bytes) == 0);
    }

    public void testPrimaryStrengthFromJson() throws Exception {
        String resource = "icu_collation.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));

        Analyzer analyzer = analysis.indexAnalyzers.get("icu_german_collate");

        String[] words = new String[]{
                "Göbel",
                "Goethe",
                "Goldmann",
                "Göthe",
                "Götz"
        };
        MultiMap<BytesRef,String> bytesRefMap = new TreeMultiMap<>();
        for (String s : words) {
            TokenStream ts = analyzer.tokenStream(null, s);
            bytesRefMap.put(bytesFromTokenStream(ts), s);
        }
        Iterator<Set<String>> it = bytesRefMap.values().iterator();
        assertEquals("[Göbel]",it.next().toString());
        assertEquals("[Goethe, Göthe]",it.next().toString());
        assertEquals("[Götz]",it.next().toString());
        assertEquals("[Goldmann]",it.next().toString());
    }

    public void testQuaternaryStrengthFromJson() throws Exception {
        String resource = "icu_collation.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));

        Analyzer analyzer = analysis.indexAnalyzers.get("icu_german_collate_without_punct");

        String[] words = new String[]{
                "Göbel",
                "G-oethe",
                "Gold*mann",
                "Göthe",
                "Götz"
        };
        MultiMap<BytesRef,String> bytesRefMap = new TreeMultiMap<>();
        for (String s : words) {
            TokenStream ts = analyzer.tokenStream(null, s);
            bytesRefMap.put(bytesFromTokenStream(ts), s);
        }
        Iterator<Set<String>> it = bytesRefMap.values().iterator();
        assertEquals("[Göbel]",it.next().toString());
        assertEquals("[G-oethe]",it.next().toString());
        assertEquals("[Göthe]",it.next().toString());
        assertEquals("[Götz]",it.next().toString());
        assertEquals("[Gold*mann]",it.next().toString());
    }

    public void testGermanPhoneBook() throws Exception {
        String resource = "icu_collation.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("german_phonebook");

        String[] words = new String[]{
                "Göbel",
                "Goethe",
                "Goldmann",
                "Göthe",
                "Götz"
        };
        MultiMap<BytesRef,String> bytesRefMap = new TreeMultiMap<>();
        for (String s : words) {
            TokenStream ts = analyzer.tokenStream(null, s);
            bytesRefMap.put(bytesFromTokenStream(ts), s);
        }
        Iterator<Set<String>> it = bytesRefMap.values().iterator();
        assertEquals("[Göbel]",it.next().toString());
        assertEquals("[Goethe, Göthe]",it.next().toString());
        assertEquals("[Götz]",it.next().toString());
        assertEquals("[Goldmann]",it.next().toString());
    }

    public void testReorder() throws Exception {
        String resource = "icu_collation.json";
        Settings settings = Settings.builder()
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        OpenSearchTestCase.TestAnalysis analysis = OpenSearchTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));
        Analyzer analyzer = analysis.indexAnalyzers.get("reorder");
        assertNotNull(analyzer);
    }

    private BytesRef bytesFromTokenStream(TokenStream stream) throws Exception {
        TermToBytesRefAttribute termAttr = stream.getAttribute(TermToBytesRefAttribute.class);
        stream.reset();
        BytesRefBuilder bytesRefBuilder = new BytesRefBuilder();
        while (stream.incrementToken()) {
            BytesRef bytesRef = termAttr.getBytesRef();
            bytesRefBuilder.append(bytesRef);
        }
        stream.close();
        return bytesRefBuilder.toBytesRef();
    }

    private int compare(byte[] left, byte[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }
}
