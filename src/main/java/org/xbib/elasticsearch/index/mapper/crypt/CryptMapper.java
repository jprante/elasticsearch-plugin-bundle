package org.xbib.elasticsearch.index.mapper.crypt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.StringFieldMapper;
import org.elasticsearch.index.mapper.TextFieldMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.TypeParsers.parseField;
import static org.elasticsearch.index.mapper.TypeParsers.parseMultiField;

/**
 *
 */
public class CryptMapper extends TextFieldMapper {

    private static final Logger logger = LogManager.getLogger(CryptMapper.class.getName());

    public static final String MAPPER_TYPE = "crypt";

    private static final  char[] hexDigit = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private String algo;

    public CryptMapper(String simpleName, TextFieldType fieldType, MappedFieldType defaultFieldType,
                       int positionIncrementGap, Boolean includeInAll,
                       Settings indexSettings, MultiFields multiFields, CopyTo copyTo, String algo) {
        super(simpleName, fieldType, defaultFieldType, positionIncrementGap, includeInAll,
                indexSettings, multiFields, copyTo);
        this.algo = algo;
    }

    static StringFieldMapper.ValueAndBoost parseCreateFieldForCrypt(ParseContext context, String nullValue,
                                                                    float defaultBoost, String algo) throws IOException {
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

    static String crypt(String plainText, String algo) {
        if (plainText == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algo);
            digest.update(plainText.getBytes(Charset.forName("UTF-8")));
            return '{' + algo + '}' + bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
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

    @Override
    protected String contentType() {
        return MAPPER_TYPE;
    }

    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
        StringFieldMapper.ValueAndBoost valueAndBoost =
                parseCreateFieldForCrypt(context, fieldType().nullValueAsString(), fieldType().boost(), algo);
        if (valueAndBoost.value() == null) {
            return;
        }
        if (fieldType().indexOptions() != IndexOptions.NONE || fieldType().stored()) {
            Field field = new Field(fieldType().name(), valueAndBoost.value(), fieldType());
            fields.add(field);
        }
        if (fieldType().hasDocValues()) {
            fields.add(new SortedSetDocValuesField(fieldType().name(), new BytesRef(valueAndBoost.value())));
        }
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, ToXContent.Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        builder.field("algo", algo);
    }

    public static class Builder extends TextFieldMapper.Builder {

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
            if (fieldType.indexOptions() != IndexOptions.NONE && !fieldType.tokenized()) {
                defaultFieldType.setOmitNorms(true);
                defaultFieldType.setIndexOptions(IndexOptions.DOCS);
                if (!omitNormsSet && Float.compare(fieldType.boost(), 1.0f) == 0) {
                    fieldType.setOmitNorms(true);
                }
                if (!indexOptionsSet) {
                    fieldType.setIndexOptions(IndexOptions.DOCS);
                }
            }
            setupFieldType(context);
            return new CryptMapper(name, fieldType(), defaultFieldType, 100,
                    includeInAll, context.indexSettings(),
                    multiFieldsBuilder.build(this, context), copyTo, algo);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, Mapper.TypeParser.ParserContext parserContext) {
            Builder builder = new Builder(name);
            parseField(builder, name, node, parserContext);
            Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String propName = entry.getKey();
                Object propNode = entry.getValue();
                switch (propName) {
                    case "algo" :
                        builder.algo(propNode.toString());
                        iterator.remove();
                        break;
                    case "position_increment_gap" :
                        iterator.remove();
                        break;
                    default:
                        parseMultiField(builder, name, parserContext, propName, propNode);
                        break;
                }
            }
            return builder;
        }
    }
}
