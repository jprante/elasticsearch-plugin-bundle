package org.xbib.elasticsearch.common.standardnumber.check;

/**
 *
 */
public class LuhnMOD10 implements Digit {

    @Override
    public String encode(String digits) {
        return digits + compute(digits);
    }

    @Override
    public boolean verify(String digits) {
        return computeSum(digits) % 10 == 0;
    }

    @Override
    public int compute(String digits) {
        int val = computeSum(digits);
        return (val == 0) ? 0 : (10 - val);
    }

    @Override
    public int getDigit(String digits) {
        return digits.charAt(digits.length() - 1) - '0';
    }

    @Override
    public String getNumber(String digits) {
        return digits.substring(0, digits.length() - 1);
    }

    private int computeSum(String digits) {
        int val = 0;
        for (int i = 0; i < digits.length(); i += 2) {
            int c = digits.charAt(i) - '0';
            val += c;
        }
        for (int i = 1; i < digits.length(); i += 2) {
            int c = digits.charAt(i) - '0';
            val += (c >= 5) ? (2 * c - 9) : (2 * c);
        }
        return val % 10;
    }
}
