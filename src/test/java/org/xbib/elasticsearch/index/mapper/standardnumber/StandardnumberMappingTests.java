package org.xbib.elasticsearch.index.mapper.standardnumber;

import org.apache.lucene.util.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.SourceToParse;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESSingleNodeTestCase;
import org.xbib.elasticsearch.plugin.bundle.BundlePlugin;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;

/**
 * Standard number mapping tests.
 */
public class StandardnumberMappingTests extends ESSingleNodeTestCase {

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
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
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
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(0, doc.getFields("someField").length);
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        logger.warn("testNonStandardnumber: built mapping =" + builtMapping);
        DocumentMapper docMapper2 = createIndex("some_index2")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(builtMapping));
        json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        sourceToParse = SourceToParse.source("some_index2", "someType", "1", json, XContentType.JSON);
        doc = docMapper2.parse(sourceToParse).rootDoc();
        assertEquals(0, doc.getFields("someField").length);
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return Streams.copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
