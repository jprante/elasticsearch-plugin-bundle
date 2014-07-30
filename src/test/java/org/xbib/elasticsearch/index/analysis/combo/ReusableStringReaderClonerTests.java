package org.xbib.elasticsearch.index.analysis.combo;

import org.junit.Test;

import java.io.Reader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class ReusableStringReaderClonerTests {

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

}
