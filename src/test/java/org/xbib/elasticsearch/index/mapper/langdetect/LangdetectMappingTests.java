package org.xbib.elasticsearch.index.mapper.langdetect;

import com.google.common.base.Charsets;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.DocumentMapperParser;
import org.elasticsearch.index.mapper.ParseContext;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.index.mapper.MapperTestUtils;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.elasticsearch.common.io.Streams.copyToString;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class LangdetectMappingTests extends Assert {

    @Test
    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("simple-mapping.json");
        DocumentMapper docMapper = newMapperParser().parse(mapping);
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField.lang").length);
        assertEquals("eng", doc.getFields("someField.lang")[0].stringValue());

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = newMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField.lang").length);
        assertEquals("eng", doc.getFields("someField.lang")[0].stringValue());
    }

    @Test
    public void testBinary() throws Exception {
        String mapping = copyToStringFromClasspath("base64-mapping.json");
        DocumentMapper docMapper = newMapperParser().parse(mapping);
        String sampleBinary = copyToStringFromClasspath("base64.txt");
        String sampleText = copyToStringFromClasspath("base64-decoded.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleBinary).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField.lang").length);
        assertEquals("eng", doc.getFields("someField.lang")[0].stringValue());

        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        docMapper = newMapperParser().parse(builtMapping);
        json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField.lang").length, 1);
        assertEquals("eng", doc.getFields("someField.lang")[0].stringValue(), "eng");
    }

    @Test
    public void testMappings() throws Exception {
        Settings settings = Settings.settingsBuilder()
                .put("path.home", System.getProperty("path.home"))
                .loadFromStream("settings.json", getClass().getResourceAsStream("settings.json")).build();
        String mapping = copyToStringFromClasspath("mapping.json");
        DocumentMapper docMapper = newMapperParser(settings).parse(mapping);
        String sampleText = copyToStringFromClasspath("german.txt");
        BytesReference json = jsonBuilder().startObject().field("_id", 1).field("someField", sampleText).endObject().bytes();
        ParseContext.Document doc = docMapper.parse("someIndex", "someType", "1", json).rootDoc();
        assertEquals(1, doc.getFields("someField.lang").length);
        assertEquals("Deutsch", doc.getFields("someField.lang")[0].stringValue());
    }

    private DocumentMapperParser newMapperParser() {
        DocumentMapperParser mapperParser = MapperTestUtils.newMapperParser();
        mapperParser.putTypeParser(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        return mapperParser;
    }

    private DocumentMapperParser newMapperParser(Settings settings) {
        DocumentMapperParser mapperParser = MapperTestUtils.newMapperParser(settings);
        mapperParser.putTypeParser(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        return mapperParser;
    }

    public String copyToStringFromClasspath(String path) throws IOException {
        return copyToString(new InputStreamReader(getClass().getResource(path).openStream(), Charsets.UTF_8));
    }
}
