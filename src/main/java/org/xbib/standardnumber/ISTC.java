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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 21047 International Standard Text Code (ISTC)
 *
 * The International Standard Text Code (ISTC) is a numbering system for the unique identification
 * of text-based works; the term “work” can refer to any content appearing in conventional
 * printed books, audio-books, static e-books or enhanced digital books, as well as content
 * which might appear in a newspaper or journal.
 *
 * The ISTC provides sales analysis systems, retail websites, library catalogs and other
 * bibliographic systems with a method of automatically linking together publications
 * of the “same content” and/or “related content”, thus improving discoverability of
 * products and efficiencies.
 *
 * An ISTC number is the link between a user’s search for a piece of content and the
 * ultimate sale or loan of a publication.
 *
 * The standard was formally published in March 2009
 *
 * Checksum algorithm is ISO 7064 MOD 16/3
 *
 */
public class ISTC extends AbstractStandardNumber implements Comparable<ISTC>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("^[\\p{Alnum}\\-\\s]{12,24}");

    private String value;

    private String formatted;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "istc";
    }

    @Override
    public int compareTo(ISTC istc) {
        return istc != null ? normalizedValue().compareTo(istc.normalizedValue()) : -1;
    }

    @Override
    public ISTC set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public ISTC createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public ISTC normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = clean(value.substring(m.start(), m.end()));
        }
        return this;
    }

    @Override
    public boolean isValid() {
        return value != null && !value.isEmpty() && check();
    }

    @Override
    public ISTC verify() throws NumberFormatException {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("invalid");
        }
        if (!check()) {
            throw new NumberFormatException("bad createChecksum");
        }
        return this;
    }

    @Override
    public String normalizedValue() {
        return value;
    }

    @Override
    public String format() {
        return formatted;
    }

    @Override
    public ISTC reset() {
        this.value = null;
        this.formatted = null;
        this.createWithChecksum = false;
        return this;
    }

    private boolean check() {
        int l = value.length() - 1;
        int checksum = 0;
        int weight;
        int factor;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i);
            if (val >= 'A' && val <= 'Z') {
                val = 10 + (val - 'A');
            }
            if (val >= '0' && val <= '9') {
                val = val - '0';
            }
            factor = i % 4 < 2 ? 1 : 5;
            weight = (12 - 2 * (i % 4)) - factor; // --> 11,9,3,1
            checksum += val * weight;
        }
        int chk = checksum % 16;
        if (createWithChecksum) {
            char ch = chk > 9 ? (char)(10 + (chk - 'A')) : (char)('0' + chk);
            value = value.substring(0, l) + ch;
        }
        char digit = value.charAt(l);
        int chk2 = (digit >= '0' && digit <= '9') ? digit - '0' : digit -'A' + 10;
        return chk == chk2;
    }

    private String clean(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(raw);
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
        if (sb.indexOf("ISTC") == 0) {
            sb = new StringBuilder(sb.substring(4));
        }
        if (sb.length() > 15) {
            this.formatted = "ISTC "
                + sb.substring(0,3) + "-"
                + sb.substring(3,7) + "-"
                + sb.substring(7,15) + "-"
                + sb.substring(15);
        }
        return sb.toString();
    }
}
