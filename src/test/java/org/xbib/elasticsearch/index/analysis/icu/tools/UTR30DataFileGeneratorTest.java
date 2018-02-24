package org.xbib.elasticsearch.index.analysis.icu.tools;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import org.junit.Test;

/**
 * UTR-30 data file generator test.
 */
public class UTR30DataFileGeneratorTest {

    @SuppressForbidden(value = "execute this test to download utr30 files")
    @Test
    public void generateUTR30() throws Exception {
        UTR30DataFileGenerator utr30DataFileGenerator = new UTR30DataFileGenerator();
        utr30DataFileGenerator.execute("release-60-2", "build/");
    }
}
