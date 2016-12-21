package org.xbib.elasticsearch.common.fsa;

/**
 * A matching result returned from {@link FSATraversal}.
 *
 * @see FSATraversal
 */
public final class MatchResult {
    /**
     * The automaton has exactly one match for the input sequence.
     */
    public static final int EXACT_MATCH = 0;

    /**
     * The automaton has no match for the input sequence.
     */
    public static final int NO_MATCH = -1;

    /**
     * The automaton contains a prefix of the input sequence. That is:
     * one of the input sequences used to build the automaton is a
     * prefix of the input sequence that is shorter than the sequence.
     * {@link MatchResult#index} will contain an index of the
     * first character of the input sequence not present in the
     * dictionary.
     */
    public static final int AUTOMATON_HAS_PREFIX = -3;

    /**
     * The sequence is a prefix of at least one sequence in the automaton.
     * {@link MatchResult#node} returns the node from which all sequences
     * with the given prefix start in the automaton.
     */
    public static final int SEQUENCE_IS_A_PREFIX = -4;

    /**
     * One of the match kind constants defined in this class.
     *
     * @see #NO_MATCH
     * @see #EXACT_MATCH
     * @see #AUTOMATON_HAS_PREFIX
     * @see #SEQUENCE_IS_A_PREFIX
     */
    private int kind;

    /**
     * Input sequence's index, interpretation depends on {@link #kind}.
     */
    private int index;

    /**
     * Automaton node, interpretation depends on the {@link #kind}.
     */
    private int node;

    /*
     *
     */
    public MatchResult() {
        reset(NO_MATCH, 0, 0);
    }

    /*
     * 
     */
    final void reset(int kind, int index, int node) {
        this.kind = kind;
        this.index = index;
        this.node = node;
    }

    public int getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    public int getNode() {
        return node;
    }
}
