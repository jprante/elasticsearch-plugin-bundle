package org.xbib.opensearch.plugin.bundle.test.index.analysis.baseform;

import org.apache.lucene.util.SuppressForbidden;
import org.junit.Ignore;
import org.xbib.opensearch.plugin.bundle.common.fsa.Dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

/**
 * Dictionary tests.
 */
@Ignore
public class TestDictionary {

    @SuppressForbidden(reason = "accessing local resources from classpath")
    public void testVerifyDE() throws IOException {
        Dictionary dictionary = new Dictionary();
        InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("de-lemma-utf8.txt"),
                StandardCharsets.UTF_8);
        dictionary.loadLines(reader);
        reader.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("de-lemma-utf8.txt"),
                StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) {
                if (!check(line, dictionary)) {
                    break;
                }
            }
        }
        br.close();
    }

    private boolean check(String line, Dictionary dictionary) throws CharacterCodingException {
        int pos = line.indexOf("\t");
        String word = pos > 0 ? line.substring(0, pos) : line;
        try {
            CharSequence baseform = dictionary.lookup(word);
        } catch (StackOverflowError e) {
            // if stack overflow error occurs, we have faulty entries
            return false;
        }
        return true;
    }
}
