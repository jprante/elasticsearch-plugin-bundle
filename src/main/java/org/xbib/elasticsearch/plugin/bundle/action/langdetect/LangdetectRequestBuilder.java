package org.xbib.elasticsearch.plugin.bundle.action.langdetect;

import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.client.ElasticsearchClient;

/**
 * Language detection request builder.
 */
public class LangdetectRequestBuilder extends ActionRequestBuilder<LangdetectRequest, LangdetectResponse> {

    public LangdetectRequestBuilder(ElasticsearchClient client) {
        super(client, LangdetectAction.INSTANCE, new LangdetectRequest());
    }

    public LangdetectRequestBuilder setProfile(String string) {
        request.setProfile(string);
        return this;
    }

    public LangdetectRequestBuilder setText(String string) {
        request.setText(string);
        return this;
    }

}
