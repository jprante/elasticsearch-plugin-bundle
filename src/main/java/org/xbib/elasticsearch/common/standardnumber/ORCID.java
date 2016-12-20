package org.xbib.elasticsearch.common.standardnumber;

import java.net.URI;

/**
 * Open Researcher and Contributor ID - ORCID
 *
 * ORCID is comptabible to International Standard Name Identifier (ISNI)  ISO 2772
 *
 * Checksum is on accordance to ISO/IEC 7064:2003, MOD 11-2
 */
public class ORCID extends ISNI {

    @Override
    public String type() {
        return "orcid";
    }

    @Override
    public ORCID set(CharSequence value) {
        super.set(value);
        return this;
    }

    @Override
    public ORCID createChecksum(boolean createChecksum) {
        super.createChecksum(createChecksum);
        return this;
    }

    @Override
    public ORCID normalize() {
        super.normalize();
        return this;
    }

    @Override
    public ORCID verify() {
        super.verify();
        return this;
    }

    public URI toURI() {
        return URI.create("http://orcid.org/" + normalizedValue());
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ORCID && value.equals(((ORCID)object).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
