package org.xbib.elasticsearch.common.standardnumber.check.iso7064;

import org.xbib.elasticsearch.common.standardnumber.check.Digit;

import java.math.BigDecimal;

public class MOD9710 implements Digit {

    private static final BigDecimal CONSTANT_97 = new BigDecimal(97);

    @Override
    public String encode(String digits) {
        int c = compute(digits);
        if (c == 0) {
            return digits + "00";
        } else if (c < 10) {
            return digits + '0' + c;
        } else {
            return digits + c;
        }
    }

    @Override
    public boolean verify(String digits) {
        try {
            return new BigDecimal(digits != null ? digits : "0").remainder(CONSTANT_97).intValue() == 1;
        } catch (NumberFormatException e) {
            // invalid big decimal
            return false;
        }
    }

    @Override
    public int compute(String digits) {
        return new BigDecimal(digits).remainder(CONSTANT_97).intValue();
    }

    @Override
    public int getDigit(String digits) {
        return Integer.parseInt(digits.substring(digits.length() - 2));
    }

    @Override
    public String getNumber(String digits) {
        return digits.substring(0, digits.length() - 2);
    }
}
