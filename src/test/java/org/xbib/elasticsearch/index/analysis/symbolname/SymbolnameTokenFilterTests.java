package org.xbib.elasticsearch.index.analysis.symbolname;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
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

public class SymbolnameTokenFilterTests extends Assert {

    private final static ESLogger logger = ESLoggerFactory.getLogger(SymbolnameTokenFilterTests.class.getName());

    @Test
    public void testSimple() throws IOException {

        String source = "Programmieren mit C++";

        String[] expected = {
                "Programmieren",
                "mit",
                "C++",
                "C __PLUSSIGN__ __PLUSSIGN__",
                "C",
                "__PLUSSIGN__",
                "__PLUSSIGN__"
        };
        AnalysisService analysisService = createAnalysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("symbolname");
        Tokenizer tokenizer = analysisService.tokenizer("whitespace").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }


    @Test
    public void testPunctuation() throws IOException {

        String source = "Programmieren mit C++ Version 2.0";

        String[] expected = {
                "Programmieren",
                "mit",
                "C++",
                "C __PLUSSIGN__ __PLUSSIGN__",
                "C",
                "__PLUSSIGN__",
                "__PLUSSIGN__",
                "Version",
                "2.0",
                "__DIGITTWO__ __FULLSTOP__ __DIGITZERO__",
                "__DIGITTWO__",
                "__FULLSTOP__",
                "__DIGITZERO__"
        };
        AnalysisService analysisService = createAnalysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("symbolname");
        Tokenizer tokenizer = analysisService.tokenizer("whitespace").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testSingleSymbols() throws IOException {

        String source = "Programmieren mit + und - ist toll, oder?";

        String[] expected = {
                "Programmieren",
                "mit",
                "+",
                "__PLUSSIGN__",
                "und",
                "-",
                "__HYPHENMINUS__",
                "ist",
                "toll,",
                "toll __COMMA__",
                "toll",
                "__COMMA__",
                "oder?",
                "oder __QUESTIONMARK__",
                "oder",
                "__QUESTIONMARK__"
        };
        AnalysisService analysisService = createAnalysisService();
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("symbolname");
        Tokenizer tokenizer = analysisService.tokenizer("whitespace").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    private AnalysisService createAnalysisService() {
        Settings settings = Settings.settingsBuilder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .build();
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
        Assert.assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            //logger.info("'{}'", termAttr.toString());
            assertTrue(i < expected.length);
            assertEquals(expected[i], termAttr.toString());
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }
}
