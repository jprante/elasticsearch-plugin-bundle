package org.xbib.elasticsearch.common.expansion.fst;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.BytesReader;
import org.apache.lucene.util.fst.NoOutputs;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static org.apache.lucene.util.UnicodeUtil.newString;

/**
 *
 */
public class FstExpander {

    private final FST<Object> fst;

    public FstExpander(InputStream inputStream) throws IOException {
        try {
            this.fst = new FST<>(new InputStreamDataInput(inputStream), NoOutputs.getSingleton());
        } finally {
            inputStream.close();
        }
    }

    public List<String> expand(String word) {
        CharSequence chars = split(word);
        if (chars != null) {
            return Arrays.asList(chars.toString().split(",'"));
        }
        return Collections.singletonList(word);
    }

    /**
     * Splits the input sequence of characters into separate words if this sequence is
     * potentially a compound word.
     *
     * @param word The word to be split.
     * @return Returns <code>null</code> if this word is not recognized at all. Returns a
     * character sequence with '.'-delimited compound chunks (if ambiguous
     * interpretations are possible, they are separated by a ',' character). The
     * returned buffer will change with each call to <code>split</code> so copy the
     * content if needed.
     */
    private CharSequence split(CharSequence word) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(word);
            builder.reverse();
            for (int i = builder.length(); --i > 0; ) {
                builder.setCharAt(i, Character.toLowerCase(builder.charAt(i)));
            }
            /*
             * Full unicode points representation of the input compound.
             */
            IntsRefBuilder utf32Builder = new IntsRefBuilder();
            IntsRef utf32 = fromUTF16ToUTF32(builder, utf32Builder).get();
            builder.setLength(0);
            Deque<Chunk> chunks = new ArrayDeque<>();
            matchWord(utf32, utf32.offset, builder, chunks);
            return builder.length() == 0 ? null : builder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void matchWord(IntsRef utf32, int offset, StringBuilder builder,
                           Deque<Chunk> chunks) throws IOException {
        FST.Arc<Object> arc = fst.getFirstArc(new FST.Arc<>());
        FST.Arc<Object> scratch = new FST.Arc<>();
        List<Chunk> wordsFromHere = new ArrayList<>();
        BytesReader br = fst.getBytesReader();
        for (int i = offset; i < utf32.length; i++) {
            int chr = utf32.ints[i];
            arc = fst.findTargetArc(chr, arc, arc, br);
            if (arc == null) {
                break;
            }
            if (fst.findTargetArc('<', arc, scratch, br) != null) {
                wordsFromHere.add(new Chunk(offset, i + 1));
            }
        }
        /*
         * This array stores the minimum number of decomposition words during traversals to
         * avoid splitting a larger word into smaller chunks.
         */
        IntsRefBuilder maxPathsBuilder = new IntsRefBuilder();
        maxPathsBuilder.grow(utf32.length + 1);
        Arrays.fill(maxPathsBuilder.ints(), 0, utf32.length + 1, Integer.MAX_VALUE);
        int[] maxPaths = maxPathsBuilder.ints();
        for (int j = wordsFromHere.size(); --j >= 0; ) {
            final Chunk ch = wordsFromHere.get(j);
            if (chunks.size() + 1 > maxPaths[ch.end]) {
                continue;
            }
            maxPaths[ch.end] = chunks.size() + 1;
            chunks.addLast(ch);
            if (ch.end == utf32.offset + utf32.length) {
                // add match to the builder
                if (builder.length() > 0) {
                    builder.append(",");
                }
                boolean first = true;
                Iterator<Chunk> i = chunks.descendingIterator();
                while (i.hasNext()) {
                    Chunk chunk = i.next();
                    if (!first) {
                        builder.append('.');
                    }
                    first = false;
                    String s = new StringBuilder(newString(utf32.ints, chunk.start,
                            chunk.end - chunk.start)).reverse().toString();
                    builder.append(s);
                }
            } else {
                matchWord(utf32, ch.end, builder, chunks);
            }
            chunks.removeLast();
        }
    }

    /**
     * Convert a character sequence into full unicode codepoints.
     */
    private static IntsRefBuilder fromUTF16ToUTF32(CharSequence s, IntsRefBuilder builder) {
        builder.clear();
        for (int charIdx = 0, charLimit = s.length(); charIdx < charLimit; ) {
            final int utf32 = Character.codePointAt(s, charIdx);
            builder.append(utf32);
            charIdx += Character.charCount(utf32);
        }
        return builder;
    }

    private  class Chunk {
        final int start;
        final int end;

        Chunk(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
