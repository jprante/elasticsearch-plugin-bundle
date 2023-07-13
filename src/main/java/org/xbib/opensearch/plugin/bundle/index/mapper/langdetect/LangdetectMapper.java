package org.xbib.opensearch.plugin.bundle.index.mapper.langdetect;

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
import org.opensearch.common.lucene.BytesRefs;
import org.opensearch.common.lucene.Lucene;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.support.XContentMapValues;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.index.analysis.NamedAnalyzer;
import org.opensearch.index.mapper.FieldMapper;
import org.opensearch.index.mapper.FieldNamesFieldMapper;
import org.opensearch.index.mapper.MappedFieldType;
import org.opensearch.index.mapper.Mapper;
import org.opensearch.index.mapper.MapperParsingException;
import org.opensearch.index.mapper.ParseContext;
import org.opensearch.index.mapper.SimpleMappedFieldType;
import org.opensearch.index.mapper.TextSearchInfo;
import org.opensearch.index.mapper.ValueFetcher;
import org.opensearch.index.query.QueryShardContext;
import org.opensearch.search.lookup.SearchLookup;
import org.xbib.opensearch.plugin.bundle.common.langdetect.LangdetectService;
import org.xbib.opensearch.plugin.bundle.common.langdetect.Language;
import org.xbib.opensearch.plugin.bundle.common.langdetect.LanguageDetectionException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Language detection field mapper.
 */
