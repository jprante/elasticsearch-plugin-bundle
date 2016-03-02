package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.lucene.store.OutputStreamDataOutput;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.NoOutputs;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class CreateFST {

    public void createMorphy() throws IOException {
        final HashSet<BytesRef> words = new HashSet<>();
        String[] inputs = new String[]{
                "morphy.txt.gz",
                "morphy-unknown.txt.gz"
        };
        for (String input : inputs) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new GZIPInputStream(getClass().getResourceAsStream(input)), "UTF-8"));
            Pattern pattern = Pattern.compile("\\s+");
            String line;
            String last = null;
            StringBuilder buffer = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (line.indexOf('#') >= 0) {
                    continue;
                }
                line = pattern.split(line)[0].trim();
                line = line.toLowerCase();
                if (line.equals(last)) {
                    continue;
                }
                last = line;
                buffer.setLength(0);
                buffer.append(line);
                final int len = buffer.length();
                buffer.append('>');
                words.add(new BytesRef(buffer));
                buffer.setLength(len);
                buffer.reverse().append('<');
                words.add(new BytesRef(buffer));
            }
            reader.close();
        }
        final BytesRef [] all = new BytesRef [words.size()];
        words.toArray(all);
        Arrays.sort(all, BytesRef.getUTF8SortedAsUnicodeComparator());
        final Object nothing = NoOutputs.getSingleton().getNoOutput();
        final Builder<Object> builder = new Builder<>(INPUT_TYPE.BYTE4, NoOutputs.getSingleton());
        final IntsRefBuilder intsRef = new IntsRefBuilder();
        for (BytesRef br : all) {
            intsRef.clear();
            intsRef.copyUTF8Bytes(br);
            builder.add(intsRef.get(), nothing);
        }
        final FST<Object> fst = builder.finish();
        File file = File.createTempFile("words.", ".fst");
        file.deleteOnExit();
        final OutputStreamDataOutput out = new OutputStreamDataOutput(new FileOutputStream(file));
        fst.save(out);
        out.close();
    }
}
