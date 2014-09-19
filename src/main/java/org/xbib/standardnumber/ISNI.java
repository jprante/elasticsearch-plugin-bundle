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

import org.xbib.standardnumber.check.iso7064.MOD112;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 27729 International Standard Name Identifier (ISNI)
 *
 * The International Standard Name Identifier (ISNI) is a method for uniquely identifying
 * the public identities of contributors to media content such as books, TV programmes,
 * and newspaper articles. Such an identifier consists of 16 numerical digits divided
 * into four blocks.
 *
 * Checksum is in accordance to ISO/IEC 7064:2003, MOD 11-2
 */
public class ISNI extends AbstractStandardNumber implements Comparable<ISNI>, StandardNumber {

    private final static Pattern PATTERN = Pattern.compile("[\\p{Digit}xX\\-\\s]{16,24}");

    private String value;

    private String formatted;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "isni";
    }

    /**
     * Creates a new ISNI
     *
     * @param value the value
     */
    @Override
    public ISNI set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public ISNI createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public int compareTo(ISNI isni) {
        return value != null ? value.compareTo((isni).normalizedValue()) : -1;
    }

    @Override
    public boolean isValid() {
        return value != null && !value.isEmpty() && check();
    }

    @Override
    public ISNI verify() throws NumberFormatException {
        if (!check()) {
            throw new NumberFormatException("bad createChecksum");
        }
        return this;
    }

    /**
     * Returns the value representation of the standard number
     * @return value
     */
    @Override
    public String normalizedValue() {
        return value;
    }

    /**
     * Format this number
     *
     * @return the formatted number
     */
    @Override
    public String format() {
        if (formatted == null) {
            this.formatted = value;
        }
        return formatted;
    }

    @Override
    public ISNI normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = clean(value.substring(m.start(), m.end()));
        }
        return this;
    }

    @Override
    public ISNI reset() {
        this.value = null;
        this.formatted = null;
        this.createWithChecksum = false;
        return this;
    }

    private final static MOD112 check = new MOD112();

    private boolean check() {
        if (createWithChecksum) {
            this.value = check.encode(value.length() < 16 ? value : value.substring(0, value.length()-1));
        }
        if (value.length() < 16) {
            return false;
        }
        return check.verify(value);
    }

    private String clean(String isbn) {
        StringBuilder sb = new StringBuilder(isbn);
        int i = sb.indexOf("-");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf("-");
        }
        i = sb.indexOf(" ");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf(" ");
        }
        return sb.toString();
    }

}
