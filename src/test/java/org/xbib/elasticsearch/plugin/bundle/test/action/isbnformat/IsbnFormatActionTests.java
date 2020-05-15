package org.xbib.elasticsearch.plugin.bundle.test.action.isbnformat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.elasticsearch.test.InternalSettingsPlugin;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;
import org.xbib.elasticsearch.plugin.bundle.action.isbnformat.ISBNFormatRequestBuilder;
import org.xbib.elasticsearch.plugin.bundle.action.isbnformat.ISBNFormatResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class IsbnFormatActionTests extends ESSingleNodeTestCase {

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
        logger.info("{}", Strings.toString(isbnFormatResponse.toXContent(builder, ToXContent.EMPTY_PARAMS)));
        assertEquals("3442151473", isbnFormatResponse.getIsbn10());
        assertEquals("3-442-15147-3", isbnFormatResponse.getIsbn10Formatted());
        assertEquals("9783442151479", isbnFormatResponse.getIsbn13());
        assertEquals("978-3-442-15147-9", isbnFormatResponse.getIsbn13Formatted());
    }
}
