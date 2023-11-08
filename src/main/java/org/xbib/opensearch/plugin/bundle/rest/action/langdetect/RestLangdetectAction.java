package org.xbib.opensearch.plugin.bundle.rest.action.langdetect;

import org.opensearch.client.node.NodeClient;
import org.opensearch.common.CheckedConsumer;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.common.bytes.BytesReference;
import org.opensearch.core.xcontent.DeprecationHandler;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.rest.BaseRestHandler;
import org.opensearch.rest.RestRequest;
import org.opensearch.rest.action.RestStatusToXContentListener;
import org.xbib.opensearch.plugin.bundle.action.langdetect.LangdetectAction;
import org.xbib.opensearch.plugin.bundle.action.langdetect.LangdetectRequest;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.opensearch.rest.RestRequest.Method.GET;
import static org.opensearch.rest.RestRequest.Method.POST;

/**
 * REST language detection action.
 */
public class RestLangdetectAction extends BaseRestHandler {

    @Override
    public List<Route> routes() {
        return unmodifiableList(asList(new Route(GET, "/_langdetect/health"),
                                       new Route(GET, "/_langdetect/{profile}"),
                                       new Route(POST, "/_langdetect"),
                                       new Route(POST, "/_langdetect/{profile}")));
    }

    @Override
    public String getName() {
        return "langdetect";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        final LangdetectRequest langdetectRequest = new LangdetectRequest();
        langdetectRequest.setText(request.param("text"));
        langdetectRequest.setProfile(request.param("profile", ""));
        withContent(request, parser -> {
            if (parser != null) {
                XContentParser.Token token;
                while ((token = parser.nextToken()) != null) {
                    if (token == XContentParser.Token.VALUE_STRING) {
                        if ("text".equals(parser.currentName())) {
                            langdetectRequest.setText(parser.text());
                        } else if ("profile".equals(parser.currentName())) {
                            langdetectRequest.setProfile(parser.text());
                        }
                    }
                }
            }
        });
        return channel -> client.execute(LangdetectAction.INSTANCE, langdetectRequest,
                new RestStatusToXContentListener<>(channel));
    }

    private void withContent(RestRequest restRequest, CheckedConsumer<XContentParser, IOException> withParser)
            throws IOException {
        BytesReference content = restRequest.content();
        XContentType xContentType = XContentType.JSON;
        if (content.length() > 0) {
            try (XContentParser parser = xContentType.xContent().createParser(restRequest.getXContentRegistry(),
                    DeprecationHandler.THROW_UNSUPPORTED_OPERATION, content.streamInput())) {
                withParser.accept(parser);
            }
        } else {
            withParser.accept(null);
        }
    }
}
