package org.xbib.elasticsearch.index.analysis;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

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
    public Query rewrite(Query original) throws IOException {
        return super.rewrite(original);
    }

    @Override
    public String toString() {
        return "AssertingIndexSearcher(" + super.toString() + ")";
    }

}
