package org.xbib.elasticsearch.index.mapper.crypt;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.StringFieldMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.core.TypeParsers.parseField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parseMultiField;

public class CryptMapper extends StringFieldMapper {

    public static final String CONTENT_TYPE = "crypt";

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    public static class Builder extends StringFieldMapper.Builder {

        private String algo;

        Builder(String name) {
            super(name);
            this.builder = this;
            this.algo = "SHA-256";
        }

        Builder algo(String algo) {
            this.algo = algo;
            return this;
        }

        @Override
        public CryptMapper build(Mapper.BuilderContext context) {
            if (positionOffsetGap > 0) {
                fieldType.setIndexAnalyzer(new NamedAnalyzer(fieldType.indexAnalyzer(), positionOffsetGap));
                fieldType.setSearchAnalyzer(new NamedAnalyzer(fieldType.searchAnalyzer(), positionOffsetGap));
                fieldType.setSearchQuoteAnalyzer(new NamedAnalyzer(fieldType.searchQuoteAnalyzer(), positionOffsetGap));
            }
            if (fieldType.indexOptions() != IndexOptions.NONE && !fieldType.tokenized()) {
                defaultFieldType.setOmitNorms(true);
                defaultFieldType.setIndexOptions(IndexOptions.DOCS);
                if (!omitNormsSet && fieldType.boost() == 1.0f) {
                    fieldType.setOmitNorms(true);
                }
                if (!indexOptionsSet) {
                    fieldType.setIndexOptions(IndexOptions.DOCS);
                }
            }
            setupFieldType(context);
            CryptMapper fieldMapper = new CryptMapper(
                    name, fieldType, defaultFieldType, positionOffsetGap, ignoreAbove,
                    context.indexSettings(), multiFieldsBuilder.build(this, context), copyTo, algo);
            fieldMapper.includeInAll(includeInAll);
            return fieldMapper;
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, Mapper.TypeParser.ParserContext parserContext)
                throws MapperParsingException {
            Builder builder = new Builder(name);
            parseField(builder, name, node, parserContext);
            Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String propName = Strings.toUnderscoreCase(entry.getKey());
                Object propNode = entry.getValue();
                if (propName.equals("algo")) {
                    builder.algo(propNode.toString());
                    iterator.remove();
                } else {
                    parseMultiField(builder, name, parserContext, propName, propNode);
                }
            }
            return builder;
        }
    }

    private int ignoreAbove;
    private String algo;

    public CryptMapper(String simpleName, MappedFieldType fieldType, MappedFieldType defaultFieldType,
            int positionOffsetGap, int ignoreAbove,
            Settings indexSettings, FieldMapper.MultiFields multiFields, FieldMapper.CopyTo copyTo,
            String algo) {
        super(simpleName, fieldType, defaultFieldType,
                positionOffsetGap, ignoreAbove,
                indexSettings, multiFields, copyTo);
        this.ignoreAbove = ignoreAbove;
        this.algo = algo;
    }

    @Override
    protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {
        StringFieldMapper.ValueAndBoost valueAndBoost = parseCreateFieldForCrypt(context, fieldType().nullValueAsString(), fieldType().boost(), algo);
        if (valueAndBoost.value() == null) {
            return;
        }
        if (ignoreAbove > 0 && valueAndBoost.value().length() > ignoreAbove) {
            return;
        }
        if (fieldType().indexOptions() != IndexOptions.NONE || fieldType().stored()) {
            Field field = new Field(fieldType().names().indexName(), valueAndBoost.value(), fieldType());
            field.setBoost(valueAndBoost.boost());
            fields.add(field);
        }
        if (fieldType().hasDocValues()) {
            fields.add(new SortedSetDocValuesField(fieldType().names().indexName(), new BytesRef(valueAndBoost.value())));
        }
        if (fields.isEmpty()) {
            context.ignoredValue(fieldType().names().indexName(), valueAndBoost.value());
        }
    }

    static StringFieldMapper.ValueAndBoost parseCreateFieldForCrypt(ParseContext context, String nullValue, float defaultBoost, String algo) throws IOException {
        if (context.externalValueSet()) {
            return new StringFieldMapper.ValueAndBoost((String) context.externalValue(), defaultBoost);
        }
        XContentParser parser = context.parser();
        if (parser.currentToken() == XContentParser.Token.VALUE_NULL) {
            return new StringFieldMapper.ValueAndBoost(nullValue, defaultBoost);
        }
        if (parser.currentToken() == XContentParser.Token.START_OBJECT) {
            XContentParser.Token token;
            String currentFieldName = null;
            String value = nullValue;
            float boost = defaultBoost;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else {
                    if ("value".equals(currentFieldName) || "_value".equals(currentFieldName)) {
                        value = crypt(parser.textOrNull(), algo);
                    } else if ("boost".equals(currentFieldName) || "_boost".equals(currentFieldName)) {
                        boost = parser.floatValue();
                    } else {
                        throw new IllegalArgumentException("unknown property [" + currentFieldName + "]");
                    }
                }
            }
            return new StringFieldMapper.ValueAndBoost(value, boost);
        }
        return new StringFieldMapper.ValueAndBoost(crypt(parser.textOrNull(), algo), defaultBoost);
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, ToXContent.Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        builder.field("algo", algo);
    }

    static String crypt(String plainText, String algo) {
        if (plainText == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algo);
            digest.update(plainText.getBytes(Charset.forName("UTF-8")));
            return '{' + algo + '}' + bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }

    private static String bytesToHex(byte[] b) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            buf.append(hexDigit[(b[i] >> 4) & 0x0f]).append(hexDigit[b[i] & 0x0f]);
        }
        return buf.toString();
    }
    private final static char[] hexDigit = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

}