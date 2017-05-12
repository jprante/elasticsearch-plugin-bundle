package org.xbib.elasticsearch.index.mapper.icu;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RawCollationKey;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.index.fielddata.IndexFieldData;
import org.elasticsearch.index.fielddata.plain.DocValuesIndexFieldData;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.StringFieldType;
import org.elasticsearch.index.mapper.TypeParsers;
import org.elasticsearch.search.DocValueFormat;
import org.joda.time.DateTimeZone;
import org.xbib.elasticsearch.index.analysis.icu.IcuCollationKeyAnalyzerProvider;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

import static org.xbib.elasticsearch.index.analysis.icu.IndexableBinaryStringTools.decode;
import static org.xbib.elasticsearch.index.analysis.icu.IndexableBinaryStringTools.encode;
import static org.xbib.elasticsearch.index.analysis.icu.IndexableBinaryStringTools.getDecodedLength;
import static org.xbib.elasticsearch.index.analysis.icu.IndexableBinaryStringTools.getEncodedLength;

/**
 * ICU collation key field mapper.
 */
public class IcuCollationKeyFieldMapper extends FieldMapper {

    public static final String MAPPER_TYPE = "icu_collation_key";

    public static class Defaults {
        public static final MappedFieldType FIELD_TYPE = new CollationFieldType();

        static {
            FIELD_TYPE.setTokenized(false);
            FIELD_TYPE.setOmitNorms(true);
            FIELD_TYPE.setIndexOptions(IndexOptions.DOCS);
            FIELD_TYPE.freeze();
        }

        public static final String NULL_VALUE = null;
    }

    public static final class CollationFieldType extends StringFieldType {
        private Collator collator = null;

        public CollationFieldType() {
            setIndexAnalyzer(Lucene.KEYWORD_ANALYZER);
            setSearchAnalyzer(Lucene.KEYWORD_ANALYZER);
        }

        protected CollationFieldType(CollationFieldType ref) {
            super(ref);
            this.collator = ref.collator;
        }

        public CollationFieldType clone() {
            return new CollationFieldType(this);
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o) && Objects.equals(collator, ((CollationFieldType) o).collator);
        }

        @Override
        public void checkCompatibility(MappedFieldType otherFT, List<String> conflicts, boolean strict) {
            super.checkCompatibility(otherFT, conflicts, strict);
            CollationFieldType other = (CollationFieldType) otherFT;
            if (!Objects.equals(collator, other.collator)) {
                conflicts.add("mapper [" + name() + "] has different [collator]");
            }
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Objects.hashCode(collator);
        }

        @Override
        public String typeName() {
            return MAPPER_TYPE;
        }

        public Collator collator() {
            return collator;
        }

        public void setCollator(Collator collator) {
            checkIfFrozen();
            this.collator = collator.isFrozen() ? collator : collator.freeze();
        }

        @Override
        public Query nullValueQuery() {
            if (nullValue() == null) {
                return null;
            }
            return termQuery(nullValue(), null);
        }

        @Override
        public IndexFieldData.Builder fielddataBuilder() {
            failIfNoDocValues();
            return new DocValuesIndexFieldData.Builder();
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

        public static DocValueFormat COLLATE_FORMAT = new DocValueFormat() {
            @Override
            public String getWriteableName() {
                return "collate";
            }

            @Override
            public void writeTo(StreamOutput out) throws IOException {
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
                int encodedLength = getEncodedLength(value.bytes, value.offset, value.length);
                char[] encoded = new char[encodedLength];
                encode(value.bytes, value.offset, value.length, encoded, 0, encodedLength);
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
                int decodedLength = getDecodedLength(encoded, 0, encoded.length);
                byte[] decoded = new byte[decodedLength];
                decode(encoded, 0, encoded.length, decoded, 0, decodedLength);
                return new BytesRef(decoded);
            }
        };

        @Override
        public DocValueFormat docValueFormat(final String format, final DateTimeZone timeZone) {
            return COLLATE_FORMAT;
        }
    }

