package org.xbib.elasticsearch.action.isbnformat;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 *
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
