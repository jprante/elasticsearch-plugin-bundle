package org.xbib.opensearch.plugin.bundle.action.langdetect;

import org.opensearch.action.ActionType;

/**
 * Language detection action.
 */
public class LangdetectAction extends ActionType<LangdetectResponse> {

    public static final String NAME = "generic:langdetect";

    public static final LangdetectAction INSTANCE = new LangdetectAction();

    private LangdetectAction() {
        super(NAME, LangdetectResponse::new);
    }

}
