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
package org.xbib.elasticsearch.common.standardnumber.check;

/**
 * Verhoeff's algorithm for checksum computation
 *
 * J. Verhoeff, Error Detecting Decimal Codes, Mathematical Centre Tract 29, The Mathematical Centre, Amsterdam, 1969
 */
public class DihedralGroup implements Digit {

    /**
     * dihedral addition matrix A + B = a[A][B]
     */
    private static final int a[][] = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
            { 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 },
            { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 }, { 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 },
            { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 },
            { 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 },
            { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

    /**
     *  dihedral inverse map, A + inverse[A] = 0
     */
    private static final int inverse[] = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

    /**
     * permutation weighting matrix P[position][value]
     */
    private static final int p[][] = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
            { 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 }, { 5, 8, 0, 3, 7, 9, 6, 1, 4, 2 },
            { 8, 9, 1, 6, 0, 4, 3, 5, 2, 7 }, { 9, 4, 5, 3, 1, 2, 6, 8, 7, 0 },
            { 4, 2, 8, 6, 5, 7, 3, 9, 0, 1 }, { 2, 7, 9, 3, 8, 0, 6, 4, 1, 5 },
            { 7, 0, 4, 6, 9, 1, 3, 2, 5, 8 } };


    @Override
    public String encode(String digits) {
        return Integer.toString(compute(digits)) + digits;
    }

    @Override
    public boolean verify(String digits){
        int check = 0;
        for (int i = 0; i < digits.length(); ++i) {
            check = a[check][p[i % 8][digits.charAt(i) - '0']];
        }
        return check == 0;
    }

    @Override
    public int compute(String digits) {
        int check = 0;
        for (int i = 0; i < digits.length(); ++i) {
            int c = digits.charAt(i) - '0';
            check = a[check][p[(i + 1) % 8][c]];
        }
        return inverse[check];
    }

    @Override
    public int getDigit(String digits) {
        return Integer.parseInt(digits.substring(0, 1));
    }

    @Override
    public String getNumber(String digits) {
        return digits.substring(1);
    }
}
