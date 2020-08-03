package org.xbib.elasticsearch.plugin.bundle.common.fst;

import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.NoOutputs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;


/**
 * Compile an FSA from an UTF-8 text file (must be properly sorted).
 */
public class FstCompiler {

    private static final Pattern pattern = Pattern.compile("\\s+");

    /**
     *
     * @param inputStream the input stream
     * @param outputStream the output stream
     * @throws IOException if compilation fails
     */
    public void compile(InputStream inputStream, OutputStream outputStream) throws IOException {
        final HashSet<BytesRef> words = new HashSet<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        String last = null;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            if (line.indexOf('#') >= 0) {
                continue;
            }
            line = pattern.split(line)[0].trim();
            line = line.toLowerCase(Locale.ROOT);
            if (line.equals(last)) {
                continue;
            }
            last = line;
            /*
             * Add the word to the hash set in left-to-right characters order and reversed
             * for easier matching later on.
             */
            stringBuilder.setLength(0);
            stringBuilder.append(line);
            final int len = stringBuilder.length();
            stringBuilder.append('>');
            words.add(new BytesRef(stringBuilder));
            stringBuilder.setLength(len);
            stringBuilder.reverse().append('<');
            words.add(new BytesRef(stringBuilder));
        }
        reader.close();
        final BytesRef [] all = new BytesRef[words.size()];
        words.toArray(all);
        Arrays.sort(all, BytesRef::compareTo);
        final Object nothing = NoOutputs.getSingleton().getNoOutput();
        final Builder<Object> builder = new Builder<>(INPUT_TYPE.BYTE4, NoOutputs.getSingleton());
        final IntsRefBuilder intsRef = new IntsRefBuilder();
        for (BytesRef bytesRef : all) {
            intsRef.clear();
            intsRef.copyUTF8Bytes(bytesRef);
            builder.add(intsRef.get(), nothing);
        }
        final FST<Object> fst = builder.finish();
        try (OutputStreamDataOutput out = new OutputStreamDataOutput(outputStream)) {
            fst.save(out);
        }
    }
}
