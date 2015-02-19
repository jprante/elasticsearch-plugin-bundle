package org.xbib.elasticsearch.index.analysis.baseform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dictionary {

    private final Charset UTF8 = Charset.forName("UTF-8");

    private FSA fsa;

    private FSATraversal matcher;

    public Dictionary load(String language) throws IOException {
        return load(new InputStreamReader(this.getClass().getResourceAsStream(language + "-lemma-utf8.txt"), UTF8));
    }

    public Dictionary load(Reader in) throws IOException {
        BufferedReader reader = new BufferedReader(in);
        List<byte[]> lines = new ArrayList<byte[]>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line.replace('\t', '+').getBytes(UTF8));
        }
        reader.close();
        Collections.sort(lines, FSABuilder.LEXICAL_ORDERING);
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
        return lookup(UTF8.newEncoder().encode(CharBuffer.wrap(prefix)), prefix.toString());
    }

    public CharSequence lookup(ByteBuffer buf, String request) {
        return lookup(buf, request, 0);
    }

    public CharSequence lookup(ByteBuffer buf, String request, int level) {
        if (level > 3) {
            return request;
        }
        MatchResult match = matcher.match(buf.array(), buf.position(), buf.remaining(), fsa.getRootNode());
        switch (match.kind) {
            case MatchResult.SEQUENCE_IS_A_PREFIX: {
                final int arc = fsa.getArc(match.node, (byte) '+');
                if (arc != 0 && !fsa.isArcFinal(arc)) {
                    FSAFinalStatesIterator finalStatesIterator = new FSAFinalStatesIterator(fsa, fsa.getRootNode());
                    finalStatesIterator.restartFrom(fsa.getEndNode(arc));
                    if (finalStatesIterator.hasNext()) {
                        buf = finalStatesIterator.next();
                        String s = new String(buf.array(), buf.position(), buf.remaining(), UTF8);
                        return s.isEmpty() || s.equals(request) ? s : lookup(buf, s, level + 1);
                    }
                }
                break;
            }
            case MatchResult.EXACT_MATCH: {
                break;
            }
            case MatchResult.NO_MATCH: {
                break;
            }
        }
        return request;
    }

}
