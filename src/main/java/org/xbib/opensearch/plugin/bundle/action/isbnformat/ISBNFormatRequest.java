package org.xbib.opensearch.plugin.bundle.action.isbnformat;

import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;

import static org.opensearch.action.ValidateActions.addValidationError;

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
