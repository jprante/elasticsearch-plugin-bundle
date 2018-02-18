package org.xbib.elasticsearch.action.isbnformat;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.common.standardnumber.StandardnumberService;

/**
 * Transport action for ISBN format action.
 */
public class TransportISBNFormatAction extends TransportAction<ISBNFormatRequest, ISBNFormatResponse> {

    private final StandardnumberService standardnumberService;

    @Inject
    public TransportISBNFormatAction(Settings settings, ThreadPool threadPool,
                                     ActionFilters actionFilters,
                                     IndexNameExpressionResolver indexNameExpressionResolver,
                                     TransportService transportService,
                                     StandardnumberService standardnumberService) {
        super(settings, ISBNFormatAction.NAME, threadPool, actionFilters, indexNameExpressionResolver,
                transportService.getTaskManager());
        this.standardnumberService = standardnumberService;
    }

    @Override
    protected void doExecute(ISBNFormatRequest request, ActionListener<ISBNFormatResponse> listener) {
        ISBNFormatResponse response = new ISBNFormatResponse();
        try {
            standardnumberService.handle(request.getValue(), response);
        } catch (IllegalArgumentException e) {
            logger.debug(e.getMessage(), e);
            response.setInvalid(request.getValue());
        }
        listener.onResponse(response);
    }
}
