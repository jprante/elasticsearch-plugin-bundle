package org.xbib.elasticsearch.index.mapper.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.TermsQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.Version;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.StringFieldMapper;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.TypeParsers.parseField;

/**
 *
 */
public class ReferenceMapper extends FieldMapper {

    private static final Logger logger = LogManager.getLogger(ReferenceMapper.class.getName());

    public static final String CONTENT_TYPE = "ref";

    private static final CopyTo COPYTO_EMPTY = new CopyTo.Builder().build();

    private final Client client;

    private String index;

    private String type;

    private List<String> fields;

    private FieldMapper contentMapper;

    private CopyTo copyTo;

    public ReferenceMapper(String simpleName,
                           MappedFieldType fieldType,
                           MappedFieldType defaultFieldType,
                           Client client,
                           String refindex,
                           String reftype,
                           List<String> reffields,
                           FieldMapper contentMapper,
                           Settings indexSettings,
                           MultiFields multiFields,
                           CopyTo copyTo) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, COPYTO_EMPTY);
        this.copyTo = copyTo;
        this.client = client;
        this.index = refindex;
        this.type = reftype;
        this.fields = reffields;
        this.contentMapper = contentMapper;
    }

    /**
     * Creates instances of the fields that the current field should be copied to
     */
    private static void parseCopyFields(ParseContext context, List<String> copyToFields) throws IOException {
        if (!context.isWithinCopyTo() && !copyToFields.isEmpty()) {
            context = context.createCopyToContext();
            for (String field : copyToFields) {
                // In case of a hierarchy of nested documents, we need to figure out
                // which document the field should go to
                ParseContext.Document targetDoc = null;
                for (ParseContext.Document doc = context.doc(); doc != null; doc = doc.getParent()) {
                    if (field.startsWith(doc.getPrefix())) {
                        targetDoc = doc;
                        break;
                    }
                }
                assert targetDoc != null;
                final ParseContext copyToContext;
                if (targetDoc == context.doc()) {
                    copyToContext = context;
                } else {
                    copyToContext = context.switchDoc(targetDoc);
                }
                // simplified - no dynamic field creation
                FieldMapper fieldMapper = copyToContext.docMapper().mappers().getMapper(field);
                if (fieldMapper != null) {
                    fieldMapper.parse(copyToContext);
                } else {
                    throw new MapperParsingException("attempt to copy value to non-existing field [" + field + "]");
                }
            }
        }
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
        String content = null;
        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
        } else {
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token == XContentParser.Token.VALUE_STRING) {
                    if (currentFieldName != null) {
                        switch (currentFieldName) {
                            case "ref_index":
                                index = parser.text();
                                break;
                            case "ref_type":
                                type = parser.text();
                                break;
                            case "ref_fields":
                                // single field
                                fields = new LinkedList<>();
                                fields.add(parser.text());
                                break;
                        }
                    }
                } else if (token == XContentParser.Token.START_ARRAY) {
                    if (currentFieldName != null) {
                        switch (currentFieldName) {
                            case "ref_fields": {
                                fields = new LinkedList<>();
                                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                                    if (parser.text() != null) {
                                        fields.add(parser.text());
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (content == null) {
            return null;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        if (client != null && index != null && type != null && fields != null) {
            try {
                GetResponse response = client.prepareGet()
                        .setIndex(index)
                        .setType(type)
                        .setId(content)
                        .setStoredFields(fields.toArray(new String[fields.size()]))
                        .execute()
                        .actionGet();
                if (response != null && response.isExists()) {
                    for (String field : fields) {
                        // deprecated???
                        GetField getField = response.getField(field);
                        if (getField != null) {
                            for (Object object : getField.getValues()) {
                                context = context.createExternalValueContext(object);
                                if (copyTo != null) {
                                    parseCopyFields(context, copyTo.copyToFields());
                                }
                            }
                        }
                    }
                } else {
                    logger.warn("ref doc does not exist: {}/{}/{}", index, type, content);
                }
            } catch (Exception e) {
                logger.error("error while getting ref doc {}/{}/{}: {}", index, type, content, e.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> fields) throws IOException {
        // override
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        builder.field("type", CONTENT_TYPE);
        if (index != null) {
            builder.field("ref_index", index);
        }
        if (type != null) {
            builder.field("ref_type", type);
        }
        if (fields != null) {
            builder.field("ref_fields", fields);
        }
        if (copyTo != null) {
            copyTo.toXContent(builder, params);
        }
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    public static final class Defaults {
        public static final ReferenceFieldType FIELD_TYPE = new ReferenceFieldType();

        static {
            FIELD_TYPE.freeze();
        }
    }

    public static final class ReferenceFieldType extends MappedFieldType {

        public ReferenceFieldType() {
        }

        protected ReferenceFieldType(ReferenceMapper.ReferenceFieldType ref) {
            super(ref);
        }

        public ReferenceMapper.ReferenceFieldType clone() {
            return new ReferenceMapper.ReferenceFieldType(this);
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        public String value(Object value) {
            return value == null ? null : value.toString();
        }

        /** Returns the indexed value used to construct search "values".
         *  This method is used for the default implementations of most
         *  query factory methods such as {@link #termQuery}. */
        protected BytesRef indexedValueForSearch(Object value) {
            return BytesRefs.toBytesRef(value);
        }

        @Override
        public Query termQuery(Object value, QueryShardContext context) {
            failIfNotIndexed();
            TermQuery query = new TermQuery(new Term(name(), indexedValueForSearch(value)));
            if (boost() == 1f ||
                    (context != null && context.indexVersionCreated().before(Version.V_5_0_0_alpha1))) {
                return query;
            }
            return new BoostQuery(query, boost());
        }

        @Override
        public Query termsQuery(List<?> values, QueryShardContext context) {
            failIfNotIndexed();
            BytesRef[] bytesRefs = new BytesRef[values.size()];
            for (int i = 0; i < bytesRefs.length; i++) {
                bytesRefs[i] = indexedValueForSearch(values.get(i));
            }
            return new TermsQuery(name(), bytesRefs);
        }

    }

    @SuppressWarnings({"rawtypes"})
    public static class Builder extends FieldMapper.Builder<Builder, ReferenceMapper> {

        private FieldMapper.Builder contentBuilder;

        private Client client;

        private String refIndex;

        private String refType;

        private List<String> refFields;

        public Builder(String name, Client client) {
            super(name, Defaults.FIELD_TYPE, Defaults.FIELD_TYPE);
            this.client = client;
            this.refFields = new LinkedList<>();
            this.contentBuilder = new StringFieldMapper.Builder(name);
        }

        public Builder refIndex(String refIndex) {
            this.refIndex = refIndex;
            return this;
        }

        public Builder refType(String refType) {
            this.refType = refType;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder refFields(Object refFields) {
            if (refFields instanceof List) {
                this.refFields = (List<String>) refFields;
            } else if (refFields != null) {
                this.refFields = Collections.singletonList(refFields.toString());
            }
            return this;
        }

        @Override
        public ReferenceMapper build(BuilderContext context) {
            FieldMapper contentMapper = (FieldMapper) contentBuilder.build(context);
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

            return new ReferenceMapper(name,
                    this.fieldType,
                    defaultFieldType,
                    client,
                    refIndex,
                    refType,
                    refFields,
                    contentMapper,
                    context.indexSettings(),
                    multiFieldsBuilder.build(this, context),
                    copyTo);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private Client client;

        public void setClient(Client client) {
            this.client = client;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            ReferenceMapper.Builder builder = new Builder(name, client);
            parseField(builder, name, node, parserContext);
            Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "analyzer": {
                        iterator.remove();
                        break;
                    }
                    case "ref_index":
                        builder.refIndex(fieldNode.toString());
                        iterator.remove();
                        break;
                    case "ref_type":
                        builder.refType(fieldNode.toString());
                        iterator.remove();
                        break;
                    case "ref_fields":
                        builder.refFields(entry.getValue());
                        iterator.remove();
                        break;
                }
            }
            return builder;
        }
    }

}