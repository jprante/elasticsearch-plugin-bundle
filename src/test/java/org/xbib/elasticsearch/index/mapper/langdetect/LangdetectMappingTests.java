package org.xbib.elasticsearch.index.mapper.langdetect;

import org.apache.lucene.util.SuppressForbidden;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.Settings;
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
 * Language detection mapping test.
 */
public class LangdetectMappingTests extends ESSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return Collections.singletonList(BundlePlugin.class);
    }

    public void testSimpleMappings() throws Exception {
        String mapping = copyToStringFromClasspath("simple-mapping.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        DocumentMapper docMapper2 = createIndex("some_index2")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(builtMapping));
        json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        sourceToParse = SourceToParse.source("some_index2", "someType", "1", json, XContentType.JSON);
        doc = docMapper2.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
    }

    public void testBinary() throws Exception {
        String mapping = copyToStringFromClasspath("base64-mapping.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        String sampleBinary = copyToStringFromClasspath("base64.txt");
        String sampleText = copyToStringFromClasspath("base64-decoded.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleBinary).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(2, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        DocumentMapper docMapper2 = createIndex("some_index2")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(builtMapping));
        json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        sourceToParse = SourceToParse.source("some_index2", "someType", "1", json, XContentType.JSON);
        doc = docMapper2.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length, 1);
        assertEquals("en", doc.getFields("someField")[0].stringValue(), "en");
    }

    public void testCustomMappings() throws Exception {
        //String home =  System.getProperty("path.home") != null ?  System.getProperty("path.home") :  System.getProperty("user.dir");
        Settings settings = Settings.builder()
                //.put("path.home", home)
                .loadFromStream("settings.json", getClass().getResourceAsStream("settings.json"), true)
                .build();
        String mapping = copyToStringFromClasspath("mapping.json");
        DocumentMapper docMapper = createIndex("some_index", settings)
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("german.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("Deutsch", doc.getFields("someField")[0].stringValue());
    }

    public void testBinary2() throws Exception {
        String mapping = copyToStringFromClasspath("base64-2-mapping.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        //String sampleBinary = copyToStringFromClasspath("base64-2.txt");
        String sampleText = copyToStringFromClasspath("base64-2-decoded.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("content", sampleText).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        //for (IndexableField field : doc.getFields()) {
        //    logger.info("binary2 {} = {} stored={}", field.name(), field.stringValue(), field.fieldType().stored());
        //}
        assertEquals(1, doc.getFields("content.language").length);
        assertEquals("en", doc.getFields("content.language")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        DocumentMapper docMapper2 = createIndex("some_index2")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(builtMapping));
        json = XContentFactory.jsonBuilder().startObject().field("content", sampleText).endObject().bytes();
        sourceToParse = SourceToParse.source("some_index2", "someType", "1", json, XContentType.JSON);
        doc = docMapper2.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("content.language").length);
        assertEquals("en", doc.getFields("content.language")[0].stringValue());
    }

    public void testShortTextProfile() throws Exception {
        String mapping = copyToStringFromClasspath("short-text-mapping.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        DocumentMapper docMapper2 = createIndex("some_index2")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(builtMapping));
        json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        sourceToParse = SourceToParse.source("some_index2", "someType", "1", json, XContentType.JSON);
        doc = docMapper2.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
    }

    public void testToFields() throws Exception {
        String mapping = copyToStringFromClasspath("mapping-to-fields.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        SourceToParse sourceToParse = SourceToParse.source("some_index", "someType", "1", json, XContentType.JSON);
        ParseContext.Document doc = docMapper.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        // re-parse it
        String builtMapping = docMapper.mappingSource().string();
        DocumentMapper docMapper2 = createIndex("some_index2")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(builtMapping));
        json = XContentFactory.jsonBuilder().startObject().field("someField", sampleText).endObject().bytes();
        sourceToParse = SourceToParse.source("some_index2", "someType", "1", json, XContentType.JSON);
        doc = docMapper2.parse(sourceToParse).rootDoc();
        assertEquals(1, doc.getFields("someField").length);
        assertEquals("en", doc.getFields("someField")[0].stringValue());
        assertEquals(1, doc.getFields("english_field").length);
        assertEquals("This is a very small example of a text", doc.getFields("english_field")[0].stringValue());
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return Streams.copyToString(new InputStreamReader(getClass().getResource(path).openStream(), "UTF-8"));
    }
}
