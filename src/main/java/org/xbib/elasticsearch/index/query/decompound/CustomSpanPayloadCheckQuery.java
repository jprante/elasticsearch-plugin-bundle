package org.xbib.elasticsearch.index.query.decompound;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.spans.FilterSpans;
import org.apache.lucene.search.spans.FilterSpans.AcceptStatus;
import org.apache.lucene.search.spans.SpanCollector;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanScorer;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Only return those matches that have a specific payload at the given position.
 *
 * Modified version of SpanPayloadCheckQuery which is able to process a null BytesRef in the payload to match list.
 */
public class CustomSpanPayloadCheckQuery extends SpanQuery {

    protected final List<BytesRef> payloadToMatch;
    protected final SpanQuery match;

    /**
     * @param match The underlying {@link org.apache.lucene.search.spans.SpanQuery} to check
     * @param payloadToMatch The {@link java.util.List} of payloads to match
     */
    public CustomSpanPayloadCheckQuery(SpanQuery match, List<BytesRef> payloadToMatch) {
        this.match = match;
        this.payloadToMatch = payloadToMatch;
    }

    @Override
    public String getField() {
        return match.getField();
    }

    @Override
    public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        SpanWeight matchWeight = match.createWeight(searcher, false, boost);
        return new SpanPayloadCheckWeight(searcher, needsScores ? getTermContexts(matchWeight) : null, matchWeight, boost);
    }

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        Query matchRewritten = match.rewrite(reader);
        if (match != matchRewritten && matchRewritten instanceof SpanQuery) {
            return new CustomSpanPayloadCheckQuery((SpanQuery)matchRewritten, payloadToMatch);
        }
        return super.rewrite(reader);
    }

    /**
     * Weight that pulls its Spans using a PayloadSpanCollector
     */
    public class SpanPayloadCheckWeight extends SpanWeight {

        final SpanWeight matchWeight;

        public SpanPayloadCheckWeight(IndexSearcher searcher, Map<Term, TermContext> termContexts,
                                      SpanWeight matchWeight, float boost) throws IOException {
            super(CustomSpanPayloadCheckQuery.this, searcher, termContexts, boost);
            this.matchWeight = matchWeight;
        }

        @Override
        public void extractTerms(Set<Term> terms) {
            matchWeight.extractTerms(terms);
        }

        @Override
        public void extractTermContexts(Map<Term, TermContext> contexts) {
            matchWeight.extractTermContexts(contexts);
        }

        @Override
        public Spans getSpans(final LeafReaderContext context, Postings requiredPostings) throws IOException {
            final PayloadChecker collector = new PayloadChecker();
            Spans matchSpans = matchWeight.getSpans(context, requiredPostings.atLeast(Postings.PAYLOADS));
            return (matchSpans == null) ? null : new FilterSpans(matchSpans) {
                @Override
                protected AcceptStatus accept(Spans candidate) throws IOException {
                    collector.reset();
                    candidate.collect(collector);
                    return collector.match();
                }
            };
        }

        @Override
        public SpanScorer scorer(LeafReaderContext context) throws IOException {
            if (field == null)
                return null;

            Terms terms = context.reader().terms(field);
            if (terms != null && !terms.hasPositions()) {
                throw new IllegalStateException("field \"" + field +
                        "\" was indexed without position data; cannot run SpanQuery (query=" + parentQuery + ")");
            }

            final Spans spans = getSpans(context, Postings.PAYLOADS);
            if (spans == null) {
                return null;
            }
            final Similarity.SimScorer docScorer = getSimScorer(context);
            return new SpanScorer(this, spans, docScorer);
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return matchWeight.isCacheable(ctx);
        }

    }

    private class PayloadChecker implements SpanCollector {

        int upto = 0;
        boolean matches = true;

        @Override
        public void collectLeaf(PostingsEnum postings, int position, Term term) throws IOException {
            if (!matches)
                return;
            if (upto >= payloadToMatch.size()) {
                matches = false;
                return;
            }
            BytesRef payload = postings.getPayload();
            if (payloadToMatch.get(upto) == null) {
                matches = payload == null;
                upto++;
                return;
            }
            if (payload == null) {
                matches = false;
                upto++;
                return;
            }
            matches = payloadToMatch.get(upto).bytesEquals(payload);
            upto++;
        }

        AcceptStatus match() {
            return matches && upto == payloadToMatch.size() ? AcceptStatus.YES : AcceptStatus.NO;
        }

        @Override
        public void reset() {
            this.upto = 0;
            this.matches = true;
        }
    }

    @Override
    public String toString(String field) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("CustomSpanPayloadCheckQuery(");
        buffer.append(match.toString(field));
        buffer.append(", payloadRef: ");
        for (BytesRef bytes : payloadToMatch) {
            // Term.toString() can not handle null
            if (bytes != null) {
                buffer.append(Term.toString(bytes));
                buffer.append(';');
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CustomSpanPayloadCheckQuery && sameClassAs(other) &&
                payloadToMatch.equals(((CustomSpanPayloadCheckQuery) other).payloadToMatch) &&
                match.equals(((CustomSpanPayloadCheckQuery) other).match);
    }

    @Override
    public int hashCode() {
        int result = classHash();
        result = 31 * result + Objects.hashCode(match);
        result = 31 * result + Objects.hashCode(payloadToMatch);
        return result;
    }
}
