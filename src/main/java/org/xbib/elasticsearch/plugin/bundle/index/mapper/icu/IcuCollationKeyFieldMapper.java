package org.xbib.elasticsearch.plugin.bundle.index.mapper.icu;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RawCollationKey;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocValuesFieldExistsQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.plain.SortedSetOrdinalsIndexFieldData;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.FieldNamesFieldMapper;
import org.elasticsearch.index.mapper.KeywordFieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.StringFieldType;
import org.elasticsearch.index.mapper.TextSearchInfo;
import org.elasticsearch.index.mapper.TypeParsers;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.support.CoreValuesSourceType;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IcuCollationKeyAnalyzerProvider;
import org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.IndexableBinaryStringTools;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * ICU collation key field mapper.
 */
public class IcuCollationKeyFieldMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "icu_collation_key";

    public static final FieldType FIELD_TYPE = new FieldType();

    public static class Defaults {

        static {
            FIELD_TYPE.setTokenized(false);
            FIELD_TYPE.setOmitNorms(true);
            FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            FIELD_TYPE.freeze();
        }
    }

    public static final class CollationFieldType extends StringFieldType {

        private final Collator collator;

        public CollationFieldType(String name, boolean isSearchable, boolean hasDocValues, Collator collator, Map<String, String> meta) {
            super(name, true, true, TextSearchInfo.SIMPLE_MATCH_ONLY, meta);
            setIndexAnalyzer(Lucene.KEYWORD_ANALYZER);
            this.collator = collator;
        }

        public CollationFieldType(String name, Collator collator) {
            this(name, true, true, collator, Collections.emptyMap());
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Objects.hashCode(collator);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            CollationFieldType that = (CollationFieldType) o;

            return collator != null ? collator.equals(that.collator) : that.collator == null;
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        public Collator collator() {
            return collator;
        }

        @Override
        public Query existsQuery(QueryShardContext context) {
            if (hasDocValues()) {
                return new DocValuesFieldExistsQuery(name());
            } else {
                return new TermQuery(new Term(FieldNamesFieldMapper.NAME, name()));
            }
        }

        @Override
        public IndexFieldData.Builder fielddataBuilder(String fullyQualifiedIndexName) {
            failIfNoDocValues();
            return new SortedSetOrdinalsIndexFieldData.Builder(CoreValuesSourceType.BYTES);
        }

        @Override
        protected BytesRef indexedValueForSearch(Object value) {
            if (value == null) {
                return null;
            }
            if (value instanceof BytesRef) {
                value = ((BytesRef) value).utf8ToString();
            }
            if (collator != null) {
                RawCollationKey key = collator.getRawCollationKey(value.toString(), null);
                return new BytesRef(key.bytes, 0, key.size);
            } else {
                throw new IllegalStateException("collator is null");
            }
        }

        @Override
        public Query fuzzyQuery(Object value, Fuzziness fuzziness, int prefixLength, int maxExpansions,
                                boolean transpositions, QueryShardContext context) {
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

        @Override
        public DocValueFormat docValueFormat(final String format, ZoneId timeZone) {
            return COLLATE_FORMAT;
        }

        public static DocValueFormat COLLATE_FORMAT = new DocValueFormat() {
            @Override
            public String getWriteableName() {
                return "collate";
            }

            @Override
            public void writeTo(StreamOutput out) {
            }

            @Override
            public String format(long value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String format(double value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String format(BytesRef value) {
                int encodedLength = IndexableBinaryStringTools.getEncodedLength(value.bytes, value.offset, value.length);
                char[] encoded = new char[encodedLength];
                IndexableBinaryStringTools.encode(value.bytes, value.offset, value.length, encoded, 0, encodedLength);
                return new String(encoded, 0, encodedLength);
            }

            @Override
            public long parseLong(String value, boolean roundUp, LongSupplier now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public double parseDouble(String value, boolean roundUp, LongSupplier now) {
                throw new UnsupportedOperationException();
            }

            @Override
            public BytesRef parseBytesRef(String value) {
                char[] encoded = value.toCharArray();
                int decodedLength = IndexableBinaryStringTools.getDecodedLength(encoded, 0, encoded.length);
                byte[] decoded = new byte[decodedLength];
                IndexableBinaryStringTools.decode(encoded, 0, encoded.length, decoded, 0, decodedLength);
                return new BytesRef(decoded);
            }
        };
    }

    public static class Builder extends FieldMapper.Builder<Builder> {
        private Settings.Builder settingsBuilder;
        protected String nullValue;

        public Builder(String name) {
            super(name, FIELD_TYPE);
            builder = this;
            this.settingsBuilder = Settings.builder();
        }

        @Override
        public Builder indexOptions(IndexOptions indexOptions) {
            if (indexOptions.compareTo(IndexOptions.DOCS_AND_FREQS) > 0) {
                throw new IllegalArgumentException("The [" + CONTENT_TYPE + "] field does not support positions, got [index_options]="
                    + indexOptionToString(indexOptions));
            }
            return super.indexOptions(indexOptions);
        }

        public String rules() {
            return settingsBuilder.get("rules");
        }

        public Builder rules(final String rules) {
            settingsBuilder.put("rules", rules);
            return this;
        }

        public String language() {
            return settingsBuilder.get("language");
        }

        public Builder language(final String language) {
            settingsBuilder.put("language", language);
            return this;
        }

        public String country() {
            return settingsBuilder.get("country");
        }

        public Builder country(final String country) {
            settingsBuilder.put("country", country);
            return this;
        }

        public String variant() {
            return settingsBuilder.get("variant");
        }

        public Builder variant(final String variant) {
            settingsBuilder.put("variant", variant);
            return this;
        }

        public String strength() {
            return settingsBuilder.get("strength");
        }

        public Builder strength(final String strength) {
            settingsBuilder.put("strength", strength);
            return this;
        }

        public String decomposition() {
            return settingsBuilder.get("decomposition");
        }

        public Builder decomposition(final String decomposition) {
            settingsBuilder.put("decomposition", decomposition);
            return this;
        }

        public String alternate() {
            return settingsBuilder.get("alternate");
        }

        public Builder alternate(final String alternate) {
            settingsBuilder.put("alternate", alternate);
            return this;
        }

        public boolean caseLevel() {
            return settingsBuilder.get("case_level") != null && Boolean.parseBoolean(settingsBuilder.get("case_level"));
        }

        public Builder caseLevel(final boolean caseLevel) {
            settingsBuilder.put("case_level", caseLevel);
            return this;
        }

        public String caseFirst() {
            return settingsBuilder.get("case_first");
        }

        public Builder caseFirst(final String caseFirst) {
            settingsBuilder.put("case_first", caseFirst);
            return this;
        }

        public boolean numeric() {
            return settingsBuilder.get("numeric") != null && Boolean.parseBoolean(settingsBuilder.get("numeric"));
        }

        public Builder numeric(final boolean numeric) {
            settingsBuilder.put("numeric", numeric);
            return this;
        }

        public Builder nullValue(String nullValue) {
            this.nullValue = nullValue;
            return this;
        }

        public Collator buildCollator() {
            return IcuCollationKeyAnalyzerProvider.createCollator(settingsBuilder.build());
        }

        @Override
        public IcuCollationKeyFieldMapper build(BuilderContext context) {
            final Collator collator = buildCollator();
            CollationFieldType ft = new CollationFieldType(buildFullName(context), indexed, hasDocValues, collator, meta);
            return new IcuCollationKeyFieldMapper(name, fieldType, ft,
                multiFieldsBuilder.build(this, context), copyTo, settingsBuilder.build(), collator, nullValue);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {
        @Override
        public Mapper.Builder<?> parse(String name, Map<String, Object> node, ParserContext parserContext)
            throws MapperParsingException {
            Builder builder = new Builder(name);
            TypeParsers.parseField(builder, name, node, parserContext);
            for (Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "null_value":
                        if (fieldNode == null) {
                            throw new MapperParsingException("property [null_value] cannot be null");
                        }
                        builder.nullValue(fieldNode.toString());
                        iterator.remove();
                        break;
                    case "norms":
                        builder.omitNorms(!XContentMapValues.nodeBooleanValue(fieldNode));
                        iterator.remove();
                        break;
                    case "rules":
                        builder.rules(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "language":
                        builder.language(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "country":
                        builder.country(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "variant":
                        builder.variant(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "strength":
                        builder.strength(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "decomposition":
                        builder.decomposition(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "alternate":
                        builder.alternate(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "case_level":
                        builder.caseLevel(XContentMapValues.nodeBooleanValue(fieldNode));
                        iterator.remove();
                        break;
                    case "case_first":
                        builder.caseFirst(XContentMapValues.nodeStringValue(fieldNode, null));
                        iterator.remove();
                        break;
                    case "numeric":
                        builder.numeric(XContentMapValues.nodeBooleanValue(fieldNode));
                        iterator.remove();
                        break;
                    default:
                        break;
                }
            }
            return builder;
        }
    }

    private final Settings collatorSettings;
    private final Collator collator;
    private final String nullValue;

    protected IcuCollationKeyFieldMapper(String simpleName, FieldType fieldType, MappedFieldType defaultFieldType,
                                         MultiFields multiFields,
                                         CopyTo copyTo, Settings collatorSettings, Collator collator, String nullValue) {
        super(simpleName, fieldType, defaultFieldType, multiFields, copyTo);
        assert collator.isFrozen();
        this.collatorSettings = collatorSettings;
        this.collator = collator;
        this.nullValue = nullValue;
    }

    @Override
    public CollationFieldType fieldType() {
        return (CollationFieldType) super.fieldType();
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    protected void mergeOptions(FieldMapper other, List<String> conflicts) {
        IcuCollationKeyFieldMapper icuMergeWith = (IcuCollationKeyFieldMapper) other;
        if (!Objects.equals(collator, icuMergeWith.collator)) {
            conflicts.add("mapper [" + name() + "] has different [collator]");
        }
        if (!Objects.equals(collatorSettings.get("rules"), icuMergeWith.collatorSettings.get("rules"))) {
            conflicts.add("Cannot update rules setting for [" + CONTENT_TYPE + "]");
        }
        if (!Objects.equals(collatorSettings.get("language"), icuMergeWith.collatorSettings.get("language"))) {
            conflicts.add("Cannot update language setting for [" + CONTENT_TYPE + "]");
        }
        if (!Objects.equals(collatorSettings.get("country"), icuMergeWith.collatorSettings.get("country"))) {
            conflicts.add("Cannot update country setting for [" + CONTENT_TYPE + "]");
        }
        if (!Objects.equals(collatorSettings.get("variant"), icuMergeWith.collatorSettings.get("variant"))) {
            conflicts.add("Cannot update variant setting for [" + CONTENT_TYPE + "]");
        }
        if (!Objects.equals(collatorSettings.get("strength"), icuMergeWith.collatorSettings.get("strength"))) {
            conflicts.add("Cannot update strength setting for [" + CONTENT_TYPE + "]");
        }
        if (!Objects.equals(collatorSettings.get("decomposition"), icuMergeWith.collatorSettings.get("decomposition"))) {
            conflicts.add("Cannot update decomposition setting for [" + CONTENT_TYPE + "]");
        }
        if (!Objects.equals(collatorSettings.get("alternate"), icuMergeWith.collatorSettings.get("alternate"))) {
            conflicts.add("Cannot update alternate setting for [" + CONTENT_TYPE + "]");
        }
        if (collatorSettings.getAsBoolean("case_level", true) != icuMergeWith.collatorSettings.getAsBoolean("case_level", true)) {
            conflicts.add("Cannot update case_level setting for [" + CONTENT_TYPE + "]");
        }
        if (!Objects.equals(collatorSettings.get("case_first"), icuMergeWith.collatorSettings.get("case_first"))) {
            conflicts.add("Cannot update case_first setting for [" + CONTENT_TYPE + "]");
        }
        if (collatorSettings.getAsBoolean("numeric", true) != icuMergeWith.collatorSettings.getAsBoolean("numeric", true)) {
            conflicts.add("Cannot update numeric setting for [" + CONTENT_TYPE + "]");
        }
    }


    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        if (fieldType.indexOptions() != IndexOptions.NONE && (includeDefaults || fieldType.indexOptions() != IndexOptions.DOCS)) {
            builder.field("index_options", indexOptionToString(fieldType.indexOptions()));
        }
        if (nullValue != null) {
            builder.field("null_value", nullValue);
        }
        if (includeDefaults || fieldType.omitNorms() != KeywordFieldMapper.Defaults.FIELD_TYPE.omitNorms()) {
            builder.field("norms", fieldType.omitNorms() == false);
        }
        if (includeDefaults) {
            builder.field("rules", collatorSettings.get("rules"));
        }
        if (includeDefaults) {
            builder.field("language", collatorSettings.get("language"));
        }
        if (includeDefaults) {
            builder.field("country", collatorSettings.get("country"));
        }
        if (includeDefaults) {
            builder.field("variant", collatorSettings.get("variant"));
        }
        if (includeDefaults) {
            builder.field("strength", collatorSettings.get("strength"));
        }
        if (includeDefaults) {
            builder.field("decomposition", collatorSettings.get("decomposition"));
        }
        if (includeDefaults) {
            builder.field("alternate", collatorSettings.get("alternate"));
        }
        if (includeDefaults) {
            builder.field("case_level", collatorSettings.getAsBoolean("case_level", false));
        }
        if (includeDefaults) {
            builder.field("case_first", collatorSettings.get("case_first"));
        }
        if (includeDefaults) {
            builder.field("numeric", collatorSettings.getAsBoolean("numeric", false));
        }
    }

    @Override
    protected void parseCreateField(ParseContext context) throws IOException {
        final String value;
        if (context.externalValueSet()) {
            value = context.externalValue().toString();
        } else {
            XContentParser parser = context.parser();
            if (parser.currentToken() == XContentParser.Token.VALUE_NULL) {
                value = nullValue;
            } else {
                value = parser.textOrNull();
            }
        }
        if (value == null) {
            return;
        }
        RawCollationKey key = collator.getRawCollationKey(value, null);
        final BytesRef binaryValue = new BytesRef(key.bytes, 0, key.size);
        if (fieldType.indexOptions() != IndexOptions.NONE || fieldType.stored()) {
            Field field = new Field(mappedFieldType.name(), binaryValue, fieldType);
            context.doc().add(field);
        }
        if (fieldType().hasDocValues()) {
            context.doc().add(new SortedSetDocValuesField(fieldType().name(), binaryValue));
        } else if (fieldType.indexOptions() != IndexOptions.NONE || fieldType.stored()) {
            createFieldNamesField(context);
        }
    }
}
