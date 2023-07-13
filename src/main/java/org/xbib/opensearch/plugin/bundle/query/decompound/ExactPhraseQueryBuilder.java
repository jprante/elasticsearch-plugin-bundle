package org.xbib.opensearch.plugin.bundle.query.decompound;

import org.apache.lucene.search.Query;
import org.opensearch.common.ParsingException;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.core.ParseField;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.index.query.AbstractQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryRewriteContext;
import org.opensearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Objects;

public class ExactPhraseQueryBuilder extends AbstractQueryBuilder<ExactPhraseQueryBuilder> {

    public static final String NAME = "exact_phrase";

    private static final ParseField QUERY_FIELD = new ParseField("query");

    private static final QueryTransformer QUERY_TRANSFORMER = new QueryTransformer();

    private final QueryBuilder query;

    public ExactPhraseQueryBuilder(QueryBuilder query) {
        this.query = query;
    }

    public ExactPhraseQueryBuilder(StreamInput in) throws IOException {
        super(in);
        query = in.readNamedWriteable(QueryBuilder.class);
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteable(query);
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.field(QUERY_FIELD.getPreferredName());
        query.toXContent(builder, params);
        printBoostAndQueryName(builder);
        builder.endObject();
    }

    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        return QUERY_TRANSFORMER.transform(this.query.toQuery(context));
    }

    @Override
    protected QueryBuilder doRewrite(QueryRewriteContext queryRewriteContext) throws IOException {
        QueryBuilder rewrittenQuery = query.rewrite(queryRewriteContext);
        if (rewrittenQuery != query) {
            return new ExactPhraseQueryBuilder(rewrittenQuery);
        }
        return this;
    }

    public static ExactPhraseQueryBuilder fromXContent(XContentParser parser) throws IOException {
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String queryName = null;
        QueryBuilder query = null;
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (currentFieldName != null) {
                    if (QUERY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                        query = parseInnerQueryBuilder(parser);
                    } else {
                        throw new ParsingException(parser.getTokenLocation(), "[nested] query does not support [" + currentFieldName + "]");
                    }
                }
            } else if (token.isValue()) {
                if (currentFieldName != null) {
                    if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                        boost = parser.floatValue();
                    } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                        queryName = parser.text();
                    } else {
                        throw new ParsingException(parser.getTokenLocation(), "[nested] query does not support [" + currentFieldName + "]");
                    }
                }
            }
        }
        return new ExactPhraseQueryBuilder(query).queryName(queryName).boost(boost);
    }

    @Override
    protected boolean doEquals(ExactPhraseQueryBuilder that) {
        return Objects.equals(query, that.query);
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(query);
    }

    public QueryBuilder query() {
        return query;
    }
}
