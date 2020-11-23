package org.xbib.elasticsearch.plugin.bundle.common.decompound.fst;

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
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This is a copy of org.apache.lucene.analysis.de.compounds.GermanCompoundSplitter from
 * https://github.com/dweiss/compound-splitter
 */
public class FstDecompounder {

    private static final List<String> morphemes = Arrays.asList("e", "es", "en", "er", "n", "ens", "ns", "s");

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

    public FstDecompounder(InputStream inputStream, List<String> glue) throws IOException {
        try {
            InputStreamDataInput input = new InputStreamDataInput(inputStream);
            this.surfaceForms = new FST<>(input, input, NoOutputs.getSingleton());
            // set up glue morphemes
            this.glueMorphemes = createGlueMorphemes(glue != null && glue.size() > 0 ? glue :morphemes);
        } finally {
            inputStream.close();
        }
    }

    private FST<Object> createGlueMorphemes(List<String> glue) throws IOException {
        for (int i = 0; i < glue.size(); i++) {
            glue.set(i, new StringBuilder(glue.get(i)).reverse().toString());
        }
        Collections.sort(glue);
        final Builder<Object> builder = new Builder<>(INPUT_TYPE.BYTE4, NoOutputs.getSingleton());
        final Object nothing = NoOutputs.getSingleton().getNoOutput();
        IntsRefBuilder intsBuilder = new IntsRefBuilder();
        for (String morpheme : glue) {
            fromUTF16ToUTF32(morpheme, intsBuilder);
            builder.add(intsBuilder.get(), nothing);
        }
        return builder.finish();
    }

    public List<String> decompound(String word) {
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
    public CharSequence split(CharSequence word) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(word);
            builder.reverse();
            for (int i = builder.length(); --i > 0; ) {
                // see https://issues.apache.org/jira/browse/COLLECTIONS-294
                builder.setCharAt(i, Character.toLowerCase(Character.toUpperCase(builder.charAt(i))));
            }
            IntsRefBuilder utf32Builder = new IntsRefBuilder();
            IntsRef utf32 = fromUTF16ToUTF32(builder, utf32Builder).get();
            /*
             * This array stores the minimum number of decomposition words during traversals to
             * avoid splitting a larger word into smaller chunks.
             */
            IntsRefBuilder maxPathsBuilder = new IntsRefBuilder();
            maxPathsBuilder.grow(utf32.length + 1);
            Arrays.fill(maxPathsBuilder.ints(), 0, utf32.length + 1, Integer.MAX_VALUE);
            builder.setLength(0);
            Deque<Chunk> chunks = new LinkedList<>();
            matchWord(utf32, utf32.offset, builder, maxPathsBuilder, chunks);
            return builder.length() == 0 ? null : builder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Consume a word, then recurse into glue morphemes/ further words.
     */
    private void matchWord(IntsRef utf32, int offset, StringBuilder builder, IntsRefBuilder maxPathsBuilder,
                           Deque<Chunk> chunks) throws IOException {
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
                wordsFromHere.add(new Chunk(offset, i + 1, ChunkType.WORD));
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
                // add match to the builder
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
                        builder.append(new StringBuilder(UnicodeUtil.newString(utf32.ints, chunk.start,
                                chunk.end - chunk.start)).reverse());
                    }
                }
            } else {
                matchWord(utf32, ch.end, builder, maxPathsBuilder, chunks);
                matchGlueMorpheme(utf32, ch.end, builder, maxPathsBuilder, chunks);
            }
            chunks.removeLast();
        }
    }

    /**
     * Consume a maximal glue morpheme, if any, and consume the next word.
     */
    private void matchGlueMorpheme(IntsRef utf32, final int offset, StringBuilder builder,
                                   IntsRefBuilder maxPathsBuilder,
                                   Deque<Chunk> chunks) throws IOException {
        FST.Arc<Object> arc = glueMorphemes.getFirstArc(new FST.Arc<>());
        BytesReader br = glueMorphemes.getBytesReader();
        for (int i = offset; i < utf32.length; i++) {
            int chr = utf32.ints[i];
            arc = glueMorphemes.findTargetArc(chr, arc, arc, br);
            if (arc == null) {
                break;
            }
            if (arc.isFinal()) {
                chunks.addLast(new Chunk(offset, i + 1, ChunkType.GLUE_MORPHEME));
                if (i + 1 < utf32.offset + utf32.length) {
                    matchWord(utf32, i + 1, builder, maxPathsBuilder, chunks);
                }
                chunks.removeLast();
            }
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

    /**
     * Category for a given chunk of a compound.
     */
     enum ChunkType {
        GLUE_MORPHEME, WORD
    }

    /**
     * A slice of a compound word.
     */
    private final class Chunk {
        final int start;
        final int end;
        final ChunkType type;

        Chunk(int start, int end, ChunkType type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }
    }
}
