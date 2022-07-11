package org.xbib.opensearch.plugin.bundle.test.index.mapper.langdetect;

import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.SuppressForbidden;
import org.opensearch.analysis.common.CommonAnalysisPlugin;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.io.Streams;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.common.xcontent.XContentHelper;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.index.IndexService;
import org.opensearch.index.mapper.DocumentMapper;
import org.opensearch.index.mapper.ParsedDocument;
import org.opensearch.index.mapper.SourceToParse;
import org.opensearch.plugins.Plugin;
import org.opensearch.test.OpenSearchSingleNodeTestCase;
import org.opensearch.test.InternalSettingsPlugin;
import org.xbib.opensearch.plugin.bundle.BundlePlugin;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Language detection mapping test.
 */
public class LangdetectMappingTests extends OpenSearchSingleNodeTestCase {

    /** The plugin classes that should be added to the node. */
    @Override
    protected Collection<Class<? extends Plugin>> getPlugins() {
        return pluginList(BundlePlugin.class, InternalSettingsPlugin.class, CommonAnalysisPlugin.class);
    }

    public void testSimpleMapping() throws Exception {
        IndexService indexService = createIndex("some_index", Settings.EMPTY,
                "someType", getMapping("simple-mapping.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("someType");
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder()
                .startObject().field("someField", sampleText).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "someType", "1", json, XContentType.JSON);
        ParsedDocument doc = docMapper.parse(sourceToParse);
        assertEquals(1, doc.rootDoc().getFields("someField").length);
        assertEquals("en", doc.rootDoc().getFields("someField")[0].stringValue());
    }

    public void testBinary() throws Exception {
        IndexService indexService = createIndex("some_index", Settings.EMPTY,
                "someType", getMapping("base64-mapping.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("someType");
        String sampleBinary = copyToStringFromClasspath("base64.txt");
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder()
                .startObject().field("someField", sampleBinary).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "someType", "1", json, XContentType.JSON);
        ParsedDocument doc = docMapper.parse(sourceToParse);
        for (IndexableField field : doc.rootDoc().getFields()) {
            logger.info("testBinary {} = {} stored={}", field.name(), field.stringValue(), field.fieldType().stored());
        }
        assertTrue(doc.rootDoc().getFields("someField").length >= 1);
        assertEquals("en", doc.rootDoc().getFields("someField")[0].stringValue());
    }

    public void testBinary2() throws Exception {
        String mapping = copyToStringFromClasspath("base64-2-mapping.json");
        DocumentMapper docMapper = createIndex("some_index")
                .mapperService().documentMapperParser()
                .parse("someType", new CompressedXContent(mapping));
        //String sampleBinary = copyToStringFromClasspath("base64-2.txt");
        String sampleText = copyToStringFromClasspath("base64-2-decoded.txt");
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder().startObject()
                .field("content", sampleText).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "someType", "1", json, XContentType.JSON);
        ParsedDocument doc = docMapper.parse(sourceToParse);
        for (IndexableField field : doc.rootDoc().getFields()) {
            logger.info("binary2 {} = {} stored={}", field.name(), field.stringValue(), field.fieldType().stored());
        }
        assertEquals(1, doc.rootDoc().getFields("content.language").length);
        assertEquals("en", doc.rootDoc().getFields("content.language")[0].stringValue());
    }

    public void testCustomMappings() throws Exception {
        Settings settings = Settings.builder()
                .loadFromStream("settings.json", getClass().getResourceAsStream("settings.json"), true)
                .build();
        IndexService indexService = createIndex("some_index", settings,
                "someType", getMapping("mapping.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("someType");
        String sampleText = copyToStringFromClasspath("german.txt");
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder()
                .startObject().field("someField", sampleText).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "someType", "1", json, XContentType.JSON);
        ParsedDocument doc = docMapper.parse(sourceToParse);
        for (IndexableField field : doc.rootDoc().getFields()) {
            logger.info("custom {} = {} stored={}", field.name(), field.stringValue(), field.fieldType().stored());
        }
        assertEquals(1, doc.rootDoc().getFields("someField").length);
        assertEquals("Deutsch", doc.rootDoc().getFields("someField")[0].stringValue());
    }

    public void testShortTextProfile() throws Exception {
        IndexService indexService = createIndex("some_index", Settings.EMPTY,
                "someType", getMapping("short-text-mapping.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("someType");
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder()
                .startObject().field("someField", sampleText).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "someType", "1", json, XContentType.JSON);
        ParsedDocument doc = docMapper.parse(sourceToParse);
        for (IndexableField field : doc.rootDoc().getFields()) {
            logger.info("shorttext {} = {} stored={}", field.name(), field.stringValue(), field.fieldType().stored());
        }
        assertEquals(1, doc.rootDoc().getFields("someField").length);
        assertEquals("en", doc.rootDoc().getFields("someField")[0].stringValue());
    }

    public void testToFields() throws Exception {
        IndexService indexService = createIndex("some_index", Settings.EMPTY,
                "someType", getMapping("mapping-to-fields.json"));
        DocumentMapper docMapper = indexService.mapperService().documentMapper("someType");
        String sampleText = copyToStringFromClasspath("english.txt");
        BytesReference json = BytesReference.bytes(XContentFactory.jsonBuilder()
                .startObject().field("someField", sampleText).endObject());
        SourceToParse sourceToParse = new SourceToParse("some_index", "someType", "1", json, XContentType.JSON);
        ParsedDocument doc = docMapper.parse(sourceToParse);
        assertEquals(1, doc.rootDoc().getFields("someField").length);
        assertEquals("en", doc.rootDoc().getFields("someField")[0].stringValue());
        assertEquals(1, doc.rootDoc().getFields("english_field").length);
        assertEquals("This is a very small example of a text", doc.rootDoc().getFields("english_field")[0].stringValue());
    }

    @SuppressForbidden(reason = "accessing local resources from classpath")
    private String copyToStringFromClasspath(String path) throws Exception {
        return Streams.copyToString(new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8));
    }

    private XContentBuilder getMapping(String path) throws Exception {
        return XContentFactory.jsonBuilder().map(XContentHelper.convertToMap(JsonXContent.jsonXContent,
                getClass().getResourceAsStream(path), true));
    }
}
