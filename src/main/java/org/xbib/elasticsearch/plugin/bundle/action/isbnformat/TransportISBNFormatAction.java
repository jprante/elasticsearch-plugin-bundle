package org.xbib.elasticsearch.plugin.bundle.action.isbnformat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.transport.TransportService;
import org.xbib.elasticsearch.plugin.bundle.common.standardnumber.StandardnumberService;

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
        super(ISBNFormatAction.NAME, actionFilters, transportService.getLocalNodeConnection(), transportService.getTaskManager());
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
