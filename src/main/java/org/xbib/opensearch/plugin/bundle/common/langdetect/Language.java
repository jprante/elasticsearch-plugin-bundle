package org.xbib.opensearch.plugin.bundle.common.langdetect;

import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.io.stream.Writeable;

import java.io.IOException;

/**
 * Language.
 */
public class Language implements Writeable {

    private final String lang;

    private final double prob;

    public Language(StreamInput in) throws IOException {
        this.lang = in.readString();
        this.prob = in.readDouble();
    }

    public Language(String lang, double prob) {
        this.lang = lang;
        this.prob = prob;
    }

    public String getLanguage() {
        return lang;
    }

    public double getProbability() {
        return prob;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(lang);
        out.writeDouble(prob);
    }

    @Override
    public String toString() {
        return lang + " (prob=" + prob + ")";
    }
}
