/*
 * Copyright (C) 2014 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.elasticsearch.index.mapper.langdetect;

import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexOptions;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.MergeResult;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;
import org.xbib.elasticsearch.index.analysis.langdetect.Language;
import org.xbib.elasticsearch.index.analysis.langdetect.LanguageDetectionException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.stringField;

public class LangdetectMapper extends AbstractFieldMapper {

    public static final String CONTENT_TYPE = "langdetect";

    static final class LangdetectFieldType extends MappedFieldType {

        protected LangdetectFieldType() {
            super();
        }

        protected LangdetectFieldType(LangdetectMapper.LangdetectFieldType ref) {
            super(ref);
        }

        public LangdetectMapper.LangdetectFieldType clone() {
            return new LangdetectMapper.LangdetectFieldType(this);
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        public String value(Object value) {
            return value == null ? null : value.toString();
        }
    }

    public static class Builder extends AbstractFieldMapper.Builder<Builder, LangdetectMapper> {

        private StringFieldMapper.Builder contentBuilder;
        private StringFieldMapper.Builder langBuilder;
        private Settings.Builder settingsBuilder;

        public Builder(String name) {
            super(name, new LangdetectFieldType());
            this.builder = this;
            this.contentBuilder = stringField(name);
            this.langBuilder =  stringField("lang");
            this.settingsBuilder = Settings.settingsBuilder();
        }

        public Builder content(StringFieldMapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder lang(StringFieldMapper.Builder lang) {
            this.langBuilder = lang;
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

        public Builder map(Map<String,String> map) {
            for (String key : map.keySet()) {
                settingsBuilder.put("map." + key, map.get(key));
            }
            return this;
        }

        public Builder languages(List<String> languages) {
            settingsBuilder.putArray("languages", languages.toArray(new String[languages.size()]));
            return this;
        }

        @Override
        public LangdetectMapper build(BuilderContext context) {
            ContentPath.Type origPathType = context.path().pathType();
            context.path().add(name);
            StringFieldMapper contentMapper = contentBuilder.build(context);
            StringFieldMapper langMapper = langBuilder.build(context);
            context.path().remove();
            context.path().pathType(origPathType);

            MappedFieldType defaultFieldType = new LangdetectFieldType();
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

            LangdetectService detector = new LangdetectService(settingsBuilder.build());
            detector.start();
            return new LangdetectMapper(name,
                    this.fieldType,
                    defaultFieldType,
                    contentMapper,
                    langMapper,
                    detector,
                    context.indexSettings(),
                    multiFieldsBuilder.build(this, context),
                    copyTo);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static class TypeParser implements Mapper.TypeParser {

        @Override
        public Mapper.Builder parse(String name, Map<String, Object> mapping, ParserContext parserContext)
                throws MapperParsingException {
            LangdetectMapper.Builder builder = new Builder(name);
            Iterator<Map.Entry<String, Object>> iterator = mapping.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "fields": {
                        Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                        for (Map.Entry<String, Object> fieldsEntry : fieldsNode.entrySet()) {
                            String propName = fieldsEntry.getKey();
                            Object propNode = fieldsEntry.getValue();
                            if (name.equals(propName)) {
                                builder.content((StringFieldMapper.Builder) parserContext.typeParser("string").parse(name,
                                        (Map<String, Object>) propNode, parserContext));
                            } else if ("lang".equals(propName)) {
                                builder.lang((StringFieldMapper.Builder) parserContext.typeParser("string").parse("lang",
                                        (Map<String, Object>) propNode, parserContext));
                            }
                        }
                        iterator.remove();
                        break;
                    }
                    case "number_of_trials": {
                        builder.ntrials((Integer)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "alpha": {
                        builder.alpha((Double)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "alpha_width": {
                        builder.alphaWidth((Double)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "iteration_limit": {
                        builder.iterationLimit((Integer)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "prob_threshold": {
                        builder.probThreshold((Double)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "conv_threshold": {
                        builder.convThreshold((Double)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "base_freq": {
                        builder.baseFreq((Integer)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "pattern": {
                        builder.pattern((String)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "max": {
                        builder.max((Integer)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "binary": {
                        builder.binary((Boolean)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "map" : {
                        builder.map((Map<String,String>)fieldNode);
                        iterator.remove();
                        break;
                    }
                    case "languages" : {
                        builder.languages((List<String>)fieldNode);
                        iterator.remove();
                        break;
                    }
                }
            }
            return builder;
        }
    }

    private final StringFieldMapper contentMapper;

    private final StringFieldMapper langMapper;

    private final LangdetectService detector;

    public LangdetectMapper(String simpleName,
                            MappedFieldType fieldType,
                            MappedFieldType defaultFieldType,
                            StringFieldMapper contentMapper,
                            StringFieldMapper langMapper,
                            LangdetectService detector,
                            Settings indexSettings,
                            MultiFields multiFields,
                            CopyTo copyTo) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);
        this.contentMapper = contentMapper;
        this.langMapper = langMapper;
        this.detector = detector;
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
        String content = null;
        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
            if (detector.getSettings().getAsBoolean("binary", false)) {
                try {
                    byte[] b = parser.binaryValue();
                    if (b != null && b.length > 0) {
                        content = new String(b, Charset.forName("UTF-8"));
                    }
                } catch (Exception e) {
                }
            }
        }
        if (content == null) {
            return null;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        try {
            List<Language> langs = detector.detectAll(content);
            for (Language lang : langs) {
                context = context.createExternalValueContext(lang.getLanguage());
                langMapper.parse(context);
            }
        } catch (LanguageDetectionException e) {
            context = context.createExternalValueContext("unknown");
            langMapper.parse(context);
        }
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {
    }

    @Override
    public void merge(Mapper mergeWith, MergeResult mergeResult) throws MergeMappingException {
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name());
        builder.field("type", CONTENT_TYPE);
        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        langMapper.toXContent(builder, params);
        builder.endObject();
        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType() {
       return CONTENT_TYPE;
    }

}