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

import org.xbib.elasticsearch.common.standardnumber.check.iso7064.MOD9710;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  ISO 13616 International Bank Account Number (IBAN)
 *
 * The International Bank Account Number (IBAN) is an internationally agreed means of
 * identifying bank accounts across national borders with a reduced risk of transcription
 * errors. It was originally adopted by the European Committee for Banking Standards (ECBS)
 * and later as an international standard under ISO 13616:1997. The current standard
 * is ISO 13616:2007, which indicates SWIFT as the formal registrar.
 *
 *  Checksum in accordance to ISO 7064 MOD-97
 */
public class IBAN extends AbstractStandardNumber implements Comparable<IBAN>, StandardNumber {

    /**
     * Norway = 15, Malta = 31 + "IBAN "
     *
     */
    private static final Pattern PATTERN = Pattern.compile("[\\p{Alnum}\\-\\s]{15,36}");

    private String formatted;

    private String value;

    private String country;

    private boolean createWithChecksum;

    @Override
    public String type() {
        return "iban";
    }

    @Override
    public int compareTo(IBAN iban) {
        return iban != null ? normalizedValue().compareTo(iban.normalizedValue()) : -1;
    }

    @Override
    public IBAN set(CharSequence value) {
        this.value = value != null ? value.toString() : null;
        return this;
    }

    @Override
    public IBAN createChecksum(boolean createWithChecksum) {
        this.createWithChecksum = createWithChecksum;
        return this;
    }

    @Override
    public IBAN normalize() {
        Matcher m = PATTERN.matcher(value);
        if (m.find()) {
            this.value = parse(value.substring(m.start(), m.end()));
        } else {
            this.value = null;
        }
        if (value != null && createWithChecksum) {
            int c = check.compute(value.substring(0, value.length()-2));
            String chk = String.format("%02d", c);
            this.value = value + chk;
            this.formatted = formatted.substring(0,2) + chk + formatted.substring(4);
        }
        return this;
    }

    @Override
    public boolean isValid() {
        return check();
    }

    @Override
    public IBAN verify() throws NumberFormatException {
        if (value == null || value.isEmpty()) {
            throw new NumberFormatException("invalid");
        }
        if (!check()) {
            throw new NumberFormatException("bad checksum");
        }
        if (formatted.length() != getLengthForCountryCode(country)) {
            throw new NumberFormatException("invalid length for country: "
                    + formatted.length() + " " + formatted);
        }
        return this;
    }

    @Override
    public String normalizedValue() {
        return formatted;
    }

    @Override
    public String format() {
        return formatted;
    }

    @Override
    public IBAN reset() {
        this.value = null;
        this.formatted = null;
        this.country = null;
        this.createWithChecksum = false;
        return this;
    }

    private final static MOD9710 check = new MOD9710();

    private boolean check() {
        return value != null && value.length() >= 15 && check.verify(value);
    }

    private String parse(String raw) {
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
        this.formatted = sb.toString();
        this.country = sb.substring(0,2);
        // move first 4 characters to last
        sb = new StringBuilder(sb.substring(4)).append(sb.substring(0,4));
        // replace characters with decimal values
        for (i = 0; i < sb.length(); i++) {
            char ch = sb.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                sb.deleteCharAt(i);
                String s = Integer.toString(ch - 'A' + 10);
                sb.insert(i, s);
            }
        }
        return sb.toString();
    }

    /**
     * Known country codes, this list must be sorted to allow binary search.
     */
    private static final String[] COUNTRY_CODES = {
            "AD", "AE", "AL", "AO", "AT", "AZ", "BA", "BE", "BF", "BG", "BH", "BI", "BJ", "BR", "CG", "CH", "CI",
            "CM", "CR", "CV", "CY", "CZ", "DE", "DK", "DO", "DZ", "EE", "EG", "ES", "FI", "FO", "FR", "GA", "GB",
            "GE", "GI", "GL", "GR", "GT", "HR", "HU", "IE", "IL", "IR", "IS", "IT", "KW", "KZ", "LB", "LI", "LT",
            "LU", "LV", "MC", "MD", "ME", "MG", "MK", "ML", "MR", "MT", "MU", "MZ", "NL", "NO", "PK", "PL", "PS",
            "PT", "RO", "RS", "SA", "SE", "SI", "SK", "SM", "SN", "TN", "TR", "UA", "VG" };
    /**
     * Lengths for each country's IBAN. The indices match the indices of {@link #COUNTRY_CODES}, the values are the expected length.
     */
    private static final int[] COUNTRY_IBAN_LENGTHS = {
            24 /* AD */, 23 /* AE */, 28 /* AL */, 25 /* AO */, 20 /* AT */, 28 /* AZ */, 20 /* BA */, 16 /* BE */,
            27 /* BF */, 22 /* BG */, 22 /* BH */, 16 /* BI */, 28 /* BJ */, 29 /* BR */, 27 /* CG */, 21 /* CH */,
            28 /* CI */, 27 /* CM */, 21 /* CR */, 25 /* CV */, 28 /* CY */, 24 /* CZ */, 22 /* DE */, 18 /* DK */,
            28 /* DO */, 24 /* DZ */, 20 /* EE */, 27 /* EG */, 24 /* ES */, 18 /* FI */, 18 /* FO */, 27 /* FR */,
            27 /* GA */, 22 /* GB */, 22 /* GE */, 23 /* GI */, 18 /* GL */, 27 /* GR */, 28 /* GT */, 21 /* HR */,
            28 /* HU */, 22 /* IE */, 23 /* IL */, 26 /* IR */, 26 /* IS */, 27 /* IT */, 30 /* KW */, 20 /* KZ */,
            28 /* LB */, 21 /* LI */, 20 /* LT */, 20 /* LU */, 21 /* LV */, 27 /* MC */, 24 /* MD */, 22 /* ME */,
            27 /* MG */, 19 /* MK */, 28 /* ML */, 27 /* MR */, 31 /* MT */, 30 /* MU */, 25 /* MZ */, 18 /* NL */,
            15 /* NO */, 24 /* PK */, 28 /* PL */, 29 /* PS */, 25 /* PT */, 24 /* RO */, 22 /* RS */, 24 /* SA */,
            24 /* SE */, 19 /* SI */, 24 /* SK */, 27 /* SM */, 28 /* SN */, 24 /* TN */, 26 /* TR */, 29 /* UA */,
            24 /* VG */ };

    /**
     * Returns the IBAN length for a given country code.
     * @param countryCode a non-null, uppercase, two-character country code.
     * @return the IBAN length for the given country, or -1 if the input is not a known, two-character country code.
     * @throws NullPointerException if the input is null.
     */
    private int getLengthForCountryCode(String countryCode) {
        int index = Arrays.binarySearch(COUNTRY_CODES, countryCode);
        if (index > -1) {
            return COUNTRY_IBAN_LENGTHS[index];
        }
        return -1;
    }

}
