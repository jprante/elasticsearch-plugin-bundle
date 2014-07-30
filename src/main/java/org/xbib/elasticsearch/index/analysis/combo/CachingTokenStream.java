package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Attribute;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class can be used if the token attributes of a TokenStream
 * are intended to be consumed more than once. It caches
 * all token attribute states locally in a List.
 * <p/>
 * <P>CachingTokenStream implements the optional method
 * {@link org.apache.lucene.analysis.TokenStream#reset()}, which repositions the
 * stream to the first Token.
 * <p/>
 * <P>It differs from Lucene's {@link org.apache.lucene.analysis.CachingTokenFilter} in that it
 * creates a new AttributeSource, unrelated to the provided input.
 * This permits free reuse of the TokenStream, and after
 * {@link #fillCache()} has been called, the underlying input
 * TokenStream is available for garbage collection.
 * {@link org.apache.lucene.analysis.CachingTokenFilter} has no such feature because it keeps
 * the same {@link org.apache.lucene.util.AttributeSource} and if the source analyzer
 * reuses the {@link org.apache.lucene.analysis.TokenStream} in a call of
 * {@link org.apache.lucene.analysis.Analyzer#tokenStream(String, java.io.Reader)}, then a
 * call on {@link org.apache.lucene.analysis.TokenStream#incrementToken()}
 * will affect the current attribute state of the "cached" instance.
 */
public final class CachingTokenStream extends TokenStream {
    protected List<State> cache = null;
    protected Iterator<State> iterator = null;
    protected State finalState;
    protected TokenStream input;

    /**
     * Create a new CachingTokenStream around <code>input</code>,
     * caching its token attributes, which can be replayed again
     * after a call to {@link #reset()}.
     */
    public CachingTokenStream(TokenStream input) {
        this.input = input;
        Iterator<Class<? extends Attribute>> attrIter = this.input.getAttributeClassesIterator();
        while (attrIter.hasNext()) {
            addAttribute(attrIter.next());
        }
    }

    public void fillCache() throws IOException {
        if (cache != null) {
            return;
        }
        cache = new LinkedList<State>();

        while (input.incrementToken()) {
            cache.add(input.captureState());
        }
        // capture final state
        input.end();
        finalState = input.captureState();
        input = null; // don't hold on the input

        iterator = cache.iterator();
    }

    @Override
    public final boolean incrementToken() throws IOException {
        // fill cache lazily
        fillCache();

        if (!iterator.hasNext()) {
            // the cache is exhausted, return false
            return false;
        }
        // Since the TokenFilter can be reset, the tokens need to be preserved as immutable.
        restoreState(iterator.next());
        return true;
    }

    @Override
    public final void end() {
        if (finalState != null) {
            restoreState(finalState);
        }
    }

    /**
     * Rewinds the iterator to the beginning of the cached list.
     * <p/>
     * Note that this does not call reset() on the wrapped tokenstream ever, even
     * the first time. You should reset() the inner tokenstream before wrapping
     * it with CachingTokenStream.
     */
    @Override
    public void reset() {
        if (cache != null) {
            iterator = cache.iterator();
        }
    }


}
