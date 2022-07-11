package org.xbib.opensearch.plugin.bundle.action.langdetect;

import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionRequestValidationException;
import org.opensearch.common.io.stream.StreamOutput;

import java.io.IOException;

import static org.opensearch.action.ValidateActions.addValidationError;

/**
 * Language detection request.
 */
public class LangdetectRequest extends ActionRequest {

    private String profile;

    private String text;

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        if (text == null) {
            validationException = addValidationError("text is missing", null);
        }
        return validationException;
    }

    public String getProfile() {
        return profile;
    }

    public LangdetectRequest setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public String getText() {
        return text;
    }

    public LangdetectRequest setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(text);
        out.writeOptionalString(profile);
    }
}
