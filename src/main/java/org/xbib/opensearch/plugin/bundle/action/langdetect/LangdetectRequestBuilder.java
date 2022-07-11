package org.xbib.opensearch.plugin.bundle.action.langdetect;

import org.opensearch.action.ActionRequestBuilder;
import org.opensearch.client.OpenSearchClient;

/**
 * Language detection request builder.
 */
public class LangdetectRequestBuilder extends ActionRequestBuilder<LangdetectRequest, LangdetectResponse> {

    public LangdetectRequestBuilder(OpenSearchClient client) {
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
