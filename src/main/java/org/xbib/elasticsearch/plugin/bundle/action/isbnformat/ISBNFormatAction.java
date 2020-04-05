package org.xbib.elasticsearch.plugin.bundle.action.isbnformat;

import org.elasticsearch.action.Action;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * ISBN format action.
 */
public class ISBNFormatAction extends Action<ISBNFormatRequest, ISBNFormatResponse, ISBNFormatRequestBuilder> {

    public static final String NAME = "generic:isbnformat";

    public static final ISBNFormatAction INSTANCE = new ISBNFormatAction();

    private ISBNFormatAction() {
        super(NAME);
    }

    @Override
    public ISBNFormatRequestBuilder newRequestBuilder(ElasticsearchClient client) {
        return new ISBNFormatRequestBuilder(client);
    }

    @Override
    public ISBNFormatResponse newResponse() {
        return new ISBNFormatResponse();
    }
}
