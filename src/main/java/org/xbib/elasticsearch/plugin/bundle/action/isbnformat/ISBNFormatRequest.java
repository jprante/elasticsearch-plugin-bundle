package org.xbib.elasticsearch.plugin.bundle.action.isbnformat;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 * ISBN format request.
 */
public class ISBNFormatRequest extends ActionRequest {

    private String value;

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (value == null) {
            validationException = addValidationError("value is missing", null);
        }
        return validationException;
    }

    public String getValue() {
        return value;
    }

    public ISBNFormatRequest setValue(String value) {
        this.value = value;
        return this;
    }
}
