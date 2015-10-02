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
package org.xbib.elasticsearch.index.mapper.reference;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.FieldMapperListener;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeContext;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.ObjectMapperListener;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.collect.Lists.newLinkedList;
import static org.elasticsearch.index.mapper.MapperBuilders.stringField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parseField;

public class ReferenceMapper extends AbstractFieldMapper<Object> {

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "reference");

    public static final String CONTENT_TYPE = "ref";

    @SuppressWarnings({"rawtypes"})
    public static class Builder extends AbstractFieldMapper.Builder<Builder, ReferenceMapper> {

        private Mapper.Builder contentBuilder;

        private Client client;

        private String refIndex;

        private String refType;

        private List<String> refFields;

        public Builder(String name, Client client) {
            super(name, new FieldType(AbstractFieldMapper.Defaults.FIELD_TYPE));
            this.contentBuilder = stringField(name);
            this.refFields = newLinkedList();
            this.client = client;
        }

        public Builder content(Mapper.Builder content) {
            this.contentBuilder = content;
            return this;
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
                this.refFields = (List<String>)refFields;
            } else if (refFields != null) {
                this.refFields = Collections.singletonList(refFields.toString());
            }
            return this;
        }

        @Override
        public ReferenceMapper build(BuilderContext context) {
            Mapper contentMapper = contentBuilder.build(context);
            return new ReferenceMapper(buildNames(context),
                    multiFieldsBuilder.build(this, context),
                    copyTo,
                    contentMapper,
                    client,
                    refIndex,
                    refType,
                    refFields);
        }
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public static class TypeParser implements Mapper.TypeParser {

        private final Client client;

        public TypeParser(Client client) {
            this.client = client;
        }

        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            ReferenceMapper.Builder builder = new Builder(name, client);
            parseField(builder, name, node, parserContext);
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "ref_index":
                        builder.refIndex(fieldNode.toString());
                        break;
                    case "ref_type":
                        builder.refType(fieldNode.toString());
                        break;
                    case "ref_fields":
                        builder.refFields(entry.getValue());
                        break;
                }
            }
            return builder;
        }
    }

    private final Mapper contentMapper;

    private final Client client;

    private String index;

    private String type;

    private List<String> fields;

    public ReferenceMapper(Names names,
                           MultiFields multiFields,
                           CopyTo copyTo,
                           Mapper contentMapper,
                           Client client,
                           String index,
                           String type,
                           List<String> fields) {
        super(names, 1.0f, Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null,
                ImmutableSettings.EMPTY, multiFields, copyTo);
        this.contentMapper = contentMapper;
        this.client = client;
        this.index = index;
        this.type = type;
        this.fields = fields;
    }

    @Override
    public Object value(Object value) {
        return null;
    }

    @Override
    public FieldType defaultFieldType() {
        return AbstractFieldMapper.Defaults.FIELD_TYPE;
    }

    @Override
    public FieldDataType defaultFieldDataType() {
        return null;
    }

    @Override
    public void parse(ParseContext context) throws IOException {
        String content = null;
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
                            case "ref_id":
                                content = parser.text();
                                break;
                            case "ref_index":
                                index = parser.text();
                                break;
                            case "ref_type":
                                type = parser.text();
                                break;
                            case "ref_fields":
                                // single field
                                fields = newLinkedList();
                                fields.add(parser.text());
                                break;
                        }
                    }
                } else if (token == XContentParser.Token.START_ARRAY) {
                    if (currentFieldName != null) {
                        switch (currentFieldName) {
                            case "ref_fields": {
                                // list of fields
                                fields = newLinkedList();
                                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                                    if (parser.text() != null) {
                                        fields.add(parser.text());
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (content == null) {
            // do not throw exception - silently ignore
            return;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        // client may be null at recovery time. In this case, we skip the referencing silently.
        if (client != null && index != null && type != null && fields != null) {
            // get document from other index. If an exception occurs, ignore silently
            try {
                GetResponse response = client.prepareGet()
                        .setIndex(index)
                        .setType(type)
                        .setId(content)
                        .setFields(fields.toArray(new String[fields.size()]))
                        .execute()
                        .actionGet();
                if (response != null && response.isExists()) {
                    for (String field : fields) {
                        GetField getField = response.getField(field);
                        if (getField != null) {
                            for (Object object : getField.getValues()) {
                                context = context.createExternalValueContext(object);
                                if (copyTo != null) {
                                    copyTo.parse(context);
                                }
                            }
                        }
                    }
                } else {
                    logger.warn("ref doc does not exist: {}/{}/{}", index, type, content);
                }
            } catch (Exception e) {
                logger.error("error while getting ref doc {}/{}/{}: {}", index, type, content, e.getMessage());
            }
        }
    }
    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> fields) throws IOException {

    }
    @Override
    public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
    }

    @Override
    public void traverse(FieldMapperListener fieldMapperListener) {
        contentMapper.traverse(fieldMapperListener);
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
    }

    @Override
    public void close() {
        contentMapper.close();
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, false, params);
        builder.field("type", CONTENT_TYPE);
        if (index != null) {
            builder.field("ref_index", index);
        }
        if (type != null) {
            builder.field("ref_type", type);
        }
        if (fields != null) {
            builder.field("ref_fields", fields);
        }
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

}