    public static class Builder extends FieldMapper.Builder<Builder, IcuCollationKeyFieldMapper> {
        private Settings.Builder settingsBuilder;

        public Builder(String name) {
            super(name, Defaults.FIELD_TYPE, Defaults.FIELD_TYPE);
            builder = this;
            this.settingsBuilder = Settings.builder();
        }

        @Override
        public CollationFieldType fieldType() {
            return (CollationFieldType) super.fieldType();
        }

        @Override
        public Builder indexOptions(IndexOptions indexOptions) {
            if (indexOptions.compareTo(IndexOptions.DOCS_AND_FREQS) > 0) {
                throw new IllegalArgumentException("The [" + MAPPER_TYPE + "] field does not support positions, got [index_options]="
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
            return settingsBuilder.get("caseLevel") != null && Boolean.parseBoolean(settingsBuilder.get("caseLevel"));
        }

        public Builder caseLevel(final boolean caseLevel) {
            settingsBuilder.put("caseLevel", caseLevel);
            return this;
        }

        public String caseFirst() {
            return settingsBuilder.get("caseFirst");
        }

        public Builder caseFirst(final String caseFirst) {
            settingsBuilder.put("caseFirst", caseFirst);
            return this;
        }

        public boolean numeric() {
            return settingsBuilder.get("numeric") != null && Boolean.parseBoolean(settingsBuilder.get("numeric"));
        }

        public Builder numeric(final boolean numeric) {
            settingsBuilder.put("numeric", numeric);
            return this;
        }

        public Collator buildCollator() {
            return IcuCollationKeyAnalyzerProvider.createCollator(settingsBuilder.build());
        }

        @Override
        public IcuCollationKeyFieldMapper build(BuilderContext context) {
            final Collator collator = buildCollator();
            fieldType().setCollator(collator);
            setupFieldType(context);
            return new IcuCollationKeyFieldMapper(name, fieldType, defaultFieldType, context.indexSettings(),
                multiFieldsBuilder.build(this, context), copyTo, settingsBuilder.build(), collator);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {
        @Override
        public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext)
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

    protected IcuCollationKeyFieldMapper(String simpleName, MappedFieldType fieldType, MappedFieldType defaultFieldType,
                                         Settings indexSettings, MultiFields multiFields,
                                         CopyTo copyTo, Settings collatorSettings, Collator collator) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);
        assert collator.isFrozen();
        this.collatorSettings = collatorSettings;
        this.collator = collator;
    }

    @Override
    public CollationFieldType fieldType() {
        return (CollationFieldType) super.fieldType();
    }

    @Override
    protected String contentType() {
        return MAPPER_TYPE;
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        if (includeDefaults || fieldType().nullValue() != null) {
            builder.field("null_value", fieldType().nullValue());
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
            builder.field("caseLevel", collatorSettings.getAsBoolean("caseLevel", false));
        }
        if (includeDefaults) {
            builder.field("caseFirst", collatorSettings.get("caseFirst"));
        }
        if (includeDefaults) {
            builder.field("numeric", collatorSettings.getAsBoolean("numeric", false));
        }
    }

    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
        final String value;
        if (context.externalValueSet()) {
            value = context.externalValue().toString();
        } else {
            XContentParser parser = context.parser();
            if (parser.currentToken() == XContentParser.Token.VALUE_NULL) {
                value = fieldType().nullValueAsString();
            } else {
                value = parser.textOrNull();
            }
        }
        if (value == null) {
            return;
        }
        RawCollationKey key = collator.getRawCollationKey(value, null);
        final BytesRef binaryValue = new BytesRef(key.bytes, 0, key.size);
        if (fieldType().indexOptions() != IndexOptions.NONE || fieldType().stored()) {
            Field field = new Field(fieldType().name(), binaryValue, fieldType());
            fields.add(field);
        }
        if (fieldType().hasDocValues()) {
            fields.add(new SortedDocValuesField(fieldType().name(), binaryValue));
        }
    }
}
