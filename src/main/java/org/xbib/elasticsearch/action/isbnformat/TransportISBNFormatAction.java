package org.xbib.elasticsearch.action.isbnformat;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.common.standardnumber.ISBN;

/**
 *
 */
public class TransportISBNFormatAction extends TransportAction<ISBNFormatRequest, ISBNFormatResponse> {

    @Inject
    public TransportISBNFormatAction(Settings settings, ThreadPool threadPool,
                                     ActionFilters actionFilters,
                                     IndexNameExpressionResolver indexNameExpressionResolver,
                                     TransportService transportService) {
        super(settings, ISBNFormatAction.NAME, threadPool, actionFilters, indexNameExpressionResolver, transportService.getTaskManager());
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
