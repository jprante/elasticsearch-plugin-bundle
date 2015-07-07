package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.bundle.BundlePlugin;

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
        AnalysisService analysisService = createAnalysisService();
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
        AnalysisService analysisService = createAnalysisService();
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
                "Dipl",
                "Ing",
                "arbeiten"
        };
        AnalysisService analysisService = createAnalysisService();
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
        AnalysisService analysisService = createAnalysisService();
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

        AnalysisService analysisService = createAnalysisService();
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

        AnalysisService analysisService = createAnalysisService();
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

        AnalysisService analysisService = createAnalysisService();
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    private AnalysisService createAnalysisService() {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .loadFromClasspath("org/xbib/elasticsearch/index/analysis/hyphen/hyphen_tokenizer.json").build();
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)),
                new IndicesAnalysisModule())
                .createInjector();
        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new BundlePlugin(settings).onModule(analysisModule);
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);
        return injector.getInstance(AnalysisService.class);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            assertTrue(i < expected.length);
            assertEquals(expected[i], termAttr.toString());
            //System.err.println(termAttr.toString());
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }
}
