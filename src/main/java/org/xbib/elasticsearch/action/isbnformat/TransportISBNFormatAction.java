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
package org.xbib.elasticsearch.action.isbnformat;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.xbib.elasticsearch.common.standardnumber.ISBN;

public class TransportISBNFormatAction extends TransportAction<ISBNFormatRequest, ISBNFormatResponse> {

    @Inject
    public TransportISBNFormatAction(Settings settings, ThreadPool threadPool,
                                     ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver) {
        super(settings, ISBNFormatAction.NAME, threadPool, actionFilters, indexNameExpressionResolver);
    }

    @Override
    protected void doExecute(ISBNFormatRequest request, ActionListener<ISBNFormatResponse> listener) {
        ISBNFormatResponse response = new ISBNFormatResponse();
        try {
            ISBN isbn = new ISBN().set(request.getValue()).normalize().verify();
            response.setIsbn10(isbn.ean(false).normalizedValue());
            response.setIsbn10Formatted(isbn.ean(false).format());
            response.setIsbn13(isbn.ean(true).normalizedValue());
            response.setIsbn13Formatted(isbn.ean(true).format());
        } catch (IllegalArgumentException e) {
            response.setInvalid(request.getValue());
        }
        listener.onResponse(response);
    }
}
