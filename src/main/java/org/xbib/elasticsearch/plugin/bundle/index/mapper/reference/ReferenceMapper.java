package org.xbib.elasticsearch.plugin.bundle.index.mapper.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.Version;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.TextFieldMapper;
import org.elasticsearch.index.mapper.TypeParsers;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Reference field mapper.
 */
public class ReferenceMapper extends FieldMapper {

    private static final Logger logger = LogManager.getLogger(ReferenceMapper.class.getName());

    public static final String CONTENT_TYPE = "ref";

    public static final MappedFieldType FIELD_TYPE = new ReferenceFieldType();

    static {
        FIELD_TYPE.freeze();
    }

    private static final CopyTo COPYTO_EMPTY = new CopyTo.Builder().build();

    private final Client client;

    private String index;

    private String type;

    private List<String> fields;

    private FieldMapper contentMapper;

    // override copyTo in FieldMapper
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

    @Override
    @SuppressWarnings("unchecked")
    public Mapper parse(ParseContext originalContext) throws IOException {
        String content = null;
        ParseContext context = originalContext;
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
                                // single field only
                                fields = new LinkedList<>();
                                fields.add(parser.text());
                                break;
                            default:
                                break;
                        }
                    }
                } else if (token == XContentParser.Token.START_ARRAY && "ref_fields".equals(currentFieldName)) {
                    fields = new LinkedList<>();
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        if (parser.text() != null) {
                            fields.add(parser.text());
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
                        .execute()
                        .actionGet();
                if (response != null && response.isExists()) {
                    for (String field : fields) {
                        Map<String, Object> source = response.getSource();
                        List<Object> list = XContentMapValues.extractRawValues(field, source);
                        if (list.isEmpty()) {
                            Object object = XContentMapValues.extractValue(field, source);
                            if (object instanceof Map) {
                                Map<String, Object> map = (Map<String, Object>) object;
                                Double lat = (Double)map.get("lat");
                                Double lon = (Double)map.get("lon");
                                if (lat != null && lon != null) {
                                    list = Collections.singletonList(new GeoPoint(lat, lon));
                                }
                            }
                        }
                        if (list.isEmpty()) {
                            list = XContentMapValues.extractRawValues(field, response.getSource());
                        }
                        for (Object object : list) {
                            context = context.createExternalValueContext(object);
                            if (copyTo != null) {
                                parseCopyFields(context, copyTo.copyToFields());
                            }
                        }
                    }
                } else {
                    logger.warn("ref doc does not exist: {}/{}/{}", index, type, content);
                }
            } catch (Exception e) {
                logger.error("error while getting ref doc " + index + "/" + type + "/"+ content + ": " + e.getMessage(), e);
            }
        } else {
            logger.warn("missing prerequisite: client={} index={} type={} fields={}",
                    client, index, type, fields);
        }
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<IndexableField> fields) {
        // override
    }

    @Override
    protected void doMerge(Mapper mergeWith, boolean updateAllTypes) {
        super.doMerge(mergeWith, updateAllTypes);
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
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

    private static void parseCopyFields(ParseContext originalContext, List<String> copyToFields) throws IOException {
        if (!originalContext.isWithinCopyTo() && !copyToFields.isEmpty()) {
            ParseContext context = originalContext.createCopyToContext();
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
                if (targetDoc == null) {
                    throw new IllegalArgumentException("target doc is null");
                }
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

    public static final class ReferenceFieldType extends MappedFieldType implements Cloneable {

        public ReferenceFieldType() {
            // nothing to instantiate
        }

        protected ReferenceFieldType(ReferenceMapper.ReferenceFieldType ref) {
            super(ref);
        }

        @Override
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

        /**
         * Returns the indexed value used to construct search "values".
         * This method is used for the default implementations of most
         * query factory methods such as {@link #termQuery}.
         *
         * @param value the value
         * @return indexed value
         */
        BytesRef indexedValueForSearch(Object value) {
            return BytesRefs.toBytesRef(value);
        }

        @Override
        public Query termQuery(Object value, QueryShardContext context) {
            failIfNotIndexed();
            TermQuery query = new TermQuery(new Term(name(), indexedValueForSearch(value)));
            if ((Float.compare(boost(), 1f) == 0) ||
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
            return new TermInSetQuery(name(), bytesRefs);
        }

        @Override
        public Query existsQuery(QueryShardContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query fuzzyQuery(Object value, Fuzziness fuzziness, int prefixLength, int maxExpansions,
                                boolean transpositions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query prefixQuery(String value, MultiTermQuery.RewriteMethod method, QueryShardContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Query regexpQuery(String value, int flags, int maxDeterminizedStates,
                                 MultiTermQuery.RewriteMethod method, QueryShardContext context) {
            throw new UnsupportedOperationException();
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
            super(name, FIELD_TYPE, FIELD_TYPE);
            this.client = client;
            this.refFields = new LinkedList<>();
            this.contentBuilder = new TextFieldMapper.Builder(name);
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
            setupFieldType(context);
            return new ReferenceMapper(name,
                    fieldType,
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
            TypeParsers.parseField(builder, name, node, parserContext);
            Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "analyzer": {
                        // ignore
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
                    default:
                        break;
                }
            }
            return builder;
        }
    }
}
