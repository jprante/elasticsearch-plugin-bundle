package org.xbib.elasticsearch.common.fsa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Dictionary {

    private FSA fsa;

    private FSATraversal matcher;

    public Dictionary load(String language) throws IOException {
        return load(new InputStreamReader(this.getClass()
                .getResourceAsStream(language + "-lemma-utf8.txt"), StandardCharsets.UTF_8));
    }

    public Dictionary load(Reader in) throws IOException {
        BufferedReader reader = new BufferedReader(in);
        List<byte[]> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line.replace('\t', '+').getBytes(StandardCharsets.UTF_8));
        }
        reader.close();
        lines.sort(FSABuilder.LEXICAL_ORDERING);
        FSABuilder builder = new FSABuilder();
        for (byte[] b : lines) {
            builder.add(b, 0, b.length);
        }
        this.fsa = builder.complete();
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
}
