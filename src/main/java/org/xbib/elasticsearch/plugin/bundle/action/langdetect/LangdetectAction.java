package org.xbib.elasticsearch.plugin.bundle.action.langdetect;

import org.elasticsearch.action.ActionType;

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
