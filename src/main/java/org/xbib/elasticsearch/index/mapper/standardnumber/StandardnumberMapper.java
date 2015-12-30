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
package org.xbib.elasticsearch.index.mapper.standardnumber;

import org.apache.lucene.document.Field;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.MergeResult;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.xbib.elasticsearch.common.standardnumber.StandardNumber;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.stringField;

public class StandardnumberMapper extends FieldMapper {

    public static final String CONTENT_TYPE = "standardnumber";

    static final class StandardnumberFieldType extends MappedFieldType {

        protected StandardnumberFieldType() {
            super();
        }

        protected StandardnumberFieldType(StandardnumberMapper.StandardnumberFieldType ref) {
            super(ref);
        }

        public StandardnumberMapper.StandardnumberFieldType clone() {
            return new StandardnumberMapper.StandardnumberFieldType(this);
        }

        @Override
        public String typeName() {
            return "standardnumber";
        }

        public String value(Object value) {
            return value == null ? null : value.toString();
        }
    }

    public static class Builder extends FieldMapper.Builder<Builder, StandardnumberMapper> {

        private StringFieldMapper.Builder contentBuilder;

        private StringFieldMapper.Builder stdnumBuilder = stringField("standardnumber");

        private StandardnumberService service;

        public Builder(String name, StandardnumberService service) {
            super(name, new StandardnumberFieldType());
            this.service = service;
            this.contentBuilder = stringField(name);
            this.builder = this;
        }

        public Builder content(StringFieldMapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder stdnum(StringFieldMapper.Builder stdnum) {
            this.stdnumBuilder = stdnum;
            return this;
        }

        @Override
        public StandardnumberMapper build(BuilderContext context) {
            context.path().add(name);
            StringFieldMapper contentMapper = contentBuilder.build(context);
            StringFieldMapper stdnumMapper = stdnumBuilder.build(context);
            context.path().remove();
            return new StandardnumberMapper(name,
                    fieldType,
                    defaultFieldType,
                    context.indexSettings(),
                    multiFieldsBuilder.build(this, context),
                    copyTo,
                    service,
                    contentMapper,
                    stdnumMapper);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private StandardnumberService service;

        public void setService(StandardnumberService service) {
            this.service = service;
        }

        @SuppressWarnings({"unchecked","rawtypes"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> mapping, ParserContext parserContext)
                throws MapperParsingException {
            StandardnumberMapper.Builder builder = new Builder(name, service);
            Iterator<Map.Entry<String, Object>> iterator = mapping.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                if (fieldName.equals("fields")) {
                    Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                    for (Map.Entry<String, Object> fieldsEntry : fieldsNode.entrySet()) {
                        String propName = fieldsEntry.getKey();
                        Object propNode = fieldsEntry.getValue();
                        if (name.equals(propName)) {
                            builder.content((StringFieldMapper.Builder) parserContext.typeParser("string").parse(name,
                                    (Map<String, Object>) propNode, parserContext));
                        } else if ("standardnumber".equals(propName)) {
                            builder.stdnum((StringFieldMapper.Builder) parserContext.typeParser("string").parse(propName,
                                    (Map<String, Object>) propNode, parserContext));
                        }
                    }
                    iterator.remove();
                }
            }

            return builder;
        }
    }

    private final StandardnumberService service;
    private final FieldMapper contentMapper;
    private final FieldMapper stdnumMapper;

    public StandardnumberMapper(String simpleName,
                                MappedFieldType fieldType,
                                MappedFieldType defaultFieldType,
                                Settings indexSettings,
                                MultiFields multiFields,
                                CopyTo copyTo,
                                StandardnumberService service,
                                FieldMapper contentMapper,
                                FieldMapper stdnumMapper) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);
        this.service = service;
        this.contentMapper = contentMapper;
        this.stdnumMapper = stdnumMapper;
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
        String content = null;
        XContentParser parser = context.parser();
        XContentParser.Token token = parser.currentToken();
        if (token == XContentParser.Token.VALUE_STRING) {
            content = parser.text();
        }
        if (content == null) {
            return null;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        try {
            Collection<StandardNumber> stdnums = service.detect(content);
            for (StandardNumber stdnum : stdnums) {
                context = context.createExternalValueContext(stdnum.normalizedValue());
                stdnumMapper.parse(context);
            }
        } catch(NumberFormatException e) {
            context = context.createExternalValueContext("unknown");
            stdnumMapper.parse(context);
        }
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> fields) throws IOException {
    }

    @Override
    public void merge(Mapper mergeWith, MergeResult mergeResult) throws MergeMappingException {
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(simpleName());
        builder.field("type", CONTENT_TYPE);
        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        stdnumMapper.toXContent(builder, params);
        builder.endObject();
        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }
}