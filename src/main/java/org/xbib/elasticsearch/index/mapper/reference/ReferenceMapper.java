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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.stringField;

public class ReferenceMapper extends FieldMapper {

    private final static ESLogger logger = ESLoggerFactory.getLogger("", "reference");

    public static final String CONTENT_TYPE = "ref";

    public static final class Defaults {
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
    public static class Builder extends FieldMapper.Builder<Builder, ReferenceMapper> {

        private FieldMapper.Builder contentBuilder;

        private List<FieldMapper.Builder> refBuilders;

        private Client client;

        private String refIndex;

        private String refType;

        private List<String> refFields;

        public Builder(String name, Client client) {
            super(name, new ReferenceFieldType());
            this.client = client;
            this.refBuilders = new LinkedList<>();
            this.refFields = new LinkedList<>();
            this.contentBuilder = stringField(name);
        }

        public Builder refIndex(String refIndex) {
            this.refIndex = refIndex;
            return this;
        }

        public Builder refType(String refType) {
            this.refType = refType;
            return this;
        }

        public Builder refMapper(FieldMapper.Builder ref) {
            this.refBuilders.add(ref);
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
            FieldMapper contentMapper = (FieldMapper) contentBuilder.build(context);
            List<FieldMapper> refMappers = new LinkedList<>();
            if (!refBuilders.isEmpty()) {
                for (Mapper.Builder refBuilder : refBuilders) {
                    RefContext refContext = new RefContext(context.indexSettings());
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
                    client,
                    refIndex,
                    refType,
                    refFields,
                    contentMapper,
                    refMappers,
                    context.indexSettings(),
                    multiFieldsBuilder.build(this, context),
                    copyTo);
        }
    }

    /**
     * A builder context that resets the content path temporarily for a reference mapper
     */
    public static class RefContext extends BuilderContext {

        public RefContext(Settings settings) {
            super(settings, new ContentPath());
        }
    }

    public static class TypeParser implements Mapper.TypeParser {

        private final Client client;

        public TypeParser(Client client) {
            this.client = client;
        }

        @Override
        @SuppressWarnings({"unchecked","rawtypes"})
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            ReferenceMapper.Builder builder = new Builder(name, client);
            Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                String fieldName = entry.getKey();
                Object fieldNode = entry.getValue();
                switch (fieldName) {
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
                    case "to":
                        List<String> toNames = (List<String>)fieldNode;
                        for (String toName : toNames) {
                            FieldMapper.Builder mapperBuilder = stringField(toName);
                            builder.refMapper(mapperBuilder);
                        }
                        iterator.remove();
                        break;
                }
            }
            return builder;
        }
    }

    private final Settings settings;

    private final Client client;

    private String index;

    private String type;

    private List<String> fields;

    private FieldMapper contentMapper;

    private List<FieldMapper> mappers;

    public ReferenceMapper(String simpleName,
                           MappedFieldType fieldType,
                           MappedFieldType defaultFieldType,
                           Client client,
                           String refindex,
                           String reftype,
                           List<String> reffields,
                           FieldMapper contentMapper,
                           List<FieldMapper> refmappers,
                           Settings indexSettings,
                           MultiFields multiFields,
                           CopyTo copyTo) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);
        this.settings = indexSettings;
        this.client = client;
        this.index = refindex;
        this.type = reftype;
        this.fields = reffields;
        this.contentMapper = contentMapper;
        this.mappers = refmappers;
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
                                fields = new LinkedList<>();
                                fields.add(parser.text());
                                break;
                        }
                    }
                } else if (token == XContentParser.Token.START_ARRAY) {
                    if (currentFieldName != null) {
                        switch (currentFieldName) {
                            case "ref_fields": {
                                fields = new LinkedList<>();
                                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                                    if (parser.text() != null) {
                                        fields.add(parser.text());
                                    }
                                }
                                break;
                            }
                            case "to": {
                                mappers = new LinkedList<>();
                                while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                                    if (parser.text() != null) {
                                        RefContext refContext = new RefContext(settings);
                                        mappers.add(stringField(parser.text()).build(refContext));
                                    }
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
        context = context.createExternalValueContext(content);
        contentMapper.parse(context);
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
                                for (FieldMapper mapper : mappers) {
                                    context = context.createExternalValueContext(object);
                                    mapper.parse(context);
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
        return null;
    }

    @Override
    protected void parseCreateField(ParseContext parseContext, List<Field> fields) throws IOException {
        // override
    }

    @Override
    public void merge(Mapper mergeWith, MergeResult mergeResult) throws MergeMappingException {
        // ignore this for now
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Mapper> iterator() {
        List<FieldMapper> extras = new LinkedList<>();
        extras.addAll(mappers);
        return CollectionUtils.concat(super.iterator(), extras.iterator());
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(simpleName());
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
        for (Mapper mapper : mappers) {
            builder.value(mapper.name());
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