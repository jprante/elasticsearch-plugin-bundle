package org.xbib.elasticsearch.common.fsa;

import org.junit.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 */
public class FsaCompilerTool {

    public void buildLemmatizeFSA() throws IOException {
        String[] langs = {
                "ast", "bg", "cs", "cy", "de", "en", "es", "et", "fa", "fr", "ga", "gd", "gl", "gv",
                "hu", "it", "pl", "pt", "ro", "sk", "sl", "sv", "uk"
        };
        for (String lang : langs) {
            Dictionary dictionary = new Dictionary();
            String resource = "/lemmatize/lemmatization-" + lang + ".txt";
            Path path = Paths.get("build/lemmatization-" + lang + ".fsa");
            try (DataOutputStream dataOutputStream = new DataOutputStream(Files.newOutputStream(path));
                 Reader reader = new InputStreamReader(getClass().getResourceAsStream(resource),
                    StandardCharsets.UTF_8)) {
                dictionary.loadLinesReverse(reader);
                dictionary.fsa().write(dataOutputStream);
            }
        }
    }
}
