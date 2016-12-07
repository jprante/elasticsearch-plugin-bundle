package org.xbib.elasticsearch.common.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 15707 International Standard Musical Work Code (ISWC)
 *
 * International Standard Musical Work Code (ISWC) is a unique identifier for
 * musical works, similar to ISBN.
 *
 * Its primary purpose is in collecting society administration, and to clearly identify works in
 * legal contracts. It would also be useful in library cataloging.
 *
 * Due to the fact that a musical work can have multiple authors, it is inevitable that,
 * on rare occasions, a duplicate ISWC might exist and might not be detected immediately.
 *
 * Because of the existing business practices among collecting societies, it is not possible
 * to simply declare an ISWC as obsolete. In such cases, as soon as they are identified,
 * the system will deal with duplicate registrations by linking such registration records
 * in the ISWC database.
 */
public class ISWC extends AbstractStandardNumber implements Comparable<ISWC>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[\\p{Alnum}\\p{Pd}\\s]{10,13}");

    private String value;

    private String formatted;

    @Override
    public String type() {
        return "iswc";
    }

    @Override
    public int compareTo(ISWC iswc) {
        return iswc != null ? normalizedValue().compareTo(iswc.normalizedValue()) : -1;
    }

    @Override
    public ISWC set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public ISWC createChecksum(boolean createChecksum) {
        return this;
    }

    @Override
    public ISWC normalize() {
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
    public ISWC verify() throws NumberFormatException {
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
    public ISWC reset() {
        this.value = null;
        this.formatted = null;
        return this;
    }

    private boolean check() {
        int l = value.length();
        int checksum = 1;
        int val;
        int weight;
        for (int i = 1; i < l; i++) {
            val = value.charAt(i) - '0';
            weight = i < l - 1 ? i : 1;
            checksum += val * weight;
        }
        int chk = checksum % 10;
        return chk == 0;
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
        if (sb.indexOf("ISWC") == 0) {
            sb = new StringBuilder(sb.substring(4));
        }
        if (sb.length() > 10) {
            this.formatted = "ISWC "
                    + "T-"
                    + sb.substring(1, 10) + "-"
                    + sb.substring(10, 11);
        }
        return sb.toString();
    }
}
