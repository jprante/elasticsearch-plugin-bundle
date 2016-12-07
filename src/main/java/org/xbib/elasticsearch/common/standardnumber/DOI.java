package org.xbib.elasticsearch.common.standardnumber;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 26324: Digital Object Identifier System (DOI)
 *
 * Z39.50 BIB-1 Use Attribute 1094
 *
 * DOI is an acronym for "digital object identifier", meaning a "digital identifier of an object"
 * rather than an "identifier of a digital object". The DOI system was initiated by the
 * International DOI Foundation in 1998, and initially developed with the collaboration
 * of some participants in ISO/TC46/SC9. Due to its application in the fields of
 * information and documentation and previous collaboration with some ISO/TC46/SC9 participants,
 * it was introduced as a possible work item in 2004 and further developed from 2006 to 2010.
 *
 * The DOI system is designed to work over the Internet. A DOI name is permanently assigned
 * to an object to provide a resolvable persistent network link to current information about
 * that object, including where the object, or information about it, can be found on the
 * Internet. While information about an object can change over time, its DOI name will not
 * change. A DOI name can be resolved within the DOI system to values of one or more types
 * of data relating to the object identified by that DOI name, such as a URL, an e-mail address,
 * other identifiers and descriptive metadata.
 *
 * The DOI system enables the construction of automated services and transactions.
 * Applications of the DOI system include but are not limited to managing information
 * and documentation location and access; managing metadata; facilitating electronic
 * transactions; persistent unique identification of any form of any data; and commercial
 * and non-commercial transactions.
 *
 * The content of an object associated with a DOI name is described unambiguously
 * by DOI metadata, based on a structured extensible data model that enables the object
 * to be associated with metadata of any desired degree of precision and granularity
 * to support description and services. The data model supports interoperability
 * between DOI applications.
 *
 * The scope of the DOI system is not defined by reference to the type of content
 * (format, etc.) of the referent, but by reference to the functionalities it provides
 * and the context of use. The DOI system provides, within networks of DOI applications,
 * for unique identification, persistence, resolution, metadata and semantic interoperability.
 */
public class DOI extends AbstractStandardNumber implements Comparable<DOI>, StandardNumber {

    private static final Pattern DOI_PATTERN = Pattern.compile("\\b10\\.\\d{4}([.][0-9]+)*/[a-z0-9/\\-.()<>_:;\\\\]+\\b");

    private static final Pattern DOI_URI_PATTERN = Pattern.compile("\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\b");

    private Object raw;

    private String value;

    private URI infoURI;
    private URI httpDoi;
    private URI httpDxDoi;

    @Override
    public String type() {
        return "doi";
    }

    @Override
    public int compareTo(DOI doi) {
        return doi != null ? normalizedValue().compareTo(doi.normalizedValue()) : -1;
    }

    @Override
    public DOI set(CharSequence value) {
        this.raw = value;
        return this;
    }

    @Override
    public DOI createChecksum(boolean checksum) {
        return this;
    }

    @Override
    public DOI normalize() {
        if (raw == null) {
            return this;
        }
        make(raw);
        return this;
    }

    @Override
    public boolean isValid() {
        return value != null;
    }

    @Override
    public DOI verify() throws NumberFormatException {
        if (value == null) {
            throw new NumberFormatException();
        }
        return this;
    }

    @Override
    public String normalizedValue() {
        return value;
    }

    @Override
    public String format() {
        return httpDoi != null ? httpDoi.toString() : ""; // preferred form
    }

    @Override
    public Collection<String> getTypedVariants() {
        return Arrays.asList(
                value != null ? type().toUpperCase() + " " + value : null,
                infoURI != null ? type().toUpperCase() + " " + infoURI.toString() : null,
                httpDoi != null ? type().toUpperCase() + " " + httpDoi.toString() : null,
                httpDxDoi != null ? type().toUpperCase() + " " + httpDxDoi.toString() : null
        );
    }

    public DOI reset() {
        this.value = null;
        return this;
    }

    private void make(Object o) {
        // DOIs are case insensitive in ASCII
        // DOI service only use upper casing, we use lowercasing (better for search engines)
        String content = o.toString().toLowerCase(Locale.US);
        // is it an already a DOI URI?
        Matcher m = DOI_URI_PATTERN.matcher(content);
        if (m.find()) {
            URI u = URI.create(content.substring(m.start(), m.end()));
            if ("http".equals(u.getScheme()) && ("dx.doi.org".equals(u.getHost()) || "doi.org".equals(u.getHost()))) {
                content = u.getRawPath();
            } else {
                return;
            }
        }
        m = DOI_PATTERN.matcher(content);
        if (m.find()) {
            this.value = content.substring(m.start(), m.end());
            this.infoURI = URI.create("info:doi:" + value);
            this.httpDoi = URI.create("http://doi.org/" + value);
            this.httpDxDoi = URI.create("http://dx.doi.org/" + value);
        }
    }
}
