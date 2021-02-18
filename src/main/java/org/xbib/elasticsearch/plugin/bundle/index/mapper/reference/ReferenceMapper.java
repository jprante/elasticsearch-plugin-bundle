package org.xbib.elasticsearch.plugin.bundle.index.mapper.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.SimpleMappedFieldType;
import org.elasticsearch.index.mapper.TextFieldMapper;
import org.elasticsearch.index.mapper.TextSearchInfo;
import org.elasticsearch.index.mapper.ValueFetcher;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Reference field mapper.
 */
public class ReferenceMapper extends FieldMapper {

    private static final Logger logger = LogManager.getLogger(ReferenceMapper.class.getName());
    public static final String CONTENT_TYPE = "ref";
    public static final FieldType FIELD_TYPE = new FieldType();

    static {
        FIELD_TYPE.freeze();
    }

    private static final CopyTo COPYTO_EMPTY = new CopyTo.Builder().build();

    private Client client;
    private String refIndex;
    private String refType;
    private List<String> refFields;
    private final FieldMapper contentMapper;
    // override copyTo in FieldMapper
    private final CopyTo copyTo;

    public ReferenceMapper(String simpleName,
                           MappedFieldType defaultFieldType,
                           Client client,
                           String refindex,
                           String reftype,
                           List<String> reffields,
                           FieldMapper contentMapper,
                           MultiFields multiFields,
                           CopyTo copyTo,
                           Builder builder) {
        super(simpleName, defaultFieldType, Lucene.STANDARD_ANALYZER, multiFields, COPYTO_EMPTY);
        this.copyTo = copyTo;
        this.client = client;
        this.refIndex = refindex;
        this.refType = reftype;
        this.refFields = reffields;
        this.contentMapper = contentMapper;
        this.builder = builder;
    }

    private static ReferenceMapper toType(FieldMapper in) {
        return (ReferenceMapper) in;
    }

    private static ReferenceMapper.Builder builder(FieldMapper in) {
        return toType(in).builder;
    }

    private final ReferenceMapper.Builder builder;

    /*
    public CopyTo copyTo() {
        return this.copyTo;
    }
     */

