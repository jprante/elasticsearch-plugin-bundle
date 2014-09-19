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
