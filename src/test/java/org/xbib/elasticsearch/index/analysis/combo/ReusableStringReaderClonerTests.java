package org.xbib.elasticsearch.index.analysis.combo;

import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

import java.io.IOException;
import java.io.Reader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

public class ReusableStringReaderClonerTests extends BaseTokenStreamTest {

    @Test
    public void test() throws Exception {
        String content = "test\n";
        ReusableStringReader reader = new ReusableStringReader();
        reader.setValue(content);
        ReusableStringReaderCloner cloner = new ReusableStringReaderCloner();
        cloner.init(reader);

        Reader clone1 = cloner.giveAClone();
        // The original Reader can be returned with this implementation.
        // Check it actually is.
        assertThat("returns original reader", clone1, is((Reader) reader));
        assertThat("same content", ReaderContent.readWhole(clone1), equalTo(content));
        assertThat("empty after reading", clone1.read(), equalTo(-1));

        Reader clone2 = cloner.giveAClone();
        assertThat("do not return the previous clone", clone2, not(is(clone1)));
        assertThat("same content", ReaderContent.readWhole(clone2), equalTo(content));
        assertThat("empty after reading", clone2.read(), equalTo(-1));
    }

    @Test
    public void testReuseAfterUse() throws Exception {
        String content = "test\n";
        ReusableStringReader reader = new ReusableStringReader();
        reader.setValue(content);
        ReusableStringReaderCloner cloner = new ReusableStringReaderCloner();
        cloner.init(reader);

        Reader clone1 = cloner.giveAClone();
        // The original Reader can be returned with this implementation.
        // Check it actually is.
        assertThat("returns original reader", clone1, is((Reader) reader));
        assertThat("same content", ReaderContent.readWhole(clone1), equalTo(content));
        assertThat("empty after reading", clone1.read(), equalTo(-1));

        // Now change the content!
        String otherContent = content + " CHANGED";
        assertThat("contents are not equal", otherContent, not(equalTo(content)));
        reader.setValue(otherContent);

        Reader clone2 = cloner.giveAClone();
        assertThat("do not return the previous clone", clone2, not(is(clone1)));
        assertThat("same content as before", ReaderContent.readWhole(clone2), equalTo(content));
        assertThat("empty after reading", clone2.read(), equalTo(-1));
    }

    @Test
    public void testReuseBeforeUse() throws Exception {
        String content = "test\n";
        ReusableStringReader reader = new ReusableStringReader();
        reader.setValue(content);
        ReusableStringReaderCloner cloner = new ReusableStringReaderCloner();
        cloner.init(reader);

        // Now change the content!
        String otherContent = content + " CHANGED";
        assertThat("contents are not equal", otherContent, not(equalTo(content)));
        reader.setValue(otherContent);

        Reader clone1 = cloner.giveAClone();
        // The original Reader can be returned with this implementation.
        // Check it actually is.
        assertThat("returns original reader", clone1, is((Reader) reader));
        assertThat("has the new content", ReaderContent.readWhole(clone1), equalTo(otherContent)); // alas, but desirable performance-wise
        assertThat("empty after reading", clone1.read(), equalTo(-1));

        Reader clone2 = cloner.giveAClone();
        assertThat("do not return the previous clone", clone2, not(is(clone1)));
        String actualContent = ReaderContent.readWhole(clone2);
        assertThat("same content as before", actualContent, equalTo(content));
        assertThat("empty after reading", clone2.read(), equalTo(-1));
    }

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
