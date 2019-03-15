package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.german;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.IOException;
import java.io.StringReader;

/**
 * German normalization tests.
 */
public class GermanNormalizationTests extends ESTokenStreamTestCase {

    public void testGerman1() throws IOException {

        String source = "Ein schöner Tag in Köln im Café an der Straßenecke";

        String[] expected = {
            "Ein",
            "schoner",
            "Tag",
            "in",
            "Koln",
            "im",
            "Café",
            "an",
            "der",
            "Strassenecke"
        };
        String resource = "german_normalization_analysis.json";
        Settings settings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home"))
                .loadFromStream(resource, getClass().getResourceAsStream(resource), true)
                .build();
        ESTestCase.TestAnalysis analysis = ESTestCase.createTestAnalysis(new Index("test", "_na_"),
                settings,
                new BundlePlugin(Settings.EMPTY));

        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("umlaut");
        Tokenizer tokenizer = analysis.tokenizer.get("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }
}
