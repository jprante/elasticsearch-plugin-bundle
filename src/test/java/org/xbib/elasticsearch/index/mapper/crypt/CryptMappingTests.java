package org.xbib.elasticsearch.index.mapper.crypt;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.MapperTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;

public class CryptMappingTests extends Assert {

    @Test
    public void testSHA256CryptMapping() throws Exception {
        String mapping = copyToStringFromClasspath("sha256-mapping.json");
        DocumentMapperParser docMapperParser = MapperTestUtils.newMapperParser();
        DocumentMapper docMapper = docMapperParser.parse(mapping);
        String sampleText = copyToStringFromClasspath("plaintext.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("test", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("{SHA-256}cc482c9bf51da22e59ce8731719963a3fee3d2c7240ee2ee7f13cae4f27f773a", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = docMapperParser.parse(builtMapping);
        json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("test", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("{SHA-256}cc482c9bf51da22e59ce8731719963a3fee3d2c7240ee2ee7f13cae4f27f773a", doc.getFields("someField")[0].stringValue());
    }

    @Test
    public void testSHA512CryptMapping() throws Exception {
        String mapping = copyToStringFromClasspath("sha512-mapping.json");
        DocumentMapperParser docMapperParser = MapperTestUtils.newMapperParser();
        DocumentMapper docMapper = docMapperParser.parse(mapping);
        String sampleText = copyToStringFromClasspath("plaintext.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("test", "someType", "1", json).rootDoc();
        //for (IndexableField field : doc.getFields()) {
        //    log.info("{} = {}", field.name(), field.stringValue());
        //}
        assertEquals(1, doc.getFields("someField").length);;
        assertEquals("{SHA-512}9ca2bab7ffacb00e1c3f5f00eb2405bc32755159b18a013092b54adbe88ff47c21445c3dba035c4721588e42ec6921f4153c52b9feb214e984f24676ad9553f9", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = docMapperParser.parse(builtMapping);
        json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("test", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField").length);;
        assertEquals("{SHA-512}9ca2bab7ffacb00e1c3f5f00eb2405bc32755159b18a013092b54adbe88ff47c21445c3dba035c4721588e42ec6921f4153c52b9feb214e984f24676ad9553f9", doc.getFields("someField")[0].stringValue());
    }

    public String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
