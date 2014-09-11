package org.xbib.elasticsearch.index.analysis;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;


public class AssertingIndexSearcher extends IndexSearcher {
    final Random random;

    public AssertingIndexSearcher(Random random, IndexReader r, ExecutorService ex) {
        super(r, ex);
        this.random = new Random(random.nextLong());
    }

    public AssertingIndexSearcher(Random random, IndexReaderContext context, ExecutorService ex) {
        super(context, ex);
        this.random = new Random(random.nextLong());
    }

    /**
     * Ensures, that the returned {@code Weight} is not normalized again, which may produce wrong scores.
     */
    @Override
    public Weight createNormalizedWeight(Query query) throws IOException {
        return super.createNormalizedWeight(query);
    }

    @Override
    public Query rewrite(Query original) throws IOException {
        return super.rewrite(original);
    }

    @Override
    protected Query wrapFilter(Query query, Filter filter) {
        if (random.nextBoolean()) {
            return super.wrapFilter(query, filter);
        }
        return (filter == null) ? query : new FilteredQuery(query, filter, randomFilterStrategy(random));
    }

    @Override
    protected void search(List<AtomicReaderContext> leaves, Weight weight, Collector collector) throws IOException {
        super.search(leaves, weight, collector);
    }

    @Override
    public String toString() {
        return "AssertingIndexSearcher(" + super.toString() + ")";
    }


    public static FilteredQuery.FilterStrategy randomFilterStrategy(final Random random) {
        switch (random.nextInt(6)) {
            case 5:
            case 4:
                return new FilteredQuery.RandomAccessFilterStrategy() {
                    @Override
                    protected boolean useRandomAccess(Bits bits, int firstFilterDoc) {
                        return random.nextBoolean();
                    }
                };
            case 3:
                return FilteredQuery.RANDOM_ACCESS_FILTER_STRATEGY;
            case 2:
                return FilteredQuery.LEAP_FROG_FILTER_FIRST_STRATEGY;
            case 1:
                return FilteredQuery.LEAP_FROG_QUERY_FIRST_STRATEGY;
            case 0:
                return FilteredQuery.QUERY_FIRST_FILTER_STRATEGY;
            default:
                return FilteredQuery.RANDOM_ACCESS_FILTER_STRATEGY;
        }
    }


}
