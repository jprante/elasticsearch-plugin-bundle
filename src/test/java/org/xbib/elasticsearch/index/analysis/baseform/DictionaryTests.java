package org.xbib.elasticsearch.index.analysis.baseform;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.common.fsa.Dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.CharacterCodingException;

public class DictionaryTests extends Assert {

    @Test
    public void verifyDE() throws IOException {
        Dictionary dictionary = new Dictionary();
        InputStreamReader reader = new InputStreamReader(getClass().getResource("/baseform/de-lemma-utf8.txt").openStream(), "UTF-8");
        dictionary.load(reader);
        reader.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResource("/baseform/de-lemma-utf8.txt").openStream(), "UTF-8"));
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
            return false;
        }
        return true;
    }
}
