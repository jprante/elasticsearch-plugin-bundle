package org.xbib.elasticsearch.index.analysis.lemmatize;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;

/**
 */
public class LemmatizeSearchTests extends NodeTestUtils {

    @Test
    public void testFstExpansionIndexAndSearchAnalyzer() throws Exception {
        startCluster();
        try {
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
            assertEquals(1L, searchResponse.getHits().totalHits());

            // phrase search: academic libraries -> academic library
            searchResponse = client().prepareSearch()
                    .setQuery(QueryBuilders.matchPhraseQuery("content", "academic library"))
                    .setExplain(true)
                    .execute().actionGet();
            assertEquals(1L, searchResponse.getHits().totalHits());

        } finally {
            stopCluster();
        }
    }
}
