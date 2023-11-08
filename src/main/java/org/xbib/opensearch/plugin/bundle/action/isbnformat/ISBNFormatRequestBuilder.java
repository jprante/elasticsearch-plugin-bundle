package org.xbib.opensearch.plugin.bundle.action.isbnformat;

import org.opensearch.action.ActionRequestBuilder;
import org.opensearch.client.OpenSearchClient;

/**
 * ISBN format request builder.
 */
public class ISBNFormatRequestBuilder
        extends ActionRequestBuilder<ISBNFormatRequest, ISBNFormatResponse> {

    public ISBNFormatRequestBuilder(OpenSearchClient client) {
        super(client, ISBNFormatAction.INSTANCE, new ISBNFormatRequest());
    }

    public ISBNFormatRequestBuilder setValue(String string) {
        request.setValue(string);
        return this;
    }
}
