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
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.FieldMapperListener;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeContext;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.ObjectMapperListener;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.collect.Lists.newLinkedList;
import static org.elasticsearch.index.mapper.MapperBuilders.stringField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parseMultiField;
import static org.elasticsearch.index.mapper.core.TypeParsers.parsePathType;

public class ReferenceMapper extends AbstractFieldMapper<Object> {

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "reference");

    public static final String REF = "ref";

    @SuppressWarnings({"rawtypes"})
    public static class Builder extends AbstractFieldMapper.Builder<Builder, ReferenceMapper> {

        private ContentPath.Type pathType = Defaults.PATH_TYPE;

        private Mapper.Builder contentBuilder;

        private List<Mapper.Builder> refBuilders;

        private Client client;

        private String refIndex;

        private String refType;

        private String[] refFields;

        public Builder(String name, Client client) {
            super(name, new FieldType(AbstractFieldMapper.Defaults.FIELD_TYPE));
            this.contentBuilder = stringField(name);
            this.refBuilders = newLinkedList();
            this.client = client;
        }

        public Builder pathType(ContentPath.Type pathType) {
            this.pathType = pathType;
            return this;
        }

        public Builder content(Mapper.Builder content) {
            this.contentBuilder = content;
            return this;
        }

        public Builder ref(Mapper.Builder ref) {
            this.refBuilders.add(ref);
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

        public Builder refFields(Object refFields) {
            if (refFields instanceof List) {
                List l =  (List)refFields;
                this.refFields = (String[])l.toArray(new String[l.size()]);
            } else if (refFields instanceof String[]) {
                this.refFields = (String[])refFields;
            } else {
                this.refFields = new String[] { refFields.toString() };
            }
            return this;
        }

        @Override
        public ReferenceMapper build(BuilderContext context) {
            Mapper contentMapper = null;
            List<Mapper> refMappers = newLinkedList();
            if (!refBuilders.isEmpty()) {
                contentMapper = contentBuilder.build(context);
                for (Mapper.Builder refBuilder : refBuilders) {
                    RefContext refContext = new RefContext(context);
                    refMappers.add(refBuilder.build(refContext));
                }
            } else {
                contentMapper = contentBuilder.build(context);
            }
            return new ReferenceMapper(buildNames(context), pathType,
                    multiFieldsBuilder.build(this, context), copyTo,
                    contentMapper,
                    refMappers,
                    client,
                    refIndex,
                    refType,
                    refFields);
        }
    }

    /**
     * A builder context that resets the content path temporarily for the reference mapper
     */
    public static class RefContext extends BuilderContext {

        public RefContext(BuilderContext builderContext) {
            super(builderContext.indexSettings(), new ContentPath());
        }

    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public static class TypeParser implements Mapper.TypeParser {

        private final Client client;

        public TypeParser(Client client) {
            this.client = client;
        }

        private Mapper.Builder<?, ?> findMapperBuilder(Map<String, Object> propNode, String propName, ParserContext parserContext) {
            String type;
            Object typeNode = propNode.get("type");
            if (typeNode != null) {
                type = typeNode.toString();
            } else {
                type = "string";
            }
            Mapper.TypeParser typeParser = parserContext.typeParser(type);
            return typeParser.parse(propName, propNode, parserContext);
        }

        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            ReferenceMapper.Builder builder = new Builder(name, client);
            for (Map.Entry<String, Object> entry : node.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "path":
                        builder.pathType(parsePathType(name, fieldNode.toString()));
                        break;
                    case "fields":
                        Map<String, Object> fieldsNode = (Map<String, Object>) fieldNode;
                        for (Map.Entry<String, Object> entry1 : fieldsNode.entrySet()) {
                            String propName = entry1.getKey();
                            Map<String, Object> propNode = (Map<String, Object>) entry1.getValue();
                            Mapper.Builder<?, ?> mapperBuilder = findMapperBuilder(propNode, propName, parserContext);
                            parseMultiField((AbstractFieldMapper.Builder) mapperBuilder,
                                    fieldName,
                                    parserContext,
                                    propName,
                                    propNode);
                            if (propName.equals(name)) {
                                builder.content(mapperBuilder);
                            } else {
                                builder.ref(mapperBuilder);
                            }
                        }
                        break;
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

    private final ContentPath.Type pathType;

    private final Mapper contentMapper;

    private final List<Mapper> refMappers;

    private final Client client;

    private String index;

    private String type;

    private String[] fields;

    public ReferenceMapper(Names names, ContentPath.Type pathType,
                           MultiFields multiFields, CopyTo copyTo,
                           Mapper contentMapper,
                           List<Mapper> refMappers,
                           Client client,
                           String index,
                           String type,
                           String[] fields) {
        super(names, 1.0f, Defaults.FIELD_TYPE, false, null, null, null, null, null, null, null,
                ImmutableSettings.EMPTY, multiFields, copyTo);
        this.pathType = pathType;
        this.contentMapper = contentMapper;
        this.refMappers = refMappers;
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
            // allow overriding setting in the mapping by a single field
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else if (token == XContentParser.Token.VALUE_STRING) {
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
                            fields = new String[]{parser.text()};
                            break;

                    }
                } else if (token == XContentParser.Token.START_ARRAY) {
                    List<String> values = newLinkedList();
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        if (parser.text() != null) {
                            values.add(parser.text());
                        }
                    }
                    fields = values.toArray(new String[values.size()]);
                }
            }
        }
        if (content == null) {
            // do not throw exception - silently ignore
            return;
        }
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
        // client may be null at recovery time. In this case, we skip the referencing.
        if (client != null && index != null && type != null && fields != null) {
            // get document from other index. If an exception occurs, ignore silently
            try {
                GetResponse response = client.prepareGet()
                        .setIndex(index)
                        .setType(type)
                        .setId(content)
                        .setFields(fields)
                        .execute()
                        .actionGet();
                if (response != null && response.isExists()) {
                    for (String field : fields) {
                        GetField getField = response.getField(field);
                        if (getField != null) {
                            for (Object object : getField.getValues()) {
                                for (Mapper refMapper : refMappers) {
                                    context = context.createExternalValueContext(object);
                                    refMapper.parse(context);
                                }
                            }
                        }
                    }
                } else {
                    logger.debug("does not exist: {}/{}/{}", index, type, content);
                }
            } catch (Exception e) {
               logger.debug(e.getMessage());
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
        for (Mapper refMapper : refMappers) {
            refMapper.traverse(fieldMapperListener);
        }
    }

    @Override
    public void traverse(ObjectMapperListener objectMapperListener) {
    }

    @Override
    public void close() {
        contentMapper.close();
        for (Mapper refMapper : refMappers) {
            refMapper.close();
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name());
        builder.field("type", REF);
        builder.field("path", pathType.name().toLowerCase());
        if (index != null) {
            builder.field("ref_index", index);
        }
        if (type != null) {
            builder.field("ref_type", type);
        }
        if (fields != null) {
            builder.field("ref_fields", fields);
        }
        builder.startObject("fields");
        contentMapper.toXContent(builder, params);
        for (Mapper refMapper : refMappers) {
            refMapper.toXContent(builder, params);
        }
        multiFields.toXContent(builder, params);
        builder.endObject();
        multiFields.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType() {
        return REF;
    }

}