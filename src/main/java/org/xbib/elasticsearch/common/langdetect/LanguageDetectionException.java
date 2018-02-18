package org.xbib.elasticsearch.common.langdetect;

import java.io.IOException;

/**
 * Language detection exception.
 */
public class LanguageDetectionException extends IOException {

    public LanguageDetectionException(String message) {
        super(message);
    }
}
