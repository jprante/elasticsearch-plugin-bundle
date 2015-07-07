package org.xbib.elasticsearch.index.analysis;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Weight;

import java.io.IOException;
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

    @Override
    public Weight createNormalizedWeight(Query query, boolean b) throws IOException {
        return super.createNormalizedWeight(query, b);
    }

    @Override
    public Query rewrite(Query original) throws IOException {
        return super.rewrite(original);
    }

    /*@Override
    protected Query wrapFilter(Query query, Filter filter) {
        if (random.nextBoolean()) {
            return super.wrapFilter(query, filter);
        }
        return (filter == null) ? query : new FilteredQuery(query, filter, randomFilterStrategy(random));
    }*/

    @Override
    public String toString() {
        return "AssertingIndexSearcher(" + super.toString() + ")";
    }


    /*public static FilteredQuery.FilterStrategy randomFilterStrategy(final Random random) {
        switch (random.nextInt(6)) {
            case 5:
            case 4:
                return new FilteredQuery.RandomAccessFilterStrategy() {
                    protected boolean useRandomAccess(Bits bits, long filterCost) {
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
    }*/


}
