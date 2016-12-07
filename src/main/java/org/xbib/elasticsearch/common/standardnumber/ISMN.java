package org.xbib.elasticsearch.common.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 10957 International Standard Music Number (ISMN)
 *
 * Z39.50 BIB-1 Use Attribute 1092
 *
 * The International Standard Music Number (ISMN) is a thirteen-character alphanumeric identifier
 * for printed music developed by ISO. The original proposal for an ISMN was made by the
 * UK Branch of IAML (International Association of Music Libraries, Archives and Documentation
 * Centres).
 *
 * The original format comprised four elements: a distinguishing prefix M, a publisher ID,
 * an item ID and a check digit, typically looking like M-2306-7118-7.
 *
 * From 1 January 2008 the ISMN was defined as a thirteen digit identifier beginning 979-0 where
 * the zero replaced M in the old-style number. The resulting number is identical with its
 * EAN-13 number as encoded in the item's barcode.
 *
 * @see <a href="http://www.ismn-international.org/download/Web_ISMN%20Manual_2008-3.pdf">ISMN Manual 2008</a>
 */
public class ISMN extends AbstractStandardNumber implements Comparable<ISMN>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Digit}M\\p{Pd}]{0,17}");

    private String value;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "ismn";
    }

    @Override
    public int compareTo(ISMN ismn) {
        return ismn != null ? normalizedValue().compareTo(ismn.normalizedValue()) : -1;
    }

    @Override
    public ISMN set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public ISMN createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public ISMN normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = value.substring(m.start(), m.end());
            this.value = (value.startsWith("979") ? "" : "979") + dehyphenate(value.replace('M', '0'));
        }
        return this;
    }

    @Override
    public boolean isValid() {
        return value != null && !value.isEmpty() && check();
    }

    @Override
    public ISMN verify() throws NumberFormatException {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("invalid");
        }
        if (!check()) {
            throw new NumberFormatException("invalid createChecksum");
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
    public ISMN reset() {
        this.value = null;
        this.createWithChecksum = false;
        return this;
    }

    public GTIN toGTIN() throws NumberFormatException {
        return new GTIN().set(value).normalize().verify();
    }

    private boolean check() {
        int l = createWithChecksum ? value.length() : value.length() - 1;
        int checksum = 0;
        int weight;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i) - '0';
            weight = i % 2 == 0 ? 1 : 3;
            checksum += val * weight;
        }
        int chk = 10 - checksum % 10;
        if (createWithChecksum) {
            char ch = (char) ('0' + chk);
            value = value + ch;
        }
        return chk == value.charAt(l) - '0';
    }

    private String dehyphenate(String isbn) {
        StringBuilder sb = new StringBuilder(isbn);
        int i = sb.indexOf("-");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf("-");
        }
        return sb.toString();
    }
}
