package org.xbib.elasticsearch.plugin.bundle.index.mapper.langdetect;

import com.fasterxml.jackson.core.Base64Variants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.FieldNamesFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.SimpleMappedFieldType;
import org.elasticsearch.index.mapper.TextSearchInfo;
import org.elasticsearch.index.mapper.ValueFetcher;
import org.elasticsearch.index.query.QueryShardContext;
import org.xbib.elasticsearch.plugin.bundle.common.langdetect.LangdetectService;
import org.xbib.elasticsearch.plugin.bundle.common.langdetect.Language;
import org.xbib.elasticsearch.plugin.bundle.common.langdetect.LanguageDetectionException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Language detection field mapper.
 */
public class LangdetectMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "langdetect";

    private static final Logger logger = LogManager.getLogger(LangdetectMapper.class);

    final FieldType fieldType;

    public static class Defaults {
        public static final FieldType FIELD_TYPE = new FieldType();
        public static final int NTRIALS = 7;
        public static final double ALPHA = 0.5;
        public static final double ALPHA_WIDTH = 0.05;
        public static final int ITERATION_LIMIT = 10_000;
        public static final double PROB_THRESHOLD = 0.1;
        public static final double CONV_THRESHOLD = 0.99999;
        public static final int BASE_FREQ = 10_000;

        static {
            FIELD_TYPE.setStored(true);
            FIELD_TYPE.setOmitNorms(true);
            FIELD_TYPE.setDocValuesType(DocValuesType.NONE);
            FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            FIELD_TYPE.freeze();
        }
    }

    private static LangdetectMapper toType(FieldMapper in) {
        return (LangdetectMapper) in;
    }

    private static Builder builder(FieldMapper in) {
        return toType(in).builder;
    }

    private final Builder builder;

    private LangdetectService langdetectService = null;
    private LanguageTo languageTo = null;

    public LangdetectMapper(String simpleName,
                            FieldType fieldType,
                            MappedFieldType mappedFieldType,
                            MultiFields multiFields,
                            CopyTo copyTo,
                            Builder builder) {
        super(simpleName, mappedFieldType, Lucene.KEYWORD_ANALYZER, multiFields, copyTo);
        this.fieldType = fieldType;
        this.builder = builder;
    }

    public LangdetectService getLangdetectService() {
        return Objects.requireNonNullElseGet(langdetectService, () -> {
            langdetectService = new LangdetectService(builder.settingsBuilder.build());
            return langdetectService;
        });
    }

    public LanguageTo getLanguageTo() {
        return Objects.requireNonNullElseGet(languageTo, () -> {
            languageTo = LanguageTo.builder().add(builder.languageToMap.getValue()).build();
            return languageTo;
        });
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    public FieldMapper.Builder getMergeBuilder() {
        return new LangdetectMapper.Builder(simpleName(), builder.indexAnalyzers).init(this);
    }

    @Override
    protected void parseCreateField(ParseContext context) throws IOException {
        String value;
        XContentParser parser = context.parser();
        if (context.externalValueSet()) {
            value = context.externalValue().toString();
        } else {
            value = context.parser().textOrNull();
        }
        if (value == null) {
            return;
        }
        boolean isBinary = getLangdetectService().getSettings().getAsBoolean("binary", false);
        if (isBinary) {
            try {
                byte[] b = parser.binaryValue();
                if (b != null && b.length > 0) {
                    value = new String(b, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                // Try to decode manually because of
                // com.fasterxml.jackson.core.JsonParseException: Current token (VALUE_STRING) not VALUE_EMBEDDED_OBJECT,
                // can not access as binary
                try {
                    byte[] b = Base64Variants.getDefaultVariant().decode(parser.text());
                    if (b != null && b.length > 0) {
                        value = new String(b, StandardCharsets.UTF_8);
                    }
                } catch (Exception e2) {
                    // if clear text, this may fail with IllegalArgumentException[Illegal white space character (code 0x20)
                    // ignore exception
                }
            }
        }
        try {
            List<Language> langs = getLangdetectService().detectAll(value);
            for (Language lang : langs) {
                Field field = new Field(fieldType().name(), lang.getLanguage(), fieldType);
                context.doc().add(field);
                if (getLanguageTo().languageToFields().containsKey(lang.getLanguage())) {
                    parseLanguageToFields(context, getLanguageTo().languageToFields().get(lang.getLanguage()));
                }
            }
        } catch (LanguageDetectionException e) {
            logger.warn("upps", e);
            context.createExternalValueContext("unknown");
        }
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        builder.field("type", contentType());
        Builder mergeBuilder = new Builder(simpleName(), this.builder.indexAnalyzers);
        mergeBuilder.init(this);
        mergeBuilder.toXContent(builder, includeDefaults);
        multiFields.toXContent(builder, params);
        if (null != copyTo) {
            copyTo.toXContent(builder, params);
        }
        getLanguageTo().toXContent(builder, params);
    }

    @SuppressWarnings("unchecked")
    private static void parseLanguageToFields(ParseContext originalContext, Object languageToFields) throws IOException {
        List<Object> fieldList = languageToFields instanceof List ?
                (List<Object>)languageToFields : Collections.singletonList(languageToFields);
        ParseContext context = originalContext.createCopyToContext();
        for (Object field : fieldList) {
            ParseContext.Document targetDoc = null;
            for (ParseContext.Document doc = context.doc(); doc != null; doc = doc.getParent()) {
                if (field.toString().startsWith(doc.getPrefix())) {
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
            Mapper mapper = copyToContext.docMapper().mappers().getMapper(field.toString());
            if (mapper instanceof FieldMapper) {
                FieldMapper fieldMapper = (FieldMapper) mapper;
                fieldMapper.parse(copyToContext);
            } else {
                throw new MapperParsingException("attempt to copy value to non-existing field [" + field + "]");
            }
        }
    }

    public static class Builder extends FieldMapper.Builder {

        protected Settings.Builder settingsBuilder = Settings.builder();

        private final Parameter<Boolean> store = Parameter.storeParam(
            m -> builder(m).store.getValue(),
            false
        );

        final Parameter<Integer> nTrial = Parameter.intParam(
            "number_of_trials",
            true,
            m -> {
                Parameter<Integer> nTrial = builder(m).nTrial;
                Integer value = nTrial.getValue();
                builder(m).settingsBuilder.put(nTrial.name, value);
                return value;
            },
            Defaults.NTRIALS
        );
        private final Parameter<Double> alpha = Parameter.doubleParam(
            "alpha",
            true,
            m -> {
                Parameter<Double> alpha = builder(m).alpha;
                Double value = alpha.getValue();
                builder(m).settingsBuilder.put(alpha.name, value);
                return value;
            },
            Defaults.ALPHA
        );
        private final Parameter<Double> alphaWidth = Parameter.doubleParam(
            "alpha_width",
            true,
            m -> {
                Parameter<Double> alphaWidth = builder(m).alphaWidth;
                Double value = alphaWidth.getValue();
                builder(m).settingsBuilder.put(alphaWidth.name, value);
                return value;
            },
            Defaults.ALPHA_WIDTH
        );
        private final Parameter<Integer> iterationLimit = Parameter.intParam(
            "iteration_limit",
            true,
            m -> {
                Parameter<Integer> iterationLimit = builder(m).iterationLimit;
                Integer value = iterationLimit.getValue();
                builder(m).settingsBuilder.put(iterationLimit.name, value);
                return value;
            },
            Defaults.ITERATION_LIMIT
        );
        private final Parameter<Double> probThreshold = Parameter.doubleParam(
            "prob_threshold",
            true,
            m -> {
                Parameter<Double> probThreshold = builder(m).probThreshold;
                Double value = probThreshold.getValue();
                builder(m).settingsBuilder.put(probThreshold.name, value);
                return value;
            },
            Defaults.PROB_THRESHOLD
        );
        private final Parameter<Double> convThreshold = Parameter.doubleParam(
            "conv_threshold",
            true,
            m -> {
                Parameter<Double> convThreshold = builder(m).convThreshold;
                Double value = convThreshold.getValue();
                builder(m).settingsBuilder.put(convThreshold.name, value);
                return value;
            },
            Defaults.CONV_THRESHOLD
        );
        private final Parameter<Integer> baseFreq = Parameter.intParam(
            "base_freq",
            true,
            m -> {
                Parameter<Integer> baseFreq = builder(m).baseFreq;
                Integer value = baseFreq.getValue();
                builder(m).settingsBuilder.put(baseFreq.name, value);
                return value;
            },
            Defaults.BASE_FREQ
        );
        private final Parameter<String> filterPattern = Parameter.stringParam(
            "pattern",
            true,
            m -> {
                Parameter<String> filterPattern = builder(m).filterPattern;
                String value = filterPattern.getValue();
                builder(m).settingsBuilder.put(filterPattern.name, value);
                return value;
            },
            null
        );
        private final Parameter<Integer> max = Parameter.intParam(
            "max",
            true,
            m -> {
                Parameter<Integer> max = builder(m).max;
                Integer value = max.getValue();
                builder(m).settingsBuilder.put(max.name, value);
                return value;
            },
            Integer.MAX_VALUE
        );
        private final Parameter<String> profile = Parameter.stringParam(
            "profile",
            true,
            m -> {
                Parameter<String> profile = builder(m).profile;
                String value = profile.getValue();
                builder(m).settingsBuilder.put(profile.name, value);
                return value;
            },
            null
        );
        private final Parameter<Boolean> binary = Parameter.boolParam(
            "binary",
            true,
            m -> {
                Parameter<Boolean> binary = builder(m).binary;
                Boolean value = binary.getValue();
                builder(m).settingsBuilder.put(binary.name, value);
                return value;
            },
            false
        );

        private final Parameter<Map<String, Object>> map = mapParam(
            "map",
            true,
            m -> {
                Map<String, Object> map = builder(m).map.getValue();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    builder(m).settingsBuilder.put("map." + entry.getKey(), (String) entry.getValue());
                }
                return map;
            },
            Collections.emptyMap()
        );

        private final Parameter<List<String>> languages = listParam(
            "languages",
            true,
            m -> {
                List<String> value = builder(m).languages.getValue();
                settingsBuilder.putList("languages", value);
                return value;
            },
            Collections.emptyList()
        );

        private final Parameter<Map<String, Object>> languageToMap = mapParam(
            "language_to",
            true,
            m -> builder(m).languageToMap.getValue(),
            Collections.emptyMap()
        ).neverSerialize();

        private final Parameter<Integer> positionIncrementGap = Parameter.intParam(
            "position_increment_gap",
            true,
            m -> builder(m).positionIncrementGap.getValue(),
            -1
        );

        final IndexAnalyzers indexAnalyzers;
        final NamedAnalyzer indexAnalyzer;
        final NamedAnalyzer searchAnalyzer;
        final NamedAnalyzer searchQuoteAnalyzer;

        public Builder(String name, IndexAnalyzers indexAnalyzers) {
            super(name);
            this.indexAnalyzers = indexAnalyzers;
            this.indexAnalyzer = indexAnalyzers.getDefaultIndexAnalyzer();
            this.searchAnalyzer = indexAnalyzers.getDefaultSearchAnalyzer();
            this.searchQuoteAnalyzer = indexAnalyzers.getDefaultSearchQuoteAnalyzer();
        }

        @Override
        protected List<Parameter<?>> getParameters() {
            return Arrays.asList(store, positionIncrementGap,
                                 nTrial, alpha, alphaWidth, iterationLimit, probThreshold, convThreshold,
                                 baseFreq, filterPattern, max, binary, map, languages, profile, languageToMap);
        }

        public NamedAnalyzer indexAnalyzer() {
            return indexAnalyzer();
        }

        public NamedAnalyzer searchAnalyzer() {
            return searchAnalyzer();
        }

        public NamedAnalyzer searchQuoteAnalyzer() {
            return searchQuoteAnalyzer();
        }

        @Override
        public LangdetectMapper build(ContentPath context) {
            return new LangdetectMapper(name,
                                        Defaults.FIELD_TYPE,
                                        new LangdetectFieldType(buildFullName(context)),
                                        multiFieldsBuilder.build(this, context),
                                        copyTo.build(),
                                        this
                                        );
        }
    }

    public static class LangdetectFieldType extends SimpleMappedFieldType {

        public LangdetectFieldType(String name) {
            super(name, true, false, true, TextSearchInfo.NONE, Collections.emptyMap());
        }

        /** Returns the indexed value used to construct search "values".
         *  This method is used for the default implementations of most
         *  query factory methods such as {@link #termQuery}. */
        protected BytesRef indexedValueForSearch(Object value) {
            return BytesRefs.toBytesRef(value);
        }

        @Override
        public ValueFetcher valueFetcher(QueryShardContext context, String format) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        @Override
        public Query existsQuery(QueryShardContext context) {
            return new TermQuery(new Term(FieldNamesFieldMapper.NAME, name()));
        }

        @Override
        public Query termQuery(Object value, QueryShardContext context) {
            failIfNotIndexed();
            Query query = new TermQuery(new Term(name(), indexedValueForSearch(value)));
            if (boost() != 1f) {
                query = new BoostQuery(query, boost());
            }
            return query;
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

    }

    public static final FieldMapper.TypeParser PARSER
        = new FieldMapper.TypeParser((n, c) -> new LangdetectMapper.Builder(n, c.getIndexAnalyzers()));

    public static class LanguageTo implements ToXContent {

        private final Map<String, Object> languageToFields;

        private LanguageTo(Map<String, Object> languageToFields) {
            this.languageToFields = languageToFields;
        }

        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            if (!languageToFields.isEmpty()) {
                builder.startObject("language_to");
                for (Map.Entry<String, Object> field : languageToFields.entrySet()) {
                    builder.field(field.getKey(), field.getValue());
                }
                builder.endObject();
            }
            return builder;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final Map<String, Object> languageToBuilders = new LinkedHashMap<>();

            public LanguageTo.Builder add(String language, String field) {
                languageToBuilders.put(language, field);
                return this;
            }

            public LanguageTo.Builder add(Map<String, Object> map) {
                languageToBuilders.putAll(map);
                return this;
            }

            public LanguageTo build() {
                return new LanguageTo(Collections.unmodifiableMap(languageToBuilders));
            }
        }

        public Map<String, Object> languageToFields() {
            return languageToFields;
        }
    }

    public static Parameter<Map<String, Object>> mapParam(
        String name, boolean updateable,
        Function<FieldMapper, Map<String, Object>> initializer, Map<String, Object> defaultValue)
    {
        return new Parameter<>(
            name,
            updateable,
            () -> defaultValue,
            (n, c, o) -> XContentMapValues.nodeMapValue(o, "map"),
            initializer
        );
    }

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
