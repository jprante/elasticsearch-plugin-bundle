package org.xbib.elasticsearch.common.fsa;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Dictionary.
 */
public class Dictionary {

    private FSA fsa;

    private FSATraversal matcher;

    /**
     * Format of file: sourceform "\t" targetform1 "\t" targetform2 ...
     * @param reader the reader
     * @return the dictionary
     * @throws IOException if dictionary load fails
     */
    public Dictionary loadLines(Reader reader) throws IOException {
        List<byte[]> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line.replace('\t', '+').getBytes(StandardCharsets.UTF_8));
            }
        }
        lines.sort(FSABuilder.LEXICAL_ORDERING);
        FSABuilder builder = new FSABuilder();
        for (byte[] b : lines) {
            builder.add(b, 0, b.length);
        }
        this.fsa = builder.complete();
        this.matcher = new FSATraversal(fsa);
        return this;
    }

    /**
     * @param reader the reader
     * @return the dictionary
     * @throws IOException if dictionary load fails
     */
    public Dictionary loadLinesReverse(Reader reader) throws IOException {
        List<byte[]> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                List<String> s = Arrays.asList(line.split("\t"));
                Collections.reverse(s);
                lines.add(String.join("+", s).getBytes(StandardCharsets.UTF_8));
            }
        }
        lines.sort(FSABuilder.LEXICAL_ORDERING);
        FSABuilder builder = new FSABuilder();
        for (byte[] b : lines) {
            builder.add(b, 0, b.length);
        }
        this.fsa = builder.complete();
        this.matcher = new FSATraversal(fsa);
        return this;
    }

    public Dictionary loadFSA(InputStream inputStream) throws IOException {
        FSABuilder builder = new FSABuilder();
        this.fsa = builder.load(new DataInputStream(inputStream));
        this.matcher = new FSATraversal(fsa);
        return this;
    }

    public CharSequence lookup(CharSequence prefix) throws CharacterCodingException {
        if (prefix == null || prefix.length() == 0) {
            return prefix;
        }
        return lookup(StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(prefix)), prefix.toString());
    }

    public CharSequence lookup(ByteBuffer buf, String request) {
        return lookup(buf, request, 0);
    }

    public CharSequence lookup(ByteBuffer buf, String request, int level) {
        if (level > 3) {
            return request;
        }
        MatchResult match = matcher.match(buf.array(), buf.position(), buf.remaining(), fsa.getRootNode());
        switch (match.getKind()) {
            case MatchResult.SEQUENCE_IS_A_PREFIX:
                final int arc = fsa.getArc(match.getNode(), (byte) '+');
                if (arc != 0 && !fsa.isArcFinal(arc)) {
                    FSAFinalStatesIterator finalStatesIterator = new FSAFinalStatesIterator(fsa, fsa.getRootNode());
                    finalStatesIterator.restartFrom(fsa.getEndNode(arc));
                    if (finalStatesIterator.hasNext()) {
                        ByteBuffer buffer = finalStatesIterator.next();
                        String s = new String(buffer.array(), buffer.position(), buffer.remaining(), StandardCharsets.UTF_8);
                        return s.isEmpty() || s.equals(request) ? s : lookup(buffer, s, level + 1);
                    }
                }
                break;
            case MatchResult.EXACT_MATCH:
            case MatchResult.NO_MATCH:
            default:
                break;
        }
        return request;
    }

    public FSA fsa() {
        return fsa;
    }
}
