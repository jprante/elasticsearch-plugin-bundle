package org.xbib.elasticsearch.plugin.bundle.action.langdetect;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;
import org.xbib.elasticsearch.plugin.bundle.common.langdetect.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.rest.RestStatus.OK;

/**
 * Language detection response.
 */
public class LangdetectResponse extends ActionResponse implements StatusToXContentObject {

    private String profile;

    private List<Language> languages = new ArrayList<>();

    LangdetectResponse() {
    }

    LangdetectResponse(StreamInput streamInput) throws IOException {
        this.profile = streamInput.readOptionalString();
        this.languages = streamInput.readList(Language::new);
    }

    public LangdetectResponse setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public String getProfile() {
        return profile;
    }

    public LangdetectResponse setLanguages(List<Language> languages) {
        this.languages = languages;
        return this;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        if (!Strings.isNullOrEmpty(profile)) {
            builder.field("profile", profile);
        }
        builder.startArray("languages");
        for (Language lang : languages) {
            builder.startObject().field("language", lang.getLanguage())
                    .field("probability", lang.getProbability()).endObject();
        }
        builder.endArray();
        return builder;
    }

    @Override
    public RestStatus status() {
        return OK;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeOptionalString(profile);
        out.writeList(languages);
    }
}
