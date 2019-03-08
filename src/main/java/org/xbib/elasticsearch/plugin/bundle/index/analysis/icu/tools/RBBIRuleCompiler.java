package org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.tools;

import com.ibm.icu.text.RuleBasedBreakIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility to convert RuleBasedBreakIterator (.rbbi) files into binary compiled form (.brk).
 */
public class RBBIRuleCompiler {

    private static final Logger logger = LogManager.getLogger(RBBIRuleCompiler.class.getName());

    public void compile(Path inputPath, Path outputPath) throws IOException {
        compile(Files.newInputStream(inputPath), Files.newOutputStream(outputPath));
    }

    public void compile(InputStream inputStream, OutputStream outputStream) throws IOException {
        String rules = getRules(inputStream);
        try (OutputStream os = outputStream) {
            new RuleBasedBreakIterator(rules);
            RuleBasedBreakIterator.compileRules(rules, os);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getRules(InputStream inputStream) throws IOException {
        StringBuilder rules = new StringBuilder();
        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    rules.append(line);
                    rules.append('\n');
                }
            }
        }
        return rules.toString();
    }
}