    @Override
    @SuppressWarnings("unchecked")
    public void parse(ParseContext originalContext) throws IOException {
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
                                refIndex = parser.text();
                                break;
                            case "ref_type":
                                refType = parser.text();
                                break;
                            case "ref_fields":
                                // single field only
                                refFields = new LinkedList<>();
                                refFields.add(parser.text());
                                break;
                            default:
                                break;
                        }
                    }
                } else if (token == XContentParser.Token.START_ARRAY && "ref_fields".equals(currentFieldName)) {
                    refFields = new LinkedList<>();
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        if (parser.text() != null) {
                            refFields.add(parser.text());
                        }
                    }
                }
            }
        }
        if (content == null) {
            return;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        if (client != null && refIndex != null && refType != null && refFields != null) {
            try {
                GetResponse response = client.prepareGet()
                        .setIndex(refIndex)
                        .setType(refType)
                        .setId(content)
                        .execute()
                        .actionGet();
                if (response != null && response.isExists()) {
                    for (String field : refFields) {
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
                    logger.warn("ref doc does not exist: {}/{}/{}", refIndex, refType, content);
                }
            } catch (Exception e) {
                logger.error("error while getting ref doc " + refIndex + "/" + refType + "/" + content + ": " + e.getMessage(), e);
            }
        } else {
            logger.warn("missing prerequisite: client={} index={} type={} fields={}",
                        client, refIndex, refType, refFields
            );
        }
    }

    @Override
    public FieldMapper.Builder getMergeBuilder() {
        //return new Builder(simpleName(), client).init(this);
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext) {
        // override
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        builder.field("type", contentType());
        Builder mergeBuilder = new Builder(simpleName(), client);
        mergeBuilder.init(this);
        mergeBuilder.toXContent(builder, includeDefaults);
        multiFields.toXContent(builder, params);
        if (null != copyTo) {
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
                Mapper mapper = copyToContext.docMapper().mappers().getMapper(field);
                if (mapper instanceof FieldMapper) {
                    FieldMapper fieldMapper = (FieldMapper) mapper;
                    fieldMapper.parse(copyToContext);
                } else {
                    throw new MapperParsingException("attempt to copy value to non-existing or non-field field [" + field + "]");
                }
            }
        }
    }

    public static final class TypeParser implements Mapper.TypeParser {

        private final BiFunction<String, ParserContext, Builder> builderFunction;
        private Client client;

        /**
         * Creates a new TypeParser
         * @param builderFunction a function that produces a Builder from a name and parsercontext
         */
        public TypeParser(BiFunction<String, ParserContext, Builder> builderFunction) {
            this.builderFunction = builderFunction;
        }

        @Override
        public Builder parse(String name, Map<String, Object> node, ParserContext parserContext) throws MapperParsingException {
            Builder builder = builderFunction.apply(name, parserContext);
            this.client = builder.client;
            builder.parse(name, parserContext, node);
            return builder;
        }

        public Client getClient() {
            return client;
        }
    }

    public static final class ReferenceFieldType extends SimpleMappedFieldType {

        public ReferenceFieldType(String name) {
            super(name, true, false, true,
                  new TextSearchInfo(FIELD_TYPE, null, Lucene.STANDARD_ANALYZER, Lucene.STANDARD_ANALYZER),
                  Collections.emptyMap());
        }

        @Override
        public ValueFetcher valueFetcher(QueryShardContext context, String format) {
            throw new UnsupportedOperationException();
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
            if ((Float.compare(boost(), 1f) == 0)) {
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

    }

    @SuppressWarnings({"rawtypes"})
    public static class Builder extends FieldMapper.Builder {

        private TextFieldMapper.Builder contentBuilder;
        private Client client;

        private final Parameter<String> refIndex = Parameter.stringParam(
            "ref_index",
            true,
            m -> builder(m).refIndex.getValue(),
            ""
        );

        private final Parameter<String> refType = Parameter.stringParam(
            "ref_type",
            true,
            m -> builder(m).refType.getValue(),
            ""
        );

        private final Parameter<List<String>> refFields = listParam(
            "ref_fields",
            true,
            m -> builder(m).refFields.getValue(),
            Collections.emptyList()
        );

        private static final IndexAnalyzers INDEX_ANALYZERS = new IndexAnalyzers(
            Collections.singletonMap(
                "default",
                new NamedAnalyzer("default", AnalyzerScope.INDEX, new StandardAnalyzer())
            ),
            Collections.emptyMap(),
            Collections.emptyMap()
        );

        public Builder(String name, Client client) {
            super(name);
            this.contentBuilder = new TextFieldMapper.Builder(name, INDEX_ANALYZERS);
            this.client = client;
        }

        @Override
        public List<Parameter<?>> getParameters() {
            return Arrays.asList(refIndex, refType, refFields);
        }

        @Override
        public ReferenceMapper build(ContentPath contentPath) {
            FieldMapper contentMapper = contentBuilder.build(contentPath);
            return new ReferenceMapper(
                name,
                new ReferenceFieldType(buildFullName(contentPath)),
                client,
                refIndex.getValue(),
                refType.getValue(),
                refFields.getValue(),
                contentMapper,
                multiFieldsBuilder.build(this, contentPath),
                copyTo.build(),
                this
            );
        }
    }

    public static final ReferenceMapper.TypeParser PARSER = new ReferenceMapper.TypeParser(
        (n, c) -> {
            Client client = null;
            try
            {
                client = Optional.ofNullable(c)
                                 .map(Mapper.TypeParser.ParserContext::queryShardContextSupplier)
                                 .map(Supplier::get)
                                 .map(QueryShardContext::getClient)
                                 .orElse(null);
            }
            catch (Exception ignored)
            {
            }
            return new ReferenceMapper.Builder(n, client);
        }
    );

    public static Parameter<List<String>> listParam(
        String name, boolean updateable,
        Function<FieldMapper, List<String>> initializer, List<String> defaultValue)
    {
        return new Parameter<>(
            name,
            updateable,
            () -> defaultValue,
            (n, c, o) -> Arrays.asList(XContentMapValues.nodeStringArrayValue(o)),
            initializer
        );
    }
}
