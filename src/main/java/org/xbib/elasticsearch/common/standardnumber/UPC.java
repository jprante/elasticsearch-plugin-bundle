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
package org.xbib.elasticsearch.common.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 15420 Universal Product Code (UPC)
 *
 * The Universal Product Code (UPC) is a barcode symbology (i.e., a specific type of barcode)
 * that is widely used in the United States, Canada, the United Kingdom, Australia,
 * New Zealand and in other countries for tracking trade items in stores.
 * Its most common form, the UPC-A, consists of 12 numerical digits, which are uniquely
 * assigned to each trade item.
 *
 * Along with the related EAN barcode, the UPC is the barcode mainly used for scanning
 * of trade items at the point of sale, per GS1 specifications.
 *
 * UPC data structures are a component of GTINs (Global Trade Item Numbers).
 *
 * All of these data structures follow the global GS1 specification which bases on
 * international standards.
 */
public class UPC extends AbstractStandardNumber implements Comparable<UPC>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Digit}]{0,12}");

    private String value;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "upc";
    }

    @Override
    public int compareTo(UPC upc) {
        return upc != null ? normalizedValue().compareTo(upc.normalizedValue()) : -1;
    }

    @Override
    public UPC set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public UPC createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public UPC normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = value.substring(m.start(), m.end());
        }
        return this;
    }

    @Override
    public boolean isValid() {
        return value != null && !value.isEmpty() && check();
    }

    @Override
    public UPC verify() throws NumberFormatException {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("invalid");
        }
        if (!check()) {
            throw new NumberFormatException("bad checksum");
        }
        return this;
    }

    @Override
    public String normalizedValue() {
        return value;
    }

    @Override
    public String format() {
        return value;
    }

    @Override
    public UPC reset() {
        this.value = null;
        this.createWithChecksum = false;
        return this;
    }

    private boolean check() {
        int l = value.length() - 1;
        int checksum = 0;
        int weight;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i) - '0';
            weight = i % 2 == 0 ? 3 : 1;
            checksum += val * weight;
        }
        int chk = 10 - checksum % 10;
        if (createWithChecksum) {
            char ch = (char) ('0' + chk);
            value = value.substring(0, l) + ch;
        }
        return chk == (value.charAt(l) - '0');
    }
}
