/*
 * Copyright (C) 2014 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.standardnumber;

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
    public ORCID verify() throws NumberFormatException {
        super.verify();
        return this;
    }

    public URI toURI() {
        return URI.create("http://orcid.org/" + normalizedValue());
    }

}
