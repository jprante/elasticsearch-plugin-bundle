package org.xbib.opensearch.plugin.bundle.common.fsa;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * This is an abstract class for handling finite state automata. These
 * automata are arc-based, a design described in Jan Daciuk's <i>Incremental
 * Construction of Finite-State Automata and Transducers, and Their Use in the
 * Natural Language Processing</i> (PhD thesis, Technical University of Gdansk).
 * Concrete subclasses (implementations) provide varying tradeoffs and features:
 * traversal speed vs. memory size, for example.
 *
 * @see FSABuilder
 */
public abstract class FSA implements Iterable<ByteBuffer> {
    /**
     * @return Returns the identifier of the root node of this automaton.
     * Returns 0 if the start node is also the end node (the automaton
     * is empty).
     */
    public abstract int getRootNode();

    /**
     * @param node node
     * @return Returns the identifier of the first arc leaving <code>node</code>
     * or 0 if the node has no outgoing arcs.
     */
    public abstract int getFirstArc(int node);

    /**
     * @param arc arc
     * @return Returns the identifier of the next arc after <code>arc</code> and
     * leaving <code>node</code>. Zero is returned if no more arcs are
     * available for the node.
     */
    public abstract int getNextArc(int arc);

    /**
     * @param node  node
     * @param label label
     * @return Returns the identifier of an arc leaving <code>node</code> and
     * labeled with <code>label</code>. An identifier equal to 0 means
     * the node has no outgoing arc labeled <code>label</code>.
     */
    public abstract int getArc(int node, byte label);

    /**
     * @param arc arc
     * @return the label associated with a given <code>arc</code>.
     */
    public abstract byte getArcLabel(int arc);

    /**
     * @param arc arc
     * @return <code>true</code> if the destination node at the end of this
     * <code>arc</code> corresponds to an input sequence created when building
     * this automaton.
     */
    public abstract boolean isArcFinal(int arc);

    /**
     * Returns <code>true</code> if this <code>arc</code> does not have a
     * terminating node (@link {@link #getEndNode(int)} will throw an
     * exception). Implies {@link #isArcFinal(int)}.
     *
     * @param arc arc
     * @return <code>true</code> if this <code>arc</code> does not have a
     * terminating node
     */
    public abstract boolean isArcTerminal(int arc);

    /**
     * Return the end node pointed to by a given <code>arc</code>. Terminal arcs
     * (those that point to a terminal state) have no end node representation
     * and throw a runtime exception.
     *
     * @param arc arc
     * @return int
     */
    public abstract int getEndNode(int arc);

    /**
     * Returns a set of flags for this FSA instance.
     *
     * @return set
     */
    public abstract Set<FSAFlags> getFlags();

    public abstract void write(DataOutputStream outputStream) throws IOException;

    /**
     * @param node node
     * @return Returns the number of sequences reachable from the given state if
     * the automaton was compiled with {@link FSAFlags#NUMBERS}. The size of
     * the right language of the state, in other words.
     * @throws UnsupportedOperationException If the automaton was not compiled with
     *                                       {@link FSAFlags#NUMBERS}. The value can then be computed by manual count
     *                                       of {@link #getSequences(int)}.
     */
    public int getRightLanguageCount(int node) {
        throw new UnsupportedOperationException("Automaton not compiled with " + FSAFlags.NUMBERS);
    }

    /**
     * Returns an iterator over all binary sequences starting at the given FSA
     * state (node) and ending in final nodes. This corresponds to a set of
     * suffixes of a given prefix from all sequences stored in the automaton.
     * The returned iterator is a {@link java.nio.ByteBuffer} whose contents changes on
     * each call to {@link java.util.Iterator#next()}. The keep the contents between calls
     * to {@link java.util.Iterator#next()}, one must copy the buffer to some other
     * location.
     * Important: it is guaranteed that the returned byte buffer is
     * backed by a byte array and that the content of the byte buffer starts at
     * the array's index 0.
     *
     * @param node node
     * @return byte buffer
     * @see Iterable
     */
    public Iterable<ByteBuffer> getSequences(final int node) {
        if (node == 0) {
            return Collections.emptyList();
        }
        return () -> new FSAFinalStatesIterator(FSA.this, node);
    }

