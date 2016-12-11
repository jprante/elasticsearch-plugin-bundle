package org.xbib.elasticsearch.index.analysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeValidationException;
import org.junit.Test;
import org.xbib.elasticsearch.NodeTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.junit.Assert.fail;

/**
 *
 */
public class CustomTokenizerTest extends NodeTestUtils {

    private static final Logger logger = LogManager.getLogger(CustomTokenizerTest.class.getName());

    @Test
    public void testCustomTokenizerRemoval() throws IOException {

        // start node with plugin
        Node node = startNode();
        Client client = node.client();

        // custom tokenizer in settings
        client.admin().indices().prepareCreate("demo")
                .setSettings(copyToStringFromClasspath("settings.json"))
                .addMapping("demo", copyToStringFromClasspath("mapping.json"))
                .execute().actionGet();
        String document = copyToStringFromClasspath("document.json");
        // use tokenizer
        client.prepareIndex("demo", "demo", "1")
                .setSource(document)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .execute().actionGet();
        // delete old index with custom tokenizer
        client.admin().indices().prepareDelete("demo").execute().actionGet();
        node.close();

        // start a new node but without plugin
        node = buildNodeWithoutPlugins();
        try {
            node.start();
        } catch (NodeValidationException e) {
            fail("node not valid");
        }
        client = node.client();
        try {
            // create another index without custom tokenizer
            client.admin().indices().prepareCreate("demo")
                    .execute().actionGet();
        } catch (Exception e) {
            // will fail with java.lang.IllegalArgumentException: Unknown Tokenizer type [icu_tokenizer] for [my_hyphen_icu_tokenizer]
            logger.warn(e.getMessage(), e);
        }
        node.close();
    }

    private String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), StandardCharsets.UTF_8));
    }
}
