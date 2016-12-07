/*
 * Copyright (C) 2016 Jörg Prante
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
package org.xbib.elasticsearch.index.analysis.naturalsort;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.AttributeFactory;

import java.text.Collator;

/**
 *
 */
public class NaturalSortKeyAttributeFactory extends AttributeFactory.StaticImplementationAttributeFactory<NaturalSortKeyAttributeImpl> {

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
}
