package org.xbib.elasticsearch.common.fst;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;

/**
 */
public class FstCompilerTool {

    @Test
    @Ignore
    public void buildFstDecompound() throws IOException {
        FstCompiler fstCompiler = new FstCompiler();
        Path path = Paths.get("build/words.fst");
        try (InputStream inputStream = new GZIPInputStream(getClass().getResource("morphy.txt.gz").openStream());
             OutputStream outputStream = Files.newOutputStream(path)) {
            fstCompiler.compile(inputStream, outputStream);
        }
    }
}
