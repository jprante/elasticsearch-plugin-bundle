package org.xbib.elasticsearch.index.analysis.icu.tools;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * RBBI rule compiler test.
 */
public class RBBIRuleCompilerTest {

    @Test
    public void testRBBICompile() throws IOException {
        RBBIRuleCompiler rbbiRuleCompiler = new RBBIRuleCompiler();
        String[] rbbis = {
                "/icu/Default.rbbi",
                "/icu/KeywordTokenizer.rbbi",
                "/icu/Latin-break-only-on-whitespace.rbbi",
                "/icu/Latin-dont-break-on-hyphens.rbbi",
                "/icu/MyanmarSyllable.rbbi"
        };
        for (String rbbi : rbbis) {
            InputStream inputStream = getClass().getResourceAsStream(rbbi);
            int pos1 = rbbi.lastIndexOf("/");
            int pos2 = rbbi.lastIndexOf(".rbbi");
            String basename = rbbi.substring(pos1, pos2);
            OutputStream outputStream = Files.newOutputStream(Paths.get("build/" + basename + ".brk"));
            rbbiRuleCompiler.compile(inputStream, outputStream);
        }
    }
}
