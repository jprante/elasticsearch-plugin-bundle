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
import org.apache.lucene.index.IndexOptions;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.CollectionUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.MergeMappingException;
import org.elasticsearch.index.mapper.MergeResult;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static org.elasticsearch.index.mapper.MapperBuilders.stringField;

public class ReferenceMapper extends AbstractFieldMapper {

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "reference");

    public static final String CONTENT_TYPE = "ref";

    public static final class Defaults {
        public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
        public static final ReferenceFieldType FIELD_TYPE = new ReferenceFieldType();
        static {
            FIELD_TYPE.freeze();
        }
    }

    public static final class ReferenceFieldType extends MappedFieldType {

        public ReferenceFieldType() {
        }

        protected ReferenceFieldType(ReferenceMapper.ReferenceFieldType ref) {
            super(ref);
        }

        public ReferenceMapper.ReferenceFieldType clone() {
            return new ReferenceMapper.ReferenceFieldType(this);
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        public String value(Object value) {
            return value == null ? null : value.toString();
        }
    }

    @SuppressWarnings({"rawtypes"})
    public static class Builder extends AbstractFieldMapper.Builder<Builder, ReferenceMapper> {

        private List<FieldMapper.Builder> refBuilders;

        private Client client;

        private String refIndex;

        private String refType;

        private List<String> refFields;

        public Builder(String name, Client client) {
            super(name, new ReferenceFieldType());
            this.client = client;
            this.refBuilders = newLinkedList();
            this.refFields = newLinkedList();
        }

        public Builder refMapper(Mapper.Builder ref) {
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
            context.path().add(name);
            List<FieldMapper> refMappers = newLinkedList();
            if (!refBuilders.isEmpty()) {
                for (Mapper.Builder refBuilder : refBuilders) {
                    RefContext refContext = new RefContext(context);
                    refMappers.add((FieldMapper) refBuilder.build(refContext));
                }
            }
            context.path().remove();
            MappedFieldType defaultFieldType = Defaults.FIELD_TYPE.clone();
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

            return new ReferenceMapper(name,
                    this.fieldType,
                    defaultFieldType,
                    refMappers,
                    client,
                    refIndex,
                    refType,
                    refFields,
                    context.indexSettings(),
                    multiFieldsBuilder.build(this, context),
                    copyTo);
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

        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            ReferenceMapper.Builder builder = new Builder(name, client);
            Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
                    case "to":
                        List<String> toNames = (List<String>)fieldNode;
                        for (String toName : toNames) {
                            Mapper.Builder mapperBuilder = stringField(toName);
                            builder.refMapper(mapperBuilder);
                        }
                        iterator.remove();
                        break;
                    case "ref_index":
                        builder.refIndex(fieldNode.toString());
                        iterator.remove();
                        break;
                    case "ref_type":
                        builder.refType(fieldNode.toString());
                        iterator.remove();
                        break;
                    case "ref_fields":
                        builder.refFields(entry.getValue());
                        iterator.remove();
                        break;
                }
            }
            return builder;
        }

        /*private Mapper.Builder<?, ?> findMapperBuilder(Map<String, Object> propNode, String propName, ParserContext parserContext) {
            Object typeNode = propNode.get("type");
            String type = typeNode != null ? typeNode.toString() : "string";
            logger.info("parsing node of type {}: {}={}", type, propName, propNode);
            return parserContext.typeParser(type).parse(propName, propNode, parserContext);
        }*/
    }

    //private final FieldMapper contentMapper;

    private final List<FieldMapper> refMappers;

    private final Client client;

    private String index;

    private String type;

    private List<String> fields;

    public ReferenceMapper(String simpleName,
                           MappedFieldType fieldType,
                           MappedFieldType defaultFieldType,
                           List<FieldMapper> refMappers,
                           Client client,
                           String refindex,
                           String reftype,
                           List<String> fields,
                           Settings indexSettings,
                           MultiFields multiFields,
                           CopyTo copyTo) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);
        this.refMappers = refMappers;
        this.client = client;
        this.index = refindex;
        this.type = reftype;
        this.fields = fields;
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
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
                                fields = newArrayList();
                                fields.add(parser.text());
                                break;
                        }
                    }
                } else if (token == XContentParser.Token.START_ARRAY) {
                    if (currentFieldName != null) {
                        switch (currentFieldName) {
                            case "ref_fields": {
                                fields = newArrayList();
                                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                                    if (parser.text() != null) {
                                        fields.add(parser.text());
                                    }
                                }
                                break;
                            }
                            case "to": {
                                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                                    // ignore
                                }
                            }
                        }
                    }
                }
            }
        }
        if (content == null) {
            return null;
        }
        if (client != null && index != null && type != null && fields != null) {
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
                                for (FieldMapper refMapper : refMappers) {
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
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> fields) throws IOException {
    }

    @Override
    public void merge(Mapper mergeWith, MergeResult mergeResult) throws MergeMappingException {
        // ignore this for now
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Mapper> iterator() {
        List<FieldMapper> extras = newArrayList();
        extras.addAll(refMappers);
        return CollectionUtils.concat(super.iterator(), extras.iterator());
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name());
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
        builder.startArray("to");
        for (Mapper refMapper : refMappers) {
            builder.value(refMapper.name());
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

}