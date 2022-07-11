package org.xbib.opensearch.plugin.bundle.index.mapper.standardnumber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentParser;
import org.opensearch.common.xcontent.support.XContentMapValues;
import org.opensearch.index.mapper.FieldMapper;
import org.opensearch.index.mapper.MappedFieldType;
import org.opensearch.index.mapper.Mapper;
import org.opensearch.index.mapper.ParseContext;
import org.opensearch.index.mapper.TextFieldMapper;
import org.xbib.opensearch.plugin.bundle.common.standardnumber.StandardnumberService;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.xbib.opensearch.plugin.bundle.index.mapper.standardnumber.StandardnumberMapper.Defaults.FIELD_TYPE;

/**
 * Standard number field mapper.
 */
public class StandardnumberMapper extends FieldMapper {

    private static final Logger logger = LogManager.getLogger(StandardnumberMapper.class.getName());

    public static final String MAPPER_TYPE = "standardnumber";

    private final Settings settings;

    private final StandardnumberService service;

    public StandardnumberMapper(Settings settings,
                                String simpleName,
                                FieldType fieldType,
                                MappedFieldType mappedFieldType,
                                MultiFields multiFields,
                                CopyTo copyTo,
                                StandardnumberService service) {
        super(simpleName, fieldType, mappedFieldType, multiFields, copyTo);
        this.settings = settings;
        this.service = service;
    }

    @Override
    protected void mergeOptions(FieldMapper other, List<String> conflicts) {

    }

    @Override
    protected void parseCreateField(ParseContext context) throws IOException {
        if (context.externalValueSet()) {
            return;
        }
        XContentParser parser = context.parser();
        if (parser.currentToken() == XContentParser.Token.VALUE_NULL) {
            return;
        }
        String value = null;
        if (parser.currentToken() == XContentParser.Token.START_OBJECT) {
            XContentParser.Token token;
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else {
                    if ("value".equals(currentFieldName) || "_value".equals(currentFieldName)) {
                        value = parser.textOrNull();
                    }
                }
            }
        } else {
            value = parser.textOrNull();
        }
        try {
            Collection<CharSequence> stdnums = service.lookup(settings, value);
            for (CharSequence stdnum : stdnums) {
                Field field = new Field(mappedFieldType.name(), stdnum, fieldType);
                context.doc().add(field);
            }
        } catch (NumberFormatException e) {
            logger.trace(e.getMessage(), e);
            context.createExternalValueContext("unknown");
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(simpleName());
        builder.field("type", MAPPER_TYPE);
        builder.array("standardnumbers", settings.getAsList("standardnumbers"));
        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType() {
        return MAPPER_TYPE;
    }

    public static final class Defaults {
        public static final FieldType FIELD_TYPE = new FieldType();

        static {
            FIELD_TYPE.setStored(true);
            FIELD_TYPE.setOmitNorms(true);
            FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            FIELD_TYPE.freeze();
        }
    }

    public static class Builder extends FieldMapper.Builder<Builder> {

        private Settings.Builder settingsBuilder = Settings.builder();
        private String nullValue;
        private final StandardnumberService service;

        public Builder(String name, StandardnumberService service) {
            super(name, FIELD_TYPE);
            this.service = service;
            this.builder = this;
        }

        public Builder standardNumbers(String[] standardnumbers) {
            settingsBuilder.putList("standardnumbers", standardnumbers);
            return builder;
        }

        public Builder nullValue(String nullValue) {
            this.nullValue = nullValue;
            return builder;
        }

        @Override
        public StandardnumberMapper build(BuilderContext context) {
            if (fieldType.indexOptions() != IndexOptions.NONE && !fieldType.tokenized()) {
                fieldType.setOmitNorms(true);
                fieldType.setIndexOptions(IndexOptions.DOCS);
            }

            TextFieldMapper.TextFieldType ft = new TextFieldMapper.TextFieldType(name);
            return new StandardnumberMapper(settingsBuilder.build(),
                    name,
                    fieldType,
                    ft,
                    multiFieldsBuilder.build(this, context),
                    copyTo,
                    service);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private StandardnumberService service;

        public void setService(StandardnumberService service) {
            this.service = service;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> mapping, ParserContext parserContext) {
            StandardnumberMapper.Builder builder = new Builder(name, service);
            Iterator<Map.Entry<String, Object>> iterator = mapping.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "standardnumbers" :
                        builder.settingsBuilder.putList("standardnumbers", XContentMapValues.nodeStringArrayValue(fieldNode));
                        iterator.remove();
                        break;
                    default:
                        break;
                }
            }
            return builder;
        }
    }
}
