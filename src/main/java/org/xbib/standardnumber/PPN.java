package org.xbib.standardnumber;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pica Productie Nummer
 *
 * A catalog record numbering system, uniquely identifying records, used by PICA
 * (Project voor geIntegreerde Catalogus Automatisering) integrated library systems.
 *
 */
public class PPN extends AbstractStandardNumber implements Comparable<PPN>, StandardNumber {

    private final static Pattern PATTERN = Pattern.compile("[\\p{Digit}]{3,10}\\-{0,1}[\\p{Digit}xX]{1}\\b");

    private String value;

    private String formatted;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "ppn";
    }

    @Override
    public PPN set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public int compareTo(PPN o) {
        return value != null ? normalizedValue().compareTo(o.normalizedValue()) : -1;
    }

    @Override
    public String normalizedValue() {
        return value;
    }

    @Override
    public PPN createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public PPN normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = dehyphenate(value.substring(m.start(), m.end()));
        }
        return this;
    }

    @Override
    public boolean isValid() {
        return value != null && !value.isEmpty() && check();
    }

    @Override
    public PPN verify() throws NumberFormatException {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("invalid");
        }
        if (!check()) {
            throw new NumberFormatException("bad checksum");
        }
        return this;
    }

    @Override
    public String format() {
        if (formatted == null) {
            StringBuilder sb = new StringBuilder(value);
            this.formatted = sb.insert(sb.length()-1,"-").toString();
        }
        return formatted;
    }

    @Override
    public PPN reset() {
        this.value = null;
        this.formatted = null;
        this.createWithChecksum = false;
        return this;
    }

    private boolean check() {
        int checksum = 0;
        int weight = 2;
        int val;
        int l = value.length() - 1;
        for (int i = l - 1; i >= 0; i--) {
            val = value.charAt(i) - '0';
            checksum += val * weight++;
        }
        if (createWithChecksum) {
            char ch = checksum % 11 == 10 ? 'X' : (char)('0' + (checksum % 11));
            value = value.substring(0, l) + ch;
        }
        return 11 - checksum % 11 ==
                (value.charAt(l) == 'X' || value.charAt(l) == 'x' ? 10 : value.charAt(l) - '0');
    }

    private String dehyphenate(String value) {
        StringBuilder sb = new StringBuilder(value);
        int i = sb.indexOf("-");
        while (i >= 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf("-");
        }
        return sb.toString();
    }
}
