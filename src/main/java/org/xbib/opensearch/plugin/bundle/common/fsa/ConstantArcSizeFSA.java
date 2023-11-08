package org.xbib.opensearch.plugin.bundle.common.fsa;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * An FSA with constant-size arc representation produced directly by {@link FSABuilder}.
 *
 * @see FSABuilder
 */
public final class ConstantArcSizeFSA extends FSA {
    /**
     * Size of the target address field (constant for the builder).
     */
    static final  int TARGET_ADDRESS_SIZE = 4;

    /**
     * Size of the flags field (constant for the builder).
     */
    private static final int FLAGS_SIZE = 1;

    /**
     * Size of the label field (constant for the builder).
     */
    private static final int LABEL_SIZE = 1;

    /**
     * Size of a single arc structure.
     */
    static final int ARC_SIZE = FLAGS_SIZE + LABEL_SIZE + TARGET_ADDRESS_SIZE;

    /**
     * Offset of the flags field inside an arc.
     */
    static final int FLAGS_OFFSET = 0;

    /**
     * Offset of the label field inside an arc.
     */
    static final int LABEL_OFFSET = FLAGS_SIZE;

    /**
     * Offset of the address field inside an arc.
     */
    static final int ADDRESS_OFFSET = LABEL_OFFSET + LABEL_SIZE;
    /**
     * An arc flag indicating the target node of an arc corresponds to a final
     * state.
     */
    static final int BIT_ARC_FINAL = 1 << 1;
    /**
     * An arc flag indicating the arc is last within its state.
     */
    static final int BIT_ARC_LAST = 1;
    /**
     * A dummy address of the terminal state.
     */
    static final int TERMINAL_STATE = 0;
    /**
     * An epsilon state. The first and only arc of this state points either
     * to the root or to the terminal state, indicating an empty automaton.
     */
    private final int epsilon;

    /**
     * FSA data, serialized as a byte array.
     */
    private final byte[] data;

    /**
     * @param data FSA data. There must be no trailing bytes after the last state.
     */
    ConstantArcSizeFSA(byte[] data, int epsilon) {
        if (epsilon != 0) {
            throw new IllegalArgumentException("Epsilon is not zero?");
        }
        this.epsilon = epsilon;
        this.data = data;
    }

    @Override
    public int getRootNode() {
        return getEndNode(getFirstArc(epsilon));
    }

    @Override
    public int getFirstArc(int node) {
        return node;
    }

    @Override
    public int getArc(int node, byte label) {
        for (int arc = getFirstArc(node); arc != 0; arc = getNextArc(arc)) {
            if (getArcLabel(arc) == label) {
                return arc;
            }
        }
        return 0;
    }

    @Override
    public int getNextArc(int arc) {
        if (isArcLast(arc)) {
            return 0;
        }
        return arc + ARC_SIZE;
    }

    @Override
    public byte getArcLabel(int arc) {
        return data[arc + LABEL_OFFSET];
    }

    /**
     * Fills the target state address of an arc.
     * @param a arc
     */
    private int getArcTarget(int a) {
        int arc =a;
        arc += ADDRESS_OFFSET;
        return (data[arc]) << 24 |
                (data[arc + 1] & 0xff) << 16 |
                (data[arc + 2] & 0xff) << 8 |
                (data[arc + 3] & 0xff);
    }

    @Override
    public boolean isArcFinal(int arc) {
        return (data[arc + FLAGS_OFFSET] & BIT_ARC_FINAL) != 0;
    }

    @Override
    public boolean isArcTerminal(int arc) {
        return getArcTarget(arc) == 0;
    }

    private boolean isArcLast(int arc) {
        return (data[arc + FLAGS_OFFSET] & BIT_ARC_LAST) != 0;
    }

    @Override
    public int getEndNode(int arc) {
        return getArcTarget(arc);
    }

    @Override
    public Set<FSAFlags> getFlags() {
        return Collections.emptySet();
    }

    @Override
    public void write(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(data.length);
        outputStream.writeInt(epsilon);
        outputStream.write(data);
        outputStream.close();
    }
}
