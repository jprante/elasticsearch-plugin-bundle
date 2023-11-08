package org.xbib.opensearch.plugin.bundle.test.action.isbnformat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.core.xcontent.ToXContent;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.InternalSettingsPlugin;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;
import org.xbib.opensearch.plugin.bundle.action.isbnformat.ISBNFormatRequestBuilder;
import org.xbib.opensearch.plugin.bundle.action.isbnformat.ISBNFormatResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class IsbnFormatActionTests extends OpenSearchSingleNodeTestCase {

    private static final Logger logger = LogManager.getLogger("test");

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Arrays.asList(BundlePlugin.class, InternalSettingsPlugin.class);
    }

    public void testIsbnAction() throws IOException {
        ISBNFormatRequestBuilder isbnFormatRequestBuilder = new ISBNFormatRequestBuilder(client());
        isbnFormatRequestBuilder.setValue("3442151473");
        ISBNFormatResponse isbnFormatResponse = isbnFormatRequestBuilder.execute().actionGet();
        XContentBuilder builder = XContentFactory.jsonBuilder();
        logger.info("{}", isbnFormatResponse.toXContent(builder, ToXContent.EMPTY_PARAMS).toString());
        assertEquals("3442151473", isbnFormatResponse.getIsbn10());
        assertEquals("3-442-15147-3", isbnFormatResponse.getIsbn10Formatted());
        assertEquals("9783442151479", isbnFormatResponse.getIsbn13());
        assertEquals("978-3-442-15147-9", isbnFormatResponse.getIsbn13Formatted());
    }
}
