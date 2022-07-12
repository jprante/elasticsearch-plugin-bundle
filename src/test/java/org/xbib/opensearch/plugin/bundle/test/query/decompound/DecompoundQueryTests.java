package org.xbib.opensearch.plugin.bundle.test.query.decompound;

import org.junit.Before;
import org.opensearch.action.admin.cluster.node.info.NodeInfo;
import org.opensearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.opensearch.action.admin.cluster.node.info.PluginsAndModules;
import org.opensearch.action.index.IndexRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.analysis.common.CommonAnalysisPlugin;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.QueryStringQueryBuilder;
import org.opensearch.plugins.Plugin;
import org.opensearch.plugins.PluginInfo;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.test.OpenSearchIntegTestCase;
import org.opensearch.test.StreamsUtils;
import org.opensearch.test.hamcrest.OpenSearchAssertions;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;
import org.xbib.opensearch.plugin.bundle.query.decompound.ExactPhraseQueryBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.opensearch.action.admin.cluster.node.info.NodesInfoRequest.Metric.PLUGINS;
import static org.opensearch.test.hamcrest.OpenSearchAssertions.assertAcked;

public class DecompoundQueryTests extends OpenSearchIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(CommonAnalysisPlugin.class, BundlePlugin.class);
    }

    @Before
    public void setup() throws Exception {
        String indexSettings = StreamsUtils.copyToStringFromClasspath(
            getClass().getClassLoader(),
            "org/xbib/opensearch/plugin/bundle/test/query/decompound/decompound-settings.json"
        );
        assertAcked(prepareCreate("test")
                        .setSettings(indexSettings, XContentType.JSON)
                        .setMapping("text", "type=text,analyzer=decomp,search_analyzer=lowercase"));

        ensureGreen(TimeValue.timeValueSeconds(30), "test");
    }

    public void testPluginIsLoaded() {
        NodesInfoResponse response = client().admin().cluster().prepareNodesInfo().addMetric(PLUGINS.metricName()).get();
        for (NodeInfo nodeInfo : response.getNodes()) {
            boolean pluginFound = false;
            for (PluginInfo pluginInfo : nodeInfo.getInfo(PluginsAndModules.class).getPluginInfos()) {
                if (pluginInfo.getName().equals(BundlePlugin.class.getName())) {
                    pluginFound = true;
                    break;
                }
            }
            assertThat(pluginFound, is(true));
        }
    }

    public void testNestedCommonPhraseQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test").setId("1")
                .setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);

        QueryStringQueryBuilder queryStringQueryBuilder =
                QueryBuilders.queryStringQuery("text:\"deutsche spielbankgesellschaft\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder);
        SearchResponse resp = client().prepareSearch("test")
                .setQuery(exactPhraseQueryBuilder)
                .setTrackTotalHits(true)
                .execute().actionGet();
        OpenSearchAssertions.assertHitCount(resp, 1L);
        assertHits(resp.getHits(), "1");

        QueryStringQueryBuilder queryStringQueryBuilder2 =
                QueryBuilders.queryStringQuery("text:\"deutsche bank\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder2 = new ExactPhraseQueryBuilder(queryStringQueryBuilder2);
        SearchResponse resp2 = client().prepareSearch("test")
                .setQuery(exactPhraseQueryBuilder2)
                .setTrackTotalHits(true)
                .execute().actionGet();
        OpenSearchAssertions.assertHitCount(resp2, 0L);

        QueryStringQueryBuilder queryStringQueryBuilder3 =
                QueryBuilders.queryStringQuery("text:\"deutsche spielbankgesellschaft\" AND NOT text:\"deutsche bank\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder3 = new ExactPhraseQueryBuilder(queryStringQueryBuilder3);
        SearchResponse resp3 = client().prepareSearch("test")
                .setQuery(exactPhraseQueryBuilder3)
                .setTrackTotalHits(true)
                .execute().actionGet();
        OpenSearchAssertions.assertHitCount(resp3, 1L);
        assertHits(resp3.getHits(), "1");
    }

    public void testCommonPhraseQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test").setId("1").setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:\"deutsche bank\"");
        SearchResponse resp = client().prepareSearch("test")
                .setQuery(queryStringQueryBuilder)
                .setTrackTotalHits(true)
                .execute().actionGet();
        OpenSearchAssertions.assertHitCount(resp, 1L);
        assertHits(resp.getHits(), "1");
    }

    private void assertHits(SearchHits hits, String... ids) {
        assertThat(hits.getTotalHits().value, equalTo((long) ids.length));
        Set<String> hitIds = new HashSet<>();
        for (SearchHit hit : hits.getHits()) {
            hitIds.add(hit.getId());
        }
        assertThat(hitIds, containsInAnyOrder(ids));
    }
}
