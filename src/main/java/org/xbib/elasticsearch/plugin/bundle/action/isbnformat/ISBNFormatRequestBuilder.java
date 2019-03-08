package org.xbib.elasticsearch.plugin.bundle.action.isbnformat;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * ISBN format request builder.
 */
public class ISBNFormatRequestBuilder
        extends ActionRequestBuilder<ISBNFormatRequest, ISBNFormatResponse, ISBNFormatRequestBuilder> {

    public ISBNFormatRequestBuilder(ElasticsearchClient client) {
        super(client, ISBNFormatAction.INSTANCE, new ISBNFormatRequest());
    }

    public ISBNFormatRequestBuilder setValue(String string) {
        request.setValue(string);
        return this;
    }
}
