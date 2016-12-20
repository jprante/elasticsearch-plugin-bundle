package org.xbib.elasticsearch.action.isbnformat;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

import static org.elasticsearch.action.ValidateActions.addValidationError;

/**
 *
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

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        value = in.readString();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(value);
    }
}
