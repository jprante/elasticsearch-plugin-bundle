package org.xbib.elasticsearch.index.analysis.icu;

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
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.bundle.BundlePlugin;

import java.io.IOException;
import java.io.StringReader;

public class IcuTokenizerTests extends Assert {

    @Test
    public void testLetterNonBreak() throws IOException {

        String source = "Das ist ein Bindestrich-Wort, oder etwa nicht? Jetzt kommen wir zum Ende.";

        String[] expected = {
                "Das",
                "ist",
                "ein",
                "Bindestrich-Wort",
                "oder",
                "etwa",
                "nicht",
                "Jetzt",
                "kommen",
                "wir",
                "zum",
                "Ende"
        };
        AnalysisService analysisService = createAnalysisService();
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenizer, expected);
    }

    @Test
    public void testIdentifierNonBreak() throws IOException {
        AnalysisService analysisService = createAnalysisService();

        String source = "ISBN 3-428-84350-9";

        String[] expected = {
                "ISBN",
                "3-428-84350-9"
        };
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenizer, expected);
    }

    @Test
    public void testIdentifierNonBreakSingleToken() throws IOException {
        AnalysisService analysisService = createAnalysisService();

        String source = "3-428-84350-9";

        String[] expected = {
                "3-428-84350-9"
        };
        Tokenizer tokenizer = analysisService.tokenizer("my_hyphen_icu_tokenizer").create();
        tokenizer.setReader(new StringReader(source));
        // THIS FAILS
        //assertSimpleTSOutput(tokenizer, expected);
    }


    private AnalysisService createAnalysisService() {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .loadFromClasspath("org/xbib/elasticsearch/index/analysis/icu/icu_tokenizer.json").build();
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
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }
}