    /**
     * An alias of calling {@link #iterator} directly ({@link FSA} is also
     * {@link Iterable}).
     *
     * @return iterable
     */
    public final Iterable<ByteBuffer> getSequences() {
        return getSequences(getRootNode());
    }

    /**
     * Returns an iterator over all binary sequences starting from the initial
     * FSA state (node) and ending in final nodes. The returned iterator is a
     * {@link java.nio.ByteBuffer} whose contents changes on each call to
     * {@link java.util.Iterator#next()}. The keep the contents between calls to
     * {@link java.util.Iterator#next()}, one must copy the buffer to some other location.
     * Important: It is guaranteed that the returned byte buffer is
     * backed by a byte array and that the content of the byte buffer starts at
     * the array's index 0.
     *
     * @return iterator
     */
    @Override
    public final Iterator<ByteBuffer> iterator() {
        return getSequences().iterator();
    }

    /**
     * Visit all states. The order of visiting is undefined. This method may be faster
     * than traversing the automaton in post or preorder since it can scan states
     * linearly. Returning false from {@link StateVisitor#accept(int)}
     * immediately terminates the traversal.
     *
     * @param <T> type
     * @param v   v
     * @return state visitor
     */
    public <T extends StateVisitor> T visitAllStates(T v) {
        return visitInPostOrder(v);
    }

    /**
     * Same as {@link #visitInPostOrder(StateVisitor, int)},
     * starting from root automaton node.
     *
     * @param <T> type
     * @param v   v
     * @return state visitor
     */
    public <T extends StateVisitor> T visitInPostOrder(T v) {
        return visitInPostOrder(v, getRootNode());
    }

    /**
     * Visits all states reachable from <code>node</code> in postorder.
     * Returning false from {@link StateVisitor#accept(int)}
     * immediately terminates the traversal.
     *
     * @param <T>  type
     * @param v    v
     * @param node node
     * @return state visitor
     */
    public <T extends StateVisitor> T visitInPostOrder(T v, int node) {
        visitInPostOrder(v, node, new BitSet());
        return v;
    }

    /**
     * Private recursion.
     */
    private boolean visitInPostOrder(StateVisitor v, int node, BitSet visited) {
        if (visited.get(node)) {
            return true;
        }
        visited.set(node);
        for (int arc = getFirstArc(node); arc != 0; arc = getNextArc(arc)) {
            if (!isArcTerminal(arc) && !visitInPostOrder(v, getEndNode(arc), visited)) {
                return false;
            }
        }
        return v.accept(node);
    }

    /**
     * Same as {@link #visitInPreOrder(StateVisitor, int)}, starting from root automaton node.
     *
     * @param <T> type
     * @param v   v
     * @return state visitor
     */
    public <T extends StateVisitor> T visitInPreOrder(T v) {
        return visitInPreOrder(v, getRootNode());
    }

    /**
     * Visits all states in preorder. Returning false from {@link StateVisitor#accept(int)}
     * skips traversal of all sub-states of a given state.
     *
     * @param <T>  type
     * @param v    v
     * @param node node
     * @return state visitor
     */
    public <T extends StateVisitor> T visitInPreOrder(T v, int node) {
        visitInPreOrder(v, node, new BitSet());
        return v;
    }

    /**
     * Private recursion.
     */
    private void visitInPreOrder(StateVisitor v, int node, BitSet visited) {
        if (visited.get(node)) {
            return;
        }
        visited.set(node);
        if (v.accept(node)) {
            for (int arc = getFirstArc(node); arc != 0; arc = getNextArc(arc)) {
                if (!isArcTerminal(arc)) {
                    visitInPreOrder(v, getEndNode(arc), visited);
                }
            }
        }
    }
}
