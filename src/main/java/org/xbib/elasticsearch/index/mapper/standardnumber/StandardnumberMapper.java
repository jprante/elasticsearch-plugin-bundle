package org.xbib.elasticsearch.index.mapper.standardnumber;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexOptions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.StringFieldType;
import org.elasticsearch.index.mapper.TextFieldMapper;
import org.xbib.elasticsearch.common.standardnumber.StandardNumber;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class StandardnumberMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "standardnumber";

    private final StandardnumberService service;

    private final FieldMapper contentMapper;

    private final FieldMapper stdnumMapper;

    public StandardnumberMapper(String simpleName,
                                MappedFieldType fieldType,
                                MappedFieldType defaultFieldType,
                                Settings indexSettings,
                                MultiFields multiFields,
                                CopyTo copyTo,
                                StandardnumberService service,
                                FieldMapper contentMapper,
                                FieldMapper stdnumMapper) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);
        this.service = service;
        this.contentMapper = contentMapper;
        this.stdnumMapper = stdnumMapper;
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
        String content = null;
        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
        }
        if (content == null) {
            return null;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        try {
            Collection<StandardNumber> stdnums = service.detect(content);
            for (StandardNumber stdnum : stdnums) {
                context = context.createExternalValueContext(stdnum.normalizedValue());
                stdnumMapper.parse(context);
            }
        } catch (NumberFormatException e) {
            context = context.createExternalValueContext("unknown");
            stdnumMapper.parse(context);
        }
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> fields) throws IOException {
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(simpleName());
        builder.field("type", CONTENT_TYPE);
        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        stdnumMapper.toXContent(builder, params);
        builder.endObject();
        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    public static final class Defaults {
        public static final StandardnumberFieldType FIELD_TYPE = new StandardnumberFieldType();

        static {
            FIELD_TYPE.freeze();
        }
    }

    public static class Builder extends FieldMapper.Builder<Builder, StandardnumberMapper> {

        private TextFieldMapper.Builder contentBuilder;

        private TextFieldMapper.Builder stdnumBuilder;

        private StandardnumberService service;

        public Builder(String name, StandardnumberService service) {
            super(name, Defaults.FIELD_TYPE, Defaults.FIELD_TYPE);
            this.service = service;
            this.stdnumBuilder = new TextFieldMapper.Builder("standardnumber");
            this.contentBuilder = new TextFieldMapper.Builder(name);
            this.builder = this;
        }

        public Builder content(TextFieldMapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder stdnum(TextFieldMapper.Builder stdnum) {
            this.stdnumBuilder = stdnum;
            return this;
        }

        @Override
        public StandardnumberMapper build(BuilderContext context) {
            MappedFieldType defaultFieldType = Defaults.FIELD_TYPE.clone();
            if (this.fieldType.indexOptions() != IndexOptions.NONE && !this.fieldType.tokenized()) {
                defaultFieldType.setOmitNorms(true);
                defaultFieldType.setIndexOptions(IndexOptions.DOCS);
                if (!this.omitNormsSet && this.fieldType.boost() == 1.0F) {
                    this.fieldType.setOmitNorms(true);
                }
                if (!this.indexOptionsSet) {
                    this.fieldType.setIndexOptions(IndexOptions.DOCS);
                }
            }
            defaultFieldType.freeze();
            this.setupFieldType(context);

            context.path().add(name);
            TextFieldMapper contentMapper = contentBuilder.build(context);
            TextFieldMapper stdnumMapper = stdnumBuilder.build(context);
            context.path().remove();
            return new StandardnumberMapper(name,
                    this.fieldType,
                    defaultFieldType,
                    context.indexSettings(),
                    multiFieldsBuilder.build(this, context),
                    copyTo,
                    service,
                    contentMapper,
                    stdnumMapper);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private StandardnumberService service;

        public void setService(StandardnumberService service) {
            this.service = service;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> mapping, ParserContext parserContext)
                throws MapperParsingException {
            StandardnumberMapper.Builder builder = new Builder(name, service);
            Iterator<Map.Entry<String, Object>> iterator = mapping.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                if (fieldName.equals("fields")) {
                    Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                    for (Map.Entry<String, Object> fieldsEntry : fieldsNode.entrySet()) {
                        String propName = fieldsEntry.getKey();
                        Object propNode = fieldsEntry.getValue();
                        if (name.equals(propName)) {
                            builder.content((TextFieldMapper.Builder) parserContext.typeParser("text").parse(name,
                                    (Map<String, Object>) propNode, parserContext));
                        } else if ("standardnumber".equals(propName)) {
                            builder.stdnum((TextFieldMapper.Builder) parserContext.typeParser("text").parse(propName,
                                    (Map<String, Object>) propNode, parserContext));
                        }
                    }
                    iterator.remove();
                }
            }
            return builder;
        }
    }

    public static class StandardnumberFieldType extends StringFieldType {

        public StandardnumberFieldType() {
            super();
        }

        public StandardnumberFieldType(StandardnumberMapper.StandardnumberFieldType ref) {
            super(ref);
        }

        @Override
        public StandardnumberMapper.StandardnumberFieldType clone() {
            return new StandardnumberMapper.StandardnumberFieldType(this);
        }

        @Override
        public String typeName() {
            return "standardnumber";
        }

        public String value(Object value) {
            return value == null ? null : value.toString();
        }

    }

}