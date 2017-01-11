package org.xbib.elasticsearch.index.analysis.icu.tools;

import org.junit.Test;

import java.io.IOException;

/**
 */
public class UTR30DataFileGeneratorTest {

    @Test
    public void generate() throws IOException {
        UTR30DataFileGenerator utr30DataFileGenerator = new UTR30DataFileGenerator();
        utr30DataFileGenerator.execute();
    }
}
