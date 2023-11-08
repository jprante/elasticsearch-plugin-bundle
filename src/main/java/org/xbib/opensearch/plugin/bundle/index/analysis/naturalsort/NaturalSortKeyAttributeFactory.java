package org.xbib.opensearch.plugin.bundle.index.analysis.naturalsort;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.AttributeFactory;

import java.text.Collator;

/**
 * Natural sort key attribute factory.
 */
public class NaturalSortKeyAttributeFactory
        extends AttributeFactory.StaticImplementationAttributeFactory<NaturalSortKeyAttributeImpl> {

    private final Collator collator;

    private final int digits;

    private final int maxTokens;

    public NaturalSortKeyAttributeFactory(Collator collator, int digits, int maxTokens) {
        this(TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, collator, digits, maxTokens);
    }

    public NaturalSortKeyAttributeFactory(AttributeFactory delegate, Collator collator, int digits, int maxTokens) {
        super(delegate, NaturalSortKeyAttributeImpl.class);
        this.collator = collator;
        this.digits = digits;
        this.maxTokens = maxTokens;
    }

    @Override
    protected NaturalSortKeyAttributeImpl createInstance() {
        return new NaturalSortKeyAttributeImpl(collator, digits, maxTokens);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NaturalSortKeyAttributeFactory &&
                collator.equals(((NaturalSortKeyAttributeFactory)object).collator) &&
                digits == ((NaturalSortKeyAttributeFactory) object).digits &&
                maxTokens == ((NaturalSortKeyAttributeFactory) object).maxTokens;
    }

    @Override
    public int hashCode() {
        return collator.hashCode() ^ Integer.hashCode(digits) ^ Integer.hashCode(maxTokens);
    }
}
