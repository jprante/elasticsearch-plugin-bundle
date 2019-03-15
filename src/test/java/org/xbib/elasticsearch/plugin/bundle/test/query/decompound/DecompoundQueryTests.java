package org.xbib.elasticsearch.plugin.bundle.test.query.decompound;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.analysis.common.CommonAnalysisPlugin;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.StreamsUtils;
import org.elasticsearch.test.hamcrest.ElasticsearchAssertions;
import org.elasticsearch.transport.Netty4Plugin;
import org.junit.Before;
import org.xbib.elasticsearch.plugin.bundle.query.decompound.ExactPhraseQueryBuilder;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

public class DecompoundQueryTests extends ESIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(CommonAnalysisPlugin.class, Netty4Plugin.class, BundlePlugin.class);
    }

    @Before
    public void setup() throws Exception {
        //String indexBody = StreamsUtils.copyToStringFromClasspath(getClass().getClassLoader(),
        //        "/org/xbib/elasticsearch/plugin/bundle/test/query/decompound/decompound_query.json");
        String indexBody = "{\n" +
                "  \"settings\": {\n" +
                "    \"index\": {\n" +
                "      \"number_of_shards\": 1,\n" +
                "      \"number_of_replicas\": 0,\n" +
                "      \"analysis\": {\n" +
                "        \"filter\": {\n" +
                "          \"decomp\":{\n" +
                "            \"type\" : \"decompound\",\n" +
                "            \"use_payload\": true,\n" +
                "            \"use_cache\": true\n" +
                "          }\n" +
                "        },\n" +
                "        \"analyzer\": {\n" +
                "          \"decomp\": {\n" +
                "            \"type\": \"custom\",\n" +
                "            \"tokenizer\" : \"standard\",\n" +
                "            \"filter\" : [\n" +
                "              \"decomp\",\n" +
                "              \"lowercase\"\n" +
                "            ]\n" +
                "          },\n" +
                "          \"lowercase\": {\n" +
                "            \"type\": \"custom\",\n" +
                "            \"tokenizer\" : \"standard\",\n" +
                "            \"filter\" : [\n" +
                "              \"lowercase\"\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"mappings\": {\n" +
                "    \"_doc\": {\n" +
                "      \"properties\": {\n" +
                "        \"text\": {\n" +
                "          \"type\": \"text\",\n" +
                "          \"analyzer\": \"decomp\",\n" +
                "          \"search_analyzer\": \"lowercase\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        prepareCreate("test").setSource(indexBody, XContentType.JSON).get();
        ensureGreen("test");
    }

    public void testPluginIsLoaded() {
        NodesInfoResponse response = client().admin().cluster().prepareNodesInfo().setPlugins(true).get();
        for (NodeInfo nodeInfo : response.getNodes()) {
            boolean pluginFound = false;
            for (PluginInfo pluginInfo : nodeInfo.getPlugins().getPluginInfos()) {
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
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);

        QueryStringQueryBuilder queryStringQueryBuilder =
                QueryBuilders.queryStringQuery("text:\"deutsche spielbankgesellschaft\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder);
        SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
        ElasticsearchAssertions.assertHitCount(resp, 1L);
        assertHits(resp.getHits(), "1");

        QueryStringQueryBuilder queryStringQueryBuilder2 =
                QueryBuilders.queryStringQuery("text:\"deutsche bank\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder2 = new ExactPhraseQueryBuilder(queryStringQueryBuilder2);
        SearchResponse resp2 = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder2).get();
        ElasticsearchAssertions.assertHitCount(resp2, 0L);

        QueryStringQueryBuilder queryStringQueryBuilder3 =
                QueryBuilders.queryStringQuery("text:\"deutsche spielbankgesellschaft\" AND NOT text:\"deutsche bank\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder3 = new ExactPhraseQueryBuilder(queryStringQueryBuilder3);
        SearchResponse resp3 = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder3).get();
        ElasticsearchAssertions.assertHitCount(resp3, 1L);
        assertHits(resp3.getHits(), "1");
    }

    public void testCommonPhraseQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);

        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:\"deutsche bank\"");
        SearchResponse resp = client().prepareSearch("test").setQuery(queryStringQueryBuilder).get();
        ElasticsearchAssertions.assertHitCount(resp, 1L);
        assertHits(resp.getHits(), "1");
    }

    private void assertHits(SearchHits hits, String... ids) {
        assertThat(hits.getTotalHits(), equalTo((long) ids.length));
        Set<String> hitIds = new HashSet<>();
        for (SearchHit hit : hits.getHits()) {
            hitIds.add(hit.getId());
        }
        assertThat(hitIds, containsInAnyOrder(ids));
    }
}
