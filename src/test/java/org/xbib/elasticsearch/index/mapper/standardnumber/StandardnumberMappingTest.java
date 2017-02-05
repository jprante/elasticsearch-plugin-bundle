package org.xbib.elasticsearch.index.mapper.standardnumber;

import org.apache.lucene.index.IndexableField;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.junit.Assert.assertEquals;

/**
 */
public class StandardnumberMappingTest {

    @Test
    public void testSimpleStandardNumber() throws Exception {
        String mapping = copyToStringFromClasspath("mapping.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(mapping));
        String sampleText = "978-3-551-75213-0";
        BytesReference json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(2, doc.getFields("someField").length);
        assertEquals("978-3-551-75213-0", doc.getFields("someField")[0].stringValue());
        assertEquals("9783551752130", doc.getFields("someField")[1].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(2, doc.getFields("someField").length);
        assertEquals("978-3-551-75213-0", doc.getFields("someField")[0].stringValue());
        assertEquals("9783551752130", doc.getFields("someField")[1].stringValue());
    }

    @Test
    public void testNonStandardnumber() throws Exception {
        String mapping = copyToStringFromClasspath("mapping.json");
        DocumentMapper docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(mapping));
        String sampleText = "Hello world";
        BytesReference json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(0, doc.getFields("someField").length);
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = MapperTestUtils.newDocumentMapperParser("someIndex").parse("someType", new CompressedXContent(builtMapping));
        json = jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(0, doc.getFields("someField").length);
    }


    private String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
