package org.xbib.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Z39.56 Serial Item and Contribution Identifier
 *
 * The SICI code (Serial Item and Contribution Identifier) is described in the
 * American standard ANSI/NISO Z39.56. The SICI code is known among
 * international scientific publishers and reproduction rights agencies.
 * The SICI even provides for the unambiguous identification of each article
 * or contribution published in a given issue of a serial publication.
 *
 * The SICI contains:
 *
 * - the ISSN
 *
 * - the date of publication, between brackets and formatted according to the
 *   formula YYYYMM
 *
 * - the issue number
 *
 * - the version number of the standard, here 1, preceded by a semicolon
 *
 * - and lastly a hyphen which precedes the control character calculated
 *   on the basis of all the preceding characters
 *
 *  Example:
 *  0095-4403(199502/03)21:3<12:WATIIB>2.0.TX;2-J
 *
 */
public class SICI extends AbstractStandardNumber implements Comparable<SICI>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Graph}\\p{Punct}]{12,64}");

    private String value;

    private String formatted;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "sici";
    }

    @Override
    public int compareTo(SICI sici) {
        return sici != null ? normalizedValue().compareTo(sici.normalizedValue()) : -1;
    }

    @Override
    public SICI set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public SICI createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public SICI normalize() {
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
    public SICI verify() throws NumberFormatException {
        if (value == null) {
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
        return formatted;
    }

    @Override
    public SICI reset() {
        this.value = null;
        this.formatted = null;
        this.createWithChecksum = false;
        return this;
    }

    private final static String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ#";

    private final static int modulus = ALPHABET.length();

    private boolean check() {
        int l = value.length() - 1;
        int val;
        int sum = 0;
        for (int i = 0; i < l; i++) {
            val = ALPHABET.indexOf(value.charAt(i));
            sum += val *  (i %2 == 0 ? 1 : 3);
        }
        int chk = modulus - sum % modulus;
        if (createWithChecksum) {
            char ch = chk > 35 ? '#' : chk > 9 ? (char)(10 + (chk - 'A')) : (char)('0' + chk);
            value = value.substring(0, l) + ch;
        }
        char digit = value.charAt(l);
        int chk2 = digit == '#' ? 36 : (digit >= '0' && digit <= '9') ? digit - '0' : digit -'A' + 10;
        return chk == chk2;
    }

    private String clean(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(raw);
        int pos = sb.indexOf("SICI ");
        if (pos >= 0) {
            sb = new StringBuilder(sb.substring(pos + 5));
        }
        this.formatted = "SICI " + sb;
        return sb.toString();
    }
}
