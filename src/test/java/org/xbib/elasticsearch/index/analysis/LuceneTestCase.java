package org.xbib.elasticsearch.index.analysis;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.util.NamedThreadFactory;
import org.junit.Assert;
import org.xbib.elasticsearch.index.analysis.icu.AssertingIndexSearcher;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LuceneTestCase extends Assert {

    public static int randomIntBetween(Random r, int min, int max) {
        assert max >= min : "max must be >= min: " + min + ", " + max;
        long range = (long) max - (long) min;
        if (range < Integer.MAX_VALUE) {
            return min + r.nextInt(1 + (int) range);
        } else {
            return min + (int) Math.round(r.nextDouble() * range);
        }
    }

    public static String randomSimpleString(Random r, int maxLength) {
        final int end = randomIntBetween(r, 0, maxLength);
        if (end == 0) {
            return "";
        }
        final char[] buffer = new char[end];
        for (int i = 0; i < end; i++) {
            buffer[i] = (char) randomIntBetween(r, 'a', 'z');
        }
        return new String(buffer, 0, end);
    }

    public static void shutdownExecutorService(ExecutorService ex) {
        if (ex != null) {
            try {
                ex.shutdown();
                ex.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Could not properly shutdown executor service.");
                e.printStackTrace(System.err);
            }
        }
    }

    public static IndexSearcher newSearcher(IndexReader r) {
        return newSearcher(r, true);
    }

    public static IndexSearcher newSearcher(IndexReader r, boolean wrapWithAssertions) {
        Random random = new Random();

        int threads = 0;
        final ThreadPoolExecutor ex;
        if (random.nextBoolean()) {
            ex = null;
        } else {
            threads = randomIntBetween(random, 1, 8);
            ex = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new NamedThreadFactory("LuceneTestCase"));
        }
        if (ex != null) {
            r.addReaderClosedListener(new IndexReader.ReaderClosedListener() {
                @Override
                public void onClose(IndexReader reader) {
                    shutdownExecutorService(ex);
                }
            });
        }
        IndexSearcher ret;
        if (wrapWithAssertions) {
            ret = random.nextBoolean()
                    ? new AssertingIndexSearcher(random, r, ex)
                    : new AssertingIndexSearcher(random, r.getContext(), ex);
        } else {
            ret = random.nextBoolean()
                    ? new IndexSearcher(r, ex)
                    : new IndexSearcher(r.getContext(), ex);
        }
        ret.setSimilarity(new DefaultSimilarity());
        return ret;

    }
}
