package org.xbib.elasticsearch.rest.action.isbnformat;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestStatusToXContentListener;
import org.xbib.elasticsearch.action.isbnformat.ISBNFormatAction;
import org.xbib.elasticsearch.action.isbnformat.ISBNFormatRequest;

import java.io.IOException;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 *
 */
public class RestISBNFormatterAction extends BaseRestHandler {

    @Inject
    public RestISBNFormatterAction(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(GET, "/_isbn", this);
        controller.registerHandler(GET, "/_isbn/{value}", this);
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        final String value = request.param("value");
        final ISBNFormatRequest isbnFormatRequest = new ISBNFormatRequest().setValue(value);
        return channel -> client.execute(ISBNFormatAction.INSTANCE, isbnFormatRequest,
                    new RestStatusToXContentListener<>(channel));
    }
}
