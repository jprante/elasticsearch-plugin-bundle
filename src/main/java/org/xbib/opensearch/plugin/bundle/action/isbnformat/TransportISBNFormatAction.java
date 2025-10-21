package org.xbib.opensearch.plugin.bundle.action.isbnformat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensearch.action.support.ActionFilters;
import org.opensearch.action.support.TransportAction;
import org.opensearch.common.inject.Inject;
import org.opensearch.core.action.ActionListener;
import org.opensearch.tasks.Task;
import org.opensearch.transport.TransportService;
import org.xbib.opensearch.plugin.bundle.common.standardnumber.StandardnumberService;

/**
 * Transport action for ISBN format action.
 */
public class TransportISBNFormatAction extends TransportAction<ISBNFormatRequest, ISBNFormatResponse> {

    private static final Logger logger = LogManager.getLogger(TransportISBNFormatAction.class);

    private final StandardnumberService standardnumberService;

    @Inject
    public TransportISBNFormatAction(ActionFilters actionFilters,
                                     TransportService transportService,
                                     StandardnumberService standardnumberService) {
        super(ISBNFormatAction.NAME, actionFilters, transportService.getTaskManager());
        this.standardnumberService = standardnumberService;
    }

    @Override
    protected void doExecute(Task task, ISBNFormatRequest request, ActionListener<ISBNFormatResponse> listener) {
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
