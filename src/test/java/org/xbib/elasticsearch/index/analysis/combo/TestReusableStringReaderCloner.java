
package org.xbib.elasticsearch.index.analysis.combo;

import org.testng.annotations.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

import java.io.IOException;
import java.io.Reader;

/**
 * Testcase for {@link ReusableStringReaderCloner}
 */
@Test
public class TestReusableStringReaderCloner extends BaseTokenStreamTest {

    @Test
    public void testCloningReusableStringReader() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        // This test cannot be located inside TestReaderCloneFactory
        // because of the ReusableStringReader class being package private
        // (and it's a real pain to use Java reflection to gain access to
        //  a package private constructor)
        Reader clone;
        ReusableStringReader reader = new ReusableStringReader();
        reader.setValue("test string");
        ReaderCloneFactory.ReaderCloner<Reader> cloner = ReaderCloneFactory.getCloner(reader);
        assertNotNull(cloner);
        assertEquals(cloner.getClass().getName(), ReusableStringReaderCloner.class.getName());
        clone = cloner.giveAClone();
        ReaderContent.assertReaderContent(clone, "test string");
        clone = cloner.giveAClone();
        ReaderContent.assertReaderContent(clone, "test string");

        // Test reusability
        ReaderCloneFactory.ReaderCloner<ReusableStringReader> forClassClonerStrict = ReaderCloneFactory.getClonerStrict(ReusableStringReader.class);
        assertNotNull(forClassClonerStrict);
        assertEquals(forClassClonerStrict.getClass().getName(), ReusableStringReaderCloner.class.getName());
        reader.setValue("another test string");
        forClassClonerStrict.init(reader);
        clone = forClassClonerStrict.giveAClone();
        ReaderContent.assertReaderContent(clone, "another test string");
        clone = forClassClonerStrict.giveAClone();
        ReaderContent.assertReaderContent(clone, "another test string");
        reader.setValue("test string");
        forClassClonerStrict.init(reader);
        clone = forClassClonerStrict.giveAClone();
        ReaderContent.assertReaderContent(clone, "test string");
        clone = forClassClonerStrict.giveAClone();
        ReaderContent.assertReaderContent(clone, "test string");

        ReaderCloneFactory.ReaderCloner<Reader> forClassCloner = ReaderCloneFactory.getCloner(ReusableStringReader.class);
        assertNotNull(forClassCloner);
        assertEquals(forClassCloner.getClass().getName(), ReusableStringReaderCloner.class.getName());
        reader.setValue("another test string");
        forClassCloner.init(reader);
        clone = forClassCloner.giveAClone();
        ReaderContent.assertReaderContent(clone, "another test string");
        clone = forClassCloner.giveAClone();
        ReaderContent.assertReaderContent(clone, "another test string");
        reader.setValue("test string");
        forClassCloner.init(reader);
        clone = forClassCloner.giveAClone();
        ReaderContent.assertReaderContent(clone, "test string");
        clone = forClassCloner.giveAClone();
        ReaderContent.assertReaderContent(clone, "test string");
    }

}
