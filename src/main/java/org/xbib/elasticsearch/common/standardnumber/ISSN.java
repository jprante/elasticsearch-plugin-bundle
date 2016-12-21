package org.xbib.elasticsearch.common.standardnumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ISO 3297 International Standard Serial Number (ISSN)
 *
 * Z39.50 BIB-1 Use Attribute 8
 *
 * The International Standard Serial Number (ISSN) is a unique
 * eight-digit number used to identify a print or electronic periodical
 * publication. The ISSN system was adopted as international standard
 * ISO 3297 in 1975. The ISO subcommittee TC 46/SC 9 is responsible
 * for the standard.
 *
 * Quoted from http://www.issn.org/2-22636-All-about-ISSN.php
 *
 * The ISSN (International Standard Serial Number) is an eight-digit number
 * which identifies periodical publications as such, including electronic
 * serials.
 *
 * The ISSN is a numeric code which is used as an identifier: it has no
 * signification in itself and does not contain in itself any information
 * referring to the origin or contents of the publication.
 *
 * The ISSN takes the form of the acronym ISSN followed by two groups
 * of four digits, separated by a hyphen. The eighth character is a
 * control digit calculated according to a modulo 11 algorithm on
 * the basis of the 7 preceding digits; this eighth control character
 * may be an "X" if the result of the computing is equal to "10",
 * in order to avoid any ambiguity.
 *
 * The ISSN is linked to a standardized form of the title of the
 * identified serial, known as the "key title", which repeats
 * the title of the publication, qualifying it with additional elements
 * in order to distinguish it from other publications having identical
 * titles.
 *
 * If the title of the publication changes in any significant way,
 * a new ISSN must be assigned in order to correspond to this new form
 * of title and avoid any confusion. A serial publication whose
 * title is modified several times in the course of its existence
 * will be assigned each time a new ISSN, thus allowing precise
 * identification of each form of the title : in fact it is then
 * considered that they are different publications even if there
 * is a logical link between them.
 *
 * Contrary to other types of publications, the world of serial
 * publications is particularly changeable and complex :
 * the lifetime of a title may be extremely short; many publications
 * may be part of a complex set of relationships, etc.
 * These particularities themselves necessitated the introduction
 * of the ISSN.
 */
public class ISSN extends AbstractStandardNumber implements Comparable<ISSN>, StandardNumber {

    private static final Pattern PATTERN = Pattern.compile("[0-9]{4}\\p{Pd}?[0-9]{3}[0-9xX]");

    private String value;

    private String formatted;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "issn";
    }

    @Override
    public int compareTo(ISSN issn) {
        return value != null ? normalizedValue().compareTo(issn.normalizedValue()) : -1;
    }

    @Override
    public ISSN set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public ISSN createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public ISSN normalize() {
        Matcher m = PATTERN.matcher(value);
        this.value = m.find() ? dehyphenate(value.substring(m.start(), m.end())) : null;
        return this;
    }

    @Override
    public boolean isValid() {
        return value != null && !value.isEmpty() && check();
    }

    @Override
    public ISSN verify() {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("invalid");
        }
        if (!check()) {
            throw new NumberFormatException("bad checksum");
        }
        return this;
    }

    /**
     * Returns the value representation of the standard number
     *
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
        return formatted;
    }

    @Override
    public ISSN reset() {
        this.value = null;
        this.formatted = null;
        this.createWithChecksum = false;
        return this;
    }

    public GTIN toGTIN() throws NumberFormatException {
        return new GTIN().set("977" + value.substring(0, 7) + "000").createChecksum(true).normalize().verify();
    }

    public GTIN toGTIN(String additionalCode) throws NumberFormatException {
        // "977" + ISSN + add-on + placeholder for createChecksum
        return new GTIN().set("977" + value.substring(0, 7) + additionalCode + "0").createChecksum(true).normalize().verify();
    }

    private boolean check() {
        int l = createWithChecksum ? value.length() : value.length() - 1;
        int checksum = 0;
        int weight;
        int val;
        for (int i = 0; i < l; i++) {
            val = value.charAt(i) - '0';
            weight = 8 - i;
            checksum += weight * val;
        }
        int chk = checksum % 11;
        char p = chk == 0 ? '0' : chk == 1 ? 'X' : (char) ((11 - chk) + '0');
        return p == Character.toUpperCase(value.charAt(l));
    }

    private String dehyphenate(String isbn) {
        StringBuilder sb = new StringBuilder(isbn);
        int i = sb.indexOf("-");
        while (i > 0) {
            sb.deleteCharAt(i);
            i = sb.indexOf("-");
        }
        if (sb.length() > 7) {
            this.formatted = sb.substring(0, 4) + "-" + sb.substring(4, 8);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ISSN && value.equals(((ISSN)object).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
