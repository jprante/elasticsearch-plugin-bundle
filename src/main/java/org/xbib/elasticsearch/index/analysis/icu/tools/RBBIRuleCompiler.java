package org.xbib.elasticsearch.index.analysis.icu.tools;

import com.ibm.icu.text.RuleBasedBreakIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility to convert RuleBasedBreakIterator (.rbbi) files into binary compiled form (.brk).
 */
public class RBBIRuleCompiler {

    private static final Logger logger = LogManager.getLogger(RBBIRuleCompiler.class.getName());

    public void compile(Path inputPath, Path outputPath) throws IOException {
        String rules = getRules(inputPath);
        try (OutputStream os = Files.newOutputStream(outputPath)) {
            new RuleBasedBreakIterator(rules);
            RuleBasedBreakIterator.compileRules(rules, os);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getRules(Path rulePath) throws IOException {
        StringBuilder rules = new StringBuilder();
        try (InputStream inputStream = Files.newInputStream(rulePath);
                BufferedReader cin =
                     new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = cin.readLine()) != null) {
                if (!line.startsWith("#")) {
                    rules.append(line);
                    rules.append('\n');
                }
            }
        }
        return rules.toString();
    }
}
