
package org.xbib.standardnumber;

import org.junit.Test;
import org.xbib.elasticsearch.common.standardnumber.ISBN;

import static org.junit.Assert.assertEquals;

public class ISBNTests {

    @Test
    public void testDehypenate() {
        assertEquals("000111333", new ISBN().set("000-111-333").normalize().normalizedValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testISBNTooShort() throws Exception {
        new ISBN().set("12-7").normalize().verify();
    }

    @Test
    public void testDirtyISBN() throws Exception {
        String value = "ISBN 3-9803350-5-4 kart. : DM 24.00";
        ISBN isbn = new ISBN().set(value).normalize().verify();
        assertEquals(isbn.normalizedValue(), "3980335054");
    }

    @Test(expected = NumberFormatException.class)
    public void testTruncatedISBN() throws Exception {
        String value = "ISBN";
        new ISBN().set(value).normalize().verify();
    }

    @Test
    public void fixChecksum() throws Exception {
        String value = "3616065810";
        ISBN isbn = new ISBN().set(value).createChecksum(true).normalize().verify();
        assertEquals("361606581X", isbn.normalizedValue());
    }

    @Test
    public void testEAN() throws Exception {
        String value = "978-3-551-75213-0";
        ISBN ean = new ISBN().set(value).ean(true).normalize().verify();
        assertEquals("9783551752130", ean.normalizedValue());
        assertEquals("978-3-551-75213-0", ean.format());
    }

    @Test
    public void testEAN2() throws Exception {
        String value = "978-3-551-75213-1";
        ISBN ean = new ISBN().set(value).ean(true).createChecksum(true).normalize().verify();
        assertEquals("9783551752130", ean.normalizedValue());
        assertEquals("978-3-551-75213-0", ean.format());
    }

    @Test(expected = NumberFormatException.class)
    public void testWrongAndDirtyEAN() throws Exception {
        // correct ISBN-10 is 3-451-04112-X
        String value = "ISBN ISBN 3-451-4112-X kart. : DM 24.80";
        new ISBN().set(value).ean(false).createChecksum(true).normalize().verify();
    }

    @Test
    public void testVariants() throws Exception {
        String content = "1-9339-8817-7.";
        ISBN isbn = new ISBN().set(content).normalize();
        if (!isbn.isEAN()) {
            // create up to 4 variants: ISBN, ISBN normalized, ISBN-13, ISBN-13 normalized
            if (isbn.isValid()) {
                assertEquals("1-933988-17-7", isbn.ean(false).format());
                assertEquals("1933988177", isbn.ean(false).normalizedValue());
            }
            isbn = isbn.ean(true).set(content).normalize();
            if (isbn.isValid()) {
                assertEquals("978-1-933988-17-7", isbn.format());
                assertEquals("9781933988177", isbn.normalizedValue());
            }
        } else {
            // 2 variants, do not create ISBN-10 for an ISBN-13
            if (isbn.isValid()) {
                assertEquals(isbn.ean(true).format(), "978-1-933988-17-7");
                assertEquals(isbn.ean(true).normalizedValue(), "9781933988177");
            }
        }
    }
}
