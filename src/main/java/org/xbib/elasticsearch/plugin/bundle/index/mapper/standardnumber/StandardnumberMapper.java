package org.xbib.elasticsearch.plugin.bundle.index.mapper.standardnumber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.TextFieldMapper;
import org.xbib.elasticsearch.plugin.bundle.common.standardnumber.StandardnumberService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.xbib.elasticsearch.plugin.bundle.index.mapper.standardnumber.StandardnumberMapper.Defaults.FIELD_TYPE;

/**
 * Standard number field mapper.
 */
public class StandardnumberMapper extends FieldMapper {

    private static final Logger logger = LogManager.getLogger(StandardnumberMapper.class.getName());

    public static final String MAPPER_TYPE = "standardnumber";

    private final Settings settings;

    private final StandardnumberService service;
    final FieldType fieldType;

    public StandardnumberMapper(Settings settings,
                                String simpleName,
                                FieldType fieldType,
                                MappedFieldType mappedFieldType,
                                MultiFields multiFields,
                                CopyTo copyTo,
                                StandardnumberService service) {
        super(simpleName, mappedFieldType, multiFields, copyTo);
        this.settings = settings;
        this.service = service;
        this.fieldType = fieldType;
    }

    @Override
    public FieldMapper.Builder getMergeBuilder() {
        return new StandardnumberMapper.Builder(simpleName(), service).init(this);
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

    public static class Builder extends FieldMapper.Builder {

        public Settings.Builder settingsBuilder = Settings.builder();
        private final StandardnumberService service;

        public Builder(String name, StandardnumberService service) {
            super(name);
            this.service = service;
        }

        public Builder standardNumbers(String[] standardnumbers) {
            settingsBuilder.putList("standardnumbers", standardnumbers);
            return this;
        }

        @Override
        protected List<Parameter<?>> getParameters() {
            return Collections.emptyList();
        }

        @Override
        public StandardnumberMapper build(ContentPath context) {

            TextFieldMapper.TextFieldType ft = new TextFieldMapper.TextFieldType(name);
            return new StandardnumberMapper(settingsBuilder.build(),
                    name,
                    FIELD_TYPE,
                    ft,
                    multiFieldsBuilder.build(this, context),
                    copyTo.build(),
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

    public static TypeParser newTypeParser() {
        return new TypeParser();
    }
}
