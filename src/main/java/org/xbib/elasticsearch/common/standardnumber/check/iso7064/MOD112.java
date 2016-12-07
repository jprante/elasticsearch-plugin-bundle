package org.xbib.elasticsearch.common.standardnumber.check.iso7064;

import org.xbib.elasticsearch.common.standardnumber.check.Digit;

/**
 *
 */
public class MOD112 implements Digit {

    @Override
    public String encode(String digits) {
        int c = compute(digits);
        return digits + (c == 10 ? 'X' : c);
    }

    @Override
    public boolean verify(String digits) {
        return compute(getNumber(digits)) == getDigit(digits);
    }

    @Override
    public int compute(String digits) {
        int p = 0;
        for (int i = 0; i < digits.length(); ++i) {
            int c = digits.charAt(i) - '0';
            p = 2 * (p + c);
        }
        p = p % 11;
        return (12 - p) % 11;
    }

    @Override
    public int getDigit(String digits) {
        char c = digits.charAt(digits.length() - 1);
        return c == 'X' || c == 'x' ? 10 : c - '0';
    }

    @Override
    public String getNumber(String digits) {
        return digits.substring(0, digits.length() - 1);
    }

}
