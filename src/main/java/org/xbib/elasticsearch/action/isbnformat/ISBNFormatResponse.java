package org.xbib.elasticsearch.action.isbnformat;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.xcontent.StatusToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;

import java.io.IOException;

import static org.elasticsearch.rest.RestStatus.OK;

/**
 *
 */
public class ISBNFormatResponse extends ActionResponse implements StatusToXContent {

    private String isbn10;

    private String isbn10Formatted;

    private String isbn13;

    private String isbn13Formatted;

    private String invalid;

    public ISBNFormatResponse setIsbn10(String value) {
        this.isbn10 = value;
        return this;
    }

    public ISBNFormatResponse setIsbn10Formatted(String value) {
        this.isbn10Formatted = value;
        return this;
    }

    public ISBNFormatResponse setIsbn13(String value) {
        this.isbn13 = value;
        return this;
    }

    public ISBNFormatResponse setIsbn13Formatted(String value) {
        this.isbn13Formatted = value;
        return this;
    }

    public ISBNFormatResponse setInvalid(String value) {
        this.invalid = value;
        return this;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject("result")
                .field("isbn10", isbn10)
                .field("isbn10formatted", isbn10Formatted)
                .field("isbn13", isbn13)
                .field("isbn13formatted", isbn13Formatted)
                .field("invalid", invalid)
                .endObject();
        return builder;
    }

    @Override
    public RestStatus status() {
        return OK;
    }
}
