package org.xbib.elasticsearch.plugin.bundle.action.isbnformat;

import org.elasticsearch.action.ActionType;

/**
 * ISBN format action.
 */
public class ISBNFormatAction
        extends ActionType<ISBNFormatResponse> {

    public static final String NAME = "generic:isbnformat";

    public static final ISBNFormatAction INSTANCE = new ISBNFormatAction();

    private ISBNFormatAction() {
        super(NAME, ISBNFormatResponse::new);
    }
}
