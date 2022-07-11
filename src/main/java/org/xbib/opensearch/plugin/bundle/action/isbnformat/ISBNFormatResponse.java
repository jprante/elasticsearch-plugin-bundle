package org.xbib.opensearch.plugin.bundle.action.isbnformat;

import org.opensearch.action.ActionResponse;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.xcontent.StatusToXContentObject;
import org.opensearch.common.xcontent.ToXContent;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.rest.RestStatus;

import java.io.IOException;

import static org.opensearch.rest.RestStatus.OK;

/**
 * ISBN format response.
 */
public class ISBNFormatResponse extends ActionResponse implements StatusToXContentObject {

    private String isbn10;

    private String isbn10Formatted;

    private String isbn13;

    private String isbn13Formatted;

    private String invalid;

    ISBNFormatResponse() {
    }

    ISBNFormatResponse(StreamInput streamInput) throws IOException {
        super(streamInput);
        this.isbn10 = streamInput.readString();
        this.isbn10Formatted = streamInput.readString();
        this.isbn13 = streamInput.readString();
        this.isbn13Formatted = streamInput.readString();
    }

    public ISBNFormatResponse setIsbn10(String value) {
        this.isbn10 = value;
        return this;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public ISBNFormatResponse setIsbn10Formatted(String value) {
        this.isbn10Formatted = value;
        return this;
    }

    public String getIsbn10Formatted() {
        return isbn10Formatted;
    }

    public ISBNFormatResponse setIsbn13(String value) {
        this.isbn13 = value;
        return this;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public ISBNFormatResponse setIsbn13Formatted(String value) {
        this.isbn13Formatted = value;
        return this;
    }

    public String getIsbn13Formatted(){
        return isbn13Formatted;
    }

    public ISBNFormatResponse setInvalid(String value) {
        this.invalid = value;
        return this;
    }

    public String getInvalid() {
        return invalid;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject()
                .startObject("result")
                .field("isbn10", isbn10)
                .field("isbn10formatted", isbn10Formatted)
                .field("isbn13", isbn13)
                .field("isbn13formatted", isbn13Formatted)
                .field("invalid", invalid)
                .endObject()
                .endObject();
        return builder;
    }

    @Override
    public RestStatus status() {
        return OK;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(isbn10);
        out.writeString(isbn10Formatted);
        out.writeString(isbn13);
        out.writeString(isbn13Formatted);
    }
}
