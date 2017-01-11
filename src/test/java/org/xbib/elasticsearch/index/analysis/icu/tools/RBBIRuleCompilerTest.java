package org.xbib.elasticsearch.index.analysis.icu.tools;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 */
public class RBBIRuleCompilerTest {

    @Test
    public void testRBBICompile() throws IOException {
        RBBIRuleCompiler rbbiRuleCompiler = new RBBIRuleCompiler();
        rbbiRuleCompiler.compile(Paths.get("src/main/resources/icu/KeywordTokenizer.rbbi"),
                Paths.get("build/KeywordTokenizer.brk"));
    }
}
