package org.xbib.elasticsearch.common.standardnumber;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ARK Archival Resource Key
 *
 * An ARK is a Uniform Resource Locator (URL) that is a multi-purpose identifier
 * for information objects of any type. An ARK contains the label ark: after the
 * hostname, an URL request terminated by '?' returns a brief metadata record,
 * and an URL request terminated by '??' returns metadata that includes a commitment
 * statement from the current service provider.
 *
 * The ARK and its inflections ('?' and '??') gain access to three facets of a
 * provider's ability to provide persistence.
 *
 * Implicit in the design of the ARK scheme is that persistence is purely a matter
 * of service and not a property of a naming syntax.
 *
 * @see <a href="http://tools.ietf.org/html/draft-kunze-ark-18">ARK IETF RFC</a>
 * @see <a href="http://www.cdlib.org/services/uc3/docs/jak_ARKs_Berlin_2012.pdf">10 years ARK</a>
 */
public class ARK extends AbstractStandardNumber implements Comparable<ARK> {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Graph}\\p{Punct}]{0,48}");

    private static final Pattern URI_PATTERN = Pattern.compile("^(ark)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private URI value;

    @Override
    public String type() {
        return "ark";
    }

    @Override
    public int compareTo(ARK ark) {
        return ark != null ? normalizedValue().compareTo(ark.normalizedValue()) : -1;
    }

    @Override
    public ARK set(CharSequence value) {
        try {
            this.value = value != null ? URI.create(value.toString()) : null;
        } catch (IllegalArgumentException e) {
            this.value = null;

        }
        return this;
    }

    @Override
    public ARK createChecksum(boolean checksum) {
        return this;
    }

    @Override
    public ARK normalize() {
        if (value == null) {
            return this;
        }
        String s = value.toString();
        Matcher m = URI_PATTERN.matcher(s);
        if (m.find()) {
            this.value = URI.create(s.substring(m.start(), m.end()));
        }
        m = PATTERN.matcher(s);
        if (m.find()) {
            this.value = URI.create(s.substring(m.start(), m.end()));
        }
        return this;
    }

    @Override
    public boolean isValid() {
        return value != null && "ark".equals(value.getScheme());
    }

    /**
     * No verification.
     *
     * @return this ARK
     * @throws NumberFormatException if verification failed
     */
    @Override
    public ARK verify() throws NumberFormatException {
        if (value == null || !"ark".equals(value.getScheme())) {
            throw new NumberFormatException();
        }
        return this;
    }

    @Override
    public String normalizedValue() {
        return value != null ? value.toString() : null;
    }

    @Override
    public String format() {
        return value != null ? value.toString() : null;
    }

    public URI asURI() {
        return value;
    }

    public ARK reset() {
        this.value = null;
        return this;
    }
}
