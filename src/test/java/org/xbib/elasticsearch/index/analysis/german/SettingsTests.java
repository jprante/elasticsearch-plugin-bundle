package org.xbib.elasticsearch.index.analysis.german;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.german.AnalysisGermanPlugin;

import java.io.IOException;
import java.io.StringReader;

public class SettingsTests extends Assert {

    @Test
    public void testOne() throws IOException {

        String source = "Ein Tag in Köln im Café an der Straßenecke mit einer Standard-Nummer ISBN 1-4493-5854-3";

        String[] expected = {
                "ein",
                "tag",
                "in",
                "koln",
                "im",
                "caf",
                "an",
                "der",
                "strasseneck",
                "mit",
                "standard-numm",
                "standardnumm",
                "numm",
                "standard",
                "isbn",
                "1-4493-5854-3",
                "1449358543",
                "978-1-4493-5854-9",
                "9781449358549"
        };

        AnalysisService analysisService = createAnalysisService();
        Analyzer analyzer = analysisService.defaultAnalyzer();
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    @Test
    public void testTwo() throws IOException {

        String source = "So wird's was: das Elasticsearch-Buch erscheint beim O'Reilly-Verlag.";

        String[] expected = {
                "so",
                "wird's",
                "was",
                "das",
                "elasticsearch-buch",
                "elasticsearchbuch",
                "buch",
                "elasticsearch",
                "erscheint",
                "beim",
                "o'reilly-verlag"
        };

        AnalysisService analysisService = createAnalysisService();
        Analyzer analyzer = analysisService.defaultAnalyzer();
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    @Test
    public void testThree() throws IOException {

        String source = "978-1-4493-5854-9";

        String[] expected = {
             "978-1-4493-5854-9",
             "9781449358549"
        };

        AnalysisService analysisService = createAnalysisService();
        Analyzer analyzer = analysisService.defaultAnalyzer();
        assertSimpleTSOutput(analyzer.tokenStream(null, new StringReader(source)), expected);
    }

    private AnalysisService createAnalysisService() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .loadFromClasspath("org/xbib/elasticsearch/index/analysis/german/test-settings.json").build();

        Index index = new Index("test");

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)),
                new IndicesAnalysisModule())
                .createInjector();

        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new AnalysisGermanPlugin().onModule(analysisModule);

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
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }
}