public class LangdetectMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "langdetect";

    public static final FieldType FIELD_TYPE = new FieldType();

    private static final Logger logger = LogManager.getLogger(LangdetectMapper.class);

    static {
        FIELD_TYPE.setStored(true);
        FIELD_TYPE.setOmitNorms(true);
        FIELD_TYPE.setDocValuesType(DocValuesType.NONE);
        FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
        FIELD_TYPE.freeze();
    }

    private final LangdetectService langdetectService;

    private final LanguageTo languageTo;
    private ParseContext context;

    public LangdetectMapper(String simpleName,
                            FieldType fieldType,
                            MappedFieldType mappedFieldType,
                            MultiFields multiFields,
                            CopyTo copyTo,
                            LanguageTo languageTo,
                            LangdetectService langdetectService) {
        super(simpleName, fieldType, mappedFieldType, multiFields, copyTo);
        this.langdetectService = langdetectService;
        this.languageTo = languageTo;
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    protected void mergeOptions(FieldMapper other, List<String> conflicts) {
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
        boolean isBinary = langdetectService.getSettings().getAsBoolean("binary", false);
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
            List<Language> langs = langdetectService.detectAll(value);
            for (Language lang : langs) {
                Field field = new Field(fieldType().name(), lang.getLanguage(), fieldType);
                context.doc().add(field);
                if (languageTo.languageToFields().containsKey(lang.getLanguage())) {
                    parseLanguageToFields(context, languageTo.languageToFields().get(lang.getLanguage()));
                }
            }
        } catch (LanguageDetectionException e) {
            logger.warn("upps", e);
            context.createExternalValueContext("unknown");
        }
    }

    private static Iterable<String> extractFieldNames(final String fullPath) {
        return () -> new Iterator<>() {

            int endIndex = nextEndIndex(0);

            private int nextEndIndex(int index) {
                while (index < fullPath.length() && fullPath.charAt(index) != '.') {
                    index += 1;
                }
                return index;
            }

            @Override
            public boolean hasNext() {
                return endIndex <= fullPath.length();
            }

            @Override
            public String next() {
                final String result = fullPath.substring(0, endIndex);
                endIndex = nextEndIndex(endIndex + 1);
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        langdetectService.getSettings().toXContent(builder, params);
        languageTo.toXContent(builder, params);
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

    public static class Builder extends FieldMapper.Builder<Builder> {

        protected int positionIncrementGap = -1;

        protected LanguageTo languageTo = LanguageTo.builder().build();

        protected Settings.Builder settingsBuilder = Settings.builder();

        public Builder(String name) {
            super(name, FIELD_TYPE);
            this.builder = this;
        }

        public NamedAnalyzer indexAnalyzer() {
            return indexAnalyzer;
        }

        public NamedAnalyzer searchAnalyzer() {
            return searchAnalyzer;
        }

        public NamedAnalyzer searchQuoteAnalyzer() {
            return searchQuoteAnalyzer;
        }

        public Builder positionIncrementGap(int positionIncrementGap) {
            this.positionIncrementGap = positionIncrementGap;
            return this;
        }

        public Builder ntrials(int trials) {
            settingsBuilder.put("number_of_trials", trials);
            return this;
        }

        public Builder alpha(double alpha) {
            settingsBuilder.put("alpha", alpha);
            return this;
        }

        public Builder alphaWidth(double alphaWidth) {
            settingsBuilder.put("alpha_width", alphaWidth);
            return this;
        }

        public Builder iterationLimit(int iterationLimit) {
            settingsBuilder.put("iteration_limit", iterationLimit);
            return this;
        }

        public Builder probThreshold(double probThreshold) {
            settingsBuilder.put("prob_threshold", probThreshold);
            return this;
        }

        public Builder convThreshold(double convThreshold) {
            settingsBuilder.put("conv_threshold", convThreshold);
            return this;
        }

        public Builder baseFreq(int baseFreq) {
            settingsBuilder.put("base_freq", baseFreq);
            return this;
        }

        public Builder pattern(String pattern) {
            settingsBuilder.put("pattern", pattern);
            return this;
        }

        public Builder max(int max) {
            settingsBuilder.put("max", max);
            return this;
        }

        public Builder binary(boolean binary) {
            settingsBuilder.put("binary", binary);
            return this;
        }

        public Builder map(Map<String, Object> map) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                settingsBuilder.put("map." + entry.getKey(), (String) entry.getValue());
            }
            return this;
        }

        public Builder languages(String[] languages) {
            List<String> list = Arrays.asList(languages);
            settingsBuilder.putList("languages", list);
            return this;
        }

        public Builder profile(String profile) {
            settingsBuilder.put("profile", profile);
            return this;
        }

        public Builder languageTo(LanguageTo languageTo) {
            this.languageTo = languageTo;
            return this;
        }

        @Override
        public LangdetectMapper build(BuilderContext context) {
            LangdetectService service = new LangdetectService(settingsBuilder.build());
            return new LangdetectMapper(name,
                                        fieldType,
                                        new LangdetectFieldType(buildFullName(context)),
                                        multiFieldsBuilder.build(this, context),
                                        copyTo,
                                        languageTo,
                                        service);
        }
    }

    public static class LangdetectFieldType extends SimpleMappedFieldType {

        public LangdetectFieldType(String name) {
            super(name, true, false, true, TextSearchInfo.NONE, Collections.emptyMap());
            setIndexAnalyzer(Lucene.KEYWORD_ANALYZER);
        }

        /** Returns the indexed value used to construct search "values".
         *  This method is used for the default implementations of most
         *  query factory methods such as {@link #termQuery}. */
        protected BytesRef indexedValueForSearch(Object value) {
            return BytesRefs.toBytesRef(value);
        }

        @Override
        public ValueFetcher valueFetcher(QueryShardContext context, SearchLookup searchLookup, String format) {
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

    public static class TypeParser implements Mapper.TypeParser {

        @Override
        public Mapper.Builder<?> parse(String name, Map<String, Object> mapping, ParserContext parserContext) {
            Builder builder = new Builder(name);
            Iterator<Map.Entry<String, Object>> iterator = mapping.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "analyzer":
                    case "include_in_all":
                        iterator.remove();
                        break;
                    case "position_increment_gap":
                        int newPositionIncrementGap = XContentMapValues.nodeIntegerValue(fieldNode, -1);
                        if (newPositionIncrementGap < 0) {
                            throw new MapperParsingException("position_increment_gap less than 0 aren't allowed.");
                        }
                        builder.positionIncrementGap(newPositionIncrementGap);
                        if (builder.indexAnalyzer() == null) {
                            builder.indexAnalyzer(parserContext.getIndexAnalyzers().getDefaultIndexAnalyzer());
                        }
                        if (builder.searchAnalyzer() == null) {
                            builder.searchAnalyzer(parserContext.getIndexAnalyzers().getDefaultSearchAnalyzer());
                        }
                        if (builder.searchQuoteAnalyzer() == null) {
                            builder.searchQuoteAnalyzer(parserContext.getIndexAnalyzers().getDefaultSearchQuoteAnalyzer());
                        }
                        iterator.remove();
                        break;
                    case "store":
                        builder.store(XContentMapValues.nodeBooleanValue(fieldNode));
                        iterator.remove();
                        break;
                    case "number_of_trials":
                        builder.ntrials(XContentMapValues.nodeIntegerValue(fieldNode));
                        iterator.remove();
                        break;
                    case "alpha":
                        builder.alpha(XContentMapValues.nodeDoubleValue(fieldNode));
                        iterator.remove();
                        break;
                    case "alpha_width":
                        builder.alphaWidth(XContentMapValues.nodeDoubleValue(fieldNode));
                        iterator.remove();
                        break;
                    case "iteration_limit":
                        builder.iterationLimit(XContentMapValues.nodeIntegerValue(fieldNode));
                        iterator.remove();
                        break;
                    case "prob_threshold":
                        builder.probThreshold(XContentMapValues.nodeDoubleValue(fieldNode));
                        iterator.remove();
                        break;
                    case "conv_threshold":
                        builder.convThreshold(XContentMapValues.nodeDoubleValue(fieldNode));
                        iterator.remove();
                        break;
                    case "base_freq":
                        builder.baseFreq(XContentMapValues.nodeIntegerValue(fieldNode));
                        iterator.remove();
                        break;
                    case "pattern":
                        builder.pattern(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "max":
                        builder.max(XContentMapValues.nodeIntegerValue(fieldNode));
                        iterator.remove();
                        break;
                    case "binary":
                        builder.binary(XContentMapValues.nodeBooleanValue(fieldNode));
                        iterator.remove();
                        break;
                    case "map":
                        builder.map(XContentMapValues.nodeMapValue(fieldNode, "map"));
                        iterator.remove();
                        break;
                    case "languages":
                        builder.languages(XContentMapValues.nodeStringArrayValue(fieldNode));
                        iterator.remove();
                        break;
                    case "profile":
                        builder.profile(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "language_to" :
                        Map<String, Object> map = XContentMapValues.nodeMapValue(fieldNode, null);
                        LanguageTo.Builder languageToBuilder = LanguageTo.builder();
                        languageToBuilder.add(map);
                        builder.languageTo(languageToBuilder.build());
                        iterator.remove();
                        break;
                    default:
                        break;
                }
            }
            return builder;
        }
    }

    public static class LanguageTo {

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

}
