package org.xbib.standardnumber.check;

public interface Digit {

    /**
     * Add check digits to a string containing digits
     * @param digits  the input data string containing only digits '0'-'9'
     * @return a new string containing data and check digits
     */
    String encode(String digits);

    /**
     * Verify a string that has been encoded with a check digit
     *
     * @param digits input digits
     * @return true if valid, false otherwise
     */
    boolean verify(String digits);

    /**
     * Computes the check digit value
     *
     * @param digits - a string containing data
     * @return an integer representing the check digit
     */
    int compute(String digits);

    /**
     * Extract just the check digit from an encoded string
     *
     * @param digits input data containing check and data digits
     * @return the check digit, as an int
     */
    int getDigit(String digits);

    /**
     * Extracts the raw data without the check digits
     *
     * @param digits -- A string containing only digits 0-9
     * @return a string without check digits
     */
    String getNumber(String digits);
}
