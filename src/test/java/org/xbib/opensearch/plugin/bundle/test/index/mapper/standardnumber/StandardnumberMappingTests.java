package org.xbib.opensearch.plugin.bundle.test.index.mapper.standardnumber;

import org.apache.lucene.util.SuppressForbidden;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.io.Streams;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.mapper.DocumentMapper;
import org.opensearch.index.mapper.ParseContext;
import org.opensearch.index.mapper.SourceToParse;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;

/**
 * Standard number mapping tests.
 */
public class StandardnumberMappingTests extends OpenSearchSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testSimpleStandardNumber() throws Exception {
        String mapping = copyToStringFromClasspath("mapping.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        String sampleText = "978-3-551-75213-0";
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder().startObject()
                .field("someField", sampleText).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(2, doc.getFields("someField").length);
        assertEquals("978-3-551-75213-0", doc.getFields("someField")[0].stringValue());
        assertEquals("9783551752130", doc.getFields("someField")[1].stringValue());
    }

    public void testNonStandardnumber() throws Exception {
        String mapping = copyToStringFromClasspath("mapping.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        String sampleText = "Hello world";
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder()
                .startObject().field("someField", sampleText).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(0, doc.getFields("someField").length);
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        logger.warn("testNonStandardnumber: built mapping =" + builtMapping);
        DocumentMapper docMapper2 = createIndex("some_index2")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(builtMapping));
        json = BytesReference.bytes(XContentFactory.jsonBuilder().startObject()
                .field("someField", sampleText).endObject());
        sourceToParse = new SourceToParse("some_index2", "1", json, XContentType.JSON);
        doc = docMapper2.parse(sourceToParse).rootDoc();
        assertEquals(0, doc.getFields("someField").length);
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return Streams.copyToString(new InputStreamReader(getClass().getResource(path).openStream(), StandardCharsets.UTF_8));
    }
}
