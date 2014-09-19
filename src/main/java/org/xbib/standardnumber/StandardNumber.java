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
package org.xbib.standardnumber;

import java.util.Collection;

/**
 * A standard number is a number that
 *
 * - is backed by an international standard or a de-facto community use
 *
 * - can accept alphanumeric values (digits and letters and separator characters)
 *
 * - can be normalizedValue
 *
 * - can be verified and raise en error is verification fails
 *
 * - must have a createChecksum
 *
 * - can be formatted to a printable representation
 *
 */
public interface StandardNumber {

    /**
     * Return the type of this standard number
     *
     * @return the type
     */
    String type();

    /**
     * Set the input value of this standard number. The input must be normalized
     * and verified before being accepted as valid.
     * @param value the raw input value
     * @return this standard number
     */
    StandardNumber set(CharSequence value);

    /**
     * Normalize the value by removing all unwanted characters or
     * replacing characters with the ones required for verification.
     * @return this standard number
     */
    StandardNumber normalize();

    /**
     * Check this number for validity.
     * @return true if valid, false otherwise
     */
    boolean isValid();

    /**
     * Verify the number.
     * @return this standard number if verification was successful
     * @throws NumberFormatException if verification failed
     */
    StandardNumber verify() throws NumberFormatException;

    /**
     * Indicate that a correct check sum should be computed.
     * @return this standard number
     */
    StandardNumber createChecksum(boolean withChecksum);

    /**
     * Return normalized value of this standard number.
     * In most cases, this is also the canonical form of the standard number.
     * This is a representation without unneccessary characters, useful
     * for computation purposes, like comparing for equivalence.
     * @return the normalized value
     */
    String normalizedValue();

    /**
     * Return a formatted value of this standard number
     * This is best for human-readable representation, but is
     * not necessarily a format for computation.
     *
     * @return a formatted value
     */
    String format();

    StandardNumber reset();

    Collection<String> getTypedVariants();
}
