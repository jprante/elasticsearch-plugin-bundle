package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.lemmatize;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Lemmatize search tests.
 */
public class LemmatizeSearchTests extends ESSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Arrays.asList(BundlePlugin.class, CommonAnalysisPlugin.class);
    }

    public void testFstExpansionIndexAndSearchAnalyzer() throws Exception {
        Settings settings = Settings.builder()
                .put("index.analysis.analyzer.myanalyzer.type", "custom")
                .put("index.analysis.analyzer.myanalyzer.tokenizer", "standard")
                .put("index.analysis.analyzer.myanalyzer.filter.0", "lemmatize")
                .put("index.analysis.analyzer.myanalyzer.filter.1", "unique")
                .build();

        client().admin().indices().prepareCreate("test")
                .setSettings(settings)
                .addMapping("type1", jsonBuilder().startObject()
                        .startObject("properties")
                        .startObject("content")
                        .field("type", "text")
                        .field("analyzer", "myanalyzer")
                        .endObject()
                        .endObject()
                        .endObject())
                .execute().actionGet();

        client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();

        String[] words = new String[]{
                "While these texts were previously only available to users of academic libraries " +
                        "participating in the partnership, at the end of the first phase of EEBO-TCP the current " +
                        "25,000 texts have now been released into the public domain."
        };

        for (String word : words) {
            client().prepareIndex("test", "type1")
                    .setSource(jsonBuilder().startObject()
                            .field("content", word)
                            .endObject())
                    .execute().actionGet();
        }

        client().admin().indices().prepareRefresh().execute().actionGet();

        // libraries -> library
        SearchResponse searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.matchQuery("content", "library"))
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits());

        // phrase search: academic libraries -> academic library
        searchResponse = client().prepareSearch()
                .setQuery(QueryBuilders.matchPhraseQuery("content", "academic library"))
                .setExplain(true)
                .execute().actionGet();
        assertEquals(1L, searchResponse.getHits().getTotalHits());
    }
}
