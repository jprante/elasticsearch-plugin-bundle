package org.xbib.elasticsearch.index.query.decompound;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueryTransformer {

    private final List<QueryHandler> queryHandlers = Arrays.asList(new BooleanQueryHandler(),
            new BoostQueryHandler(), new DisjunctionMaxQueryHandler(), new ConstantScoreQueryHandler(),
            new PhraseQueryHandler());

    public Query transform(Query query) {
        for (QueryHandler queryHandler : queryHandlers) {
            if (queryHandler.accept(query)) {
                return queryHandler.handle(query, this);
            }
        }
        return query;
    }

    interface QueryHandler {

        boolean accept(Query query);

        Query handle(Query query, QueryTransformer queryTransformer);
    }

    class BooleanQueryHandler implements QueryHandler {

        @Override
        public boolean accept(Query query) {
            return query instanceof BooleanQuery;
        }

        @Override
        public Query handle(Query query, QueryTransformer queryTransformer) {
            BooleanQuery booleanQuery = (BooleanQuery) query;
            boolean changed = false;
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            for (BooleanClause clause: booleanQuery.clauses()) {
                Query newClauseQuery = queryTransformer.transform(clause.getQuery());
                if (newClauseQuery != clause.getQuery()) {
                    changed = true;
                    builder.add(new BooleanClause(newClauseQuery, clause.getOccur()));
                } else {
                    builder.add(clause);
                }
            }
            if (changed) {
                builder.setMinimumNumberShouldMatch(booleanQuery.getMinimumNumberShouldMatch());
                return builder.build();
            }
            return query;
        }
    }

    class BoostQueryHandler implements QueryHandler {

        @Override
        public boolean accept(Query query) {
            return query instanceof BoostQuery;
        }

        @Override
        public Query handle(Query query, QueryTransformer queryTransformer) {
            BoostQuery boostQuery = (BoostQuery) query;
            Query newInnerBoostQuery = queryTransformer.transform(boostQuery.getQuery());
            if (newInnerBoostQuery != boostQuery.getQuery()) {
                return new BoostQuery(newInnerBoostQuery, boostQuery.getBoost());
            }
            return query;
        }
    }

    class DisjunctionMaxQueryHandler implements QueryHandler {

        @Override
        public boolean accept(Query query) {
            return query instanceof DisjunctionMaxQuery;
        }

        @Override
        public Query handle(Query query, QueryTransformer queryTransformer) {
            DisjunctionMaxQuery disjunctionMaxQuery = (DisjunctionMaxQuery) query;
            boolean changed = false;
            List<Query> innerQueries = new ArrayList<>();
            for (Query innerQuery: disjunctionMaxQuery.getDisjuncts()) {
                Query newInnerQuery = queryTransformer.transform(innerQuery);
                if (newInnerQuery != innerQuery) {
                    changed = true;
                    innerQueries.add(newInnerQuery);
                } else {
                    innerQueries.add(innerQuery);
                }
            }
            if (changed) {
                return new DisjunctionMaxQuery(innerQueries, disjunctionMaxQuery.getTieBreakerMultiplier());
            }
            return query;
        }
    }

    class ConstantScoreQueryHandler implements QueryHandler {

        @Override
        public boolean accept(Query query) {
            return query instanceof ConstantScoreQuery;
        }

        @Override
        public Query handle(Query query, QueryTransformer queryTransformer) {
            ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) query;
            Query newInnerConstantScoreQuery = queryTransformer.transform(constantScoreQuery.getQuery());
            if (newInnerConstantScoreQuery != constantScoreQuery.getQuery()) {
                return new ConstantScoreQuery(newInnerConstantScoreQuery);
            }
            return query;
        }
    }

    class PhraseQueryHandler implements QueryHandler {

        @Override
        public boolean accept(Query query) {
            return query instanceof PhraseQuery;
        }

        @Override
        public Query handle(Query query, QueryTransformer queryTransformer) {
            PhraseQuery phraseQuery = (PhraseQuery) query;
            SpanNearQuery.Builder builder = new SpanNearQuery.Builder(phraseQuery.getTerms()[0].field(), true);
            int i = 0;
            int position = -1;
            for (Term term : phraseQuery.getTerms()) {
                if (i > 0) {
                    int gap = (phraseQuery.getPositions()[i] - position) - 1;
                    if (gap > 0) {
                        builder.addGap(gap);
                    }
                }
                position = phraseQuery.getPositions()[i];
                builder.addClause(new CustomSpanPayloadCheckQuery(new SpanTermQuery(term), Collections.singletonList(null)));
                i++;
            }
            return builder.setSlop(phraseQuery.getSlop()).build();
        }
    }
}
