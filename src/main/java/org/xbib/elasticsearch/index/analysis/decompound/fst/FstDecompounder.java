package org.xbib.elasticsearch.index.analysis.decompound.fst;

import org.apache.lucene.store.InputStreamDataInput;
import org.apache.lucene.util.IntsRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.UnicodeUtil;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.FST.BytesReader;
import org.apache.lucene.util.fst.FST.INPUT_TYPE;
import org.apache.lucene.util.fst.NoOutputs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class FstDecompounder {

    /**
     * A static FSA with inflected and base surface forms.
     *
     * @see "http://www.wolfganglezius.de/doku.php?id=cl:surfaceForms"
     */
    private final FST<Object> surfaceForms;
    /**
     * A static FSA with glue glueMorphemes. This could be merged into a single FSA
     * together with {@link #surfaceForms}, but I leave it separate for now.
     */
    private final FST<Object> glueMorphemes;
    /**
     * Reusable array of decomposition chunks.
     */
    private final ArrayDeque<Chunk> chunks = new ArrayDeque<>();
    /**
     * String builder for the result of {@link #split(CharSequence)}.
     */
    private final StringBuilder builder = new StringBuilder();
    /**
     * Full unicode points representation of the input compound.
     */
    private IntsRef utf32;
    private IntsRefBuilder utf32Builder = new IntsRefBuilder();
    /**
     * This array stores the minimum number of decomposition words during traversals to
     * avoid splitting a larger word into smaller chunks.
     */
    private IntsRefBuilder maxPathsBuilder = new IntsRefBuilder();
    /**
     * A decomposition listener accepts potential decompositions of a word.
     */
    private DecompositionListener listener;

    public FstDecompounder(InputStream is) throws IOException {
        surfaceForms = new FST<>(new InputStreamDataInput(is), NoOutputs.getSingleton());
        is.close();
        String[] morphemes =
                {
                        "e", "es", "en", "er", "n", "ens", "ns", "s"
                };
        for (int i = 0; i < morphemes.length; i++) {
            morphemes[i] = new StringBuilder(morphemes[i]).reverse().toString();
        }
        Arrays.sort(morphemes);
        final Builder<Object> builder = new Builder<>(INPUT_TYPE.BYTE4, NoOutputs.getSingleton());
        final Object nothing = NoOutputs.getSingleton().getNoOutput();
        IntsRefBuilder intsBuilder = new IntsRefBuilder();
        for (String morpheme : morphemes) {
            UTF16ToUTF32(morpheme, intsBuilder);
            builder.add(intsBuilder.get(), nothing);
        }
        glueMorphemes = builder.finish();
    }

    /**
     * Convert a character sequence <code>s</code> into full unicode codepoints.
     */
    private static IntsRefBuilder UTF16ToUTF32(CharSequence s, IntsRefBuilder builder) {
        builder.clear();
        for (int charIdx = 0, charLimit = s.length(); charIdx < charLimit; ) {
            final int utf32 = Character.codePointAt(s, charIdx);
            builder.append(utf32);
            charIdx += Character.charCount(utf32);
        }
        return builder;
    }

    public List<String> decompound(String word) {
        List<String> list = new ArrayList<>();
        CharSequence chars = split(word);
        if (chars != null) {
            String s = chars.toString();
            String[] alts = s.split(",'");
            for (String alt : alts) {
                Collections.addAll(list, alt.split("\\."));
            }
        }
        return list;
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
    public CharSequence split(CharSequence word) {
        try {
            this.builder.setLength(0);
            this.builder.append(word);
            this.builder.reverse();
            for (int i = builder.length(); --i > 0; ) {
                builder.setCharAt(i, Character.toLowerCase(builder.charAt(i)));
            }
            this.utf32 = UTF16ToUTF32(builder, utf32Builder).get();
            builder.setLength(0);
            this.listener = new DecompositionListener() {
                public void decomposition(IntsRef utf32, ArrayDeque<Chunk> chunks) {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    boolean first = true;
                    Iterator<Chunk> i = chunks.descendingIterator();
                    while (i.hasNext()) {
                        Chunk chunk = i.next();
                        if (chunk.type == ChunkType.WORD) {
                            if (!first) {
                                builder.append('.');
                            }
                            first = false;
                            builder.append(chunk.toString());
                        }
                    }
                }
            };
            maxPathsBuilder.clear();
            maxPathsBuilder.grow(utf32.length + 1);
            Arrays.fill(maxPathsBuilder.ints(), 0, utf32.length + 1, Integer.MAX_VALUE);
            matchWord(utf32, utf32.offset);
            return builder.length() == 0 ? null : builder;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Consume a word, then recurse into glue morphemes/ further words.
     */
    private void matchWord(IntsRef utf32, int offset) throws IOException {
        FST.Arc<Object> arc = surfaceForms.getFirstArc(new FST.Arc<>());
        FST.Arc<Object> scratch = new FST.Arc<>();
        List<Chunk> wordsFromHere = new ArrayList<>();
        BytesReader br = surfaceForms.getBytesReader();
        for (int i = offset; i < utf32.length; i++) {
            int chr = utf32.ints[i];
            arc = surfaceForms.findTargetArc(chr, arc, arc, br);
            if (arc == null) {
                break;
            }
            if (surfaceForms.findTargetArc('<', arc, scratch, br) != null) {
                Chunk ch = new Chunk(offset, i + 1, ChunkType.WORD);
                wordsFromHere.add(ch);
            }
        }
        int[] maxPaths = maxPathsBuilder.ints();
        for (int j = wordsFromHere.size(); --j >= 0; ) {
            final Chunk ch = wordsFromHere.get(j);
            if (chunks.size() + 1 > maxPaths[ch.end]) {
                continue;
            }
            maxPaths[ch.end] = chunks.size() + 1;
            chunks.addLast(ch);
            if (ch.end == utf32.offset + utf32.length) {
                listener.decomposition(this.utf32, chunks);
            } else {
                matchWord(utf32, ch.end);
                matchGlueMorpheme(utf32, ch.end);
            }
            chunks.removeLast();
        }
    }

    /**
     * Consume a maximal glue morpheme, if any, and consume the next word.
     */
    private void matchGlueMorpheme(IntsRef utf32, final int offset) throws IOException {
        FST.Arc<Object> arc = glueMorphemes.getFirstArc(new FST.Arc<>());
        BytesReader br = glueMorphemes.getBytesReader();
        for (int i = offset; i < utf32.length; i++) {
            int chr = utf32.ints[i];
            arc = glueMorphemes.findTargetArc(chr, arc, arc, br);
            if (arc == null) {
                break;
            }
            if (arc.isFinal()) {
                Chunk ch = new Chunk(offset, i + 1, ChunkType.GLUE_MORPHEME);
                chunks.addLast(ch);
                if (i + 1 < utf32.offset + utf32.length) {
                    matchWord(utf32, i + 1);
                }
                chunks.removeLast();
            }
        }
    }

    /**
     * Category for a given chunk of a compound.
     */
    public enum ChunkType {
        GLUE_MORPHEME, WORD,
    }

    /**
     * A decomposition listener accepts potential decompositions of a word.
     */
    interface DecompositionListener {
        /**
         * @param utf32  Full unicode points of the input sequence.
         * @param chunks Chunks with decomposed parts and matching regions.
         */
        void decomposition(IntsRef utf32, ArrayDeque<Chunk> chunks);
    }

    /**
     * A slice of a compound word.
     */
    final class Chunk {
        public final int start;
        public final int end;
        public final ChunkType type;

        Chunk(int start, int end, ChunkType type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder(UnicodeUtil.newString(utf32.ints, start, end - start)).reverse();
            if (type == ChunkType.GLUE_MORPHEME) {
                b.append("<G>");
            }
            return b.toString();
        }
    }
}
