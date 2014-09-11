package org.xbib.elasticsearch.index.analysis.hyphen;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.german.AnalysisGermanPlugin;

import java.io.IOException;
import java.io.StringReader;

public class HyphenTokenFilterTests extends Assert {

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
        Tokenizer tokenizer = analysisService.tokenizer("my_icu_tokenizer").create(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
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
        Tokenizer tokenizer = analysisService.tokenizer("my_icu_tokenizer").create(new StringReader(source));
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
                "100",
                "prozentig",
                "als",
                "Dipl",
                "Ing",
                "arbeiten"
        };
        AnalysisService analysisService = createAnalysisService();
        Tokenizer tokenizer = analysisService.tokenizer("my_icu_tokenizer").create(new StringReader(source));
        TokenFilterFactory tokenFilter = analysisService.tokenFilter("hyphen");
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    private AnalysisService createAnalysisService() {
        Settings settings = ImmutableSettings.settingsBuilder()
                .loadFromClasspath("org/xbib/elasticsearch/index/analysis/icu_tokenizer.json").build();
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
