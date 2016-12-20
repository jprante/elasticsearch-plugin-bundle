package org.xbib.elasticsearch.common.fsa;

/**
 * State visitor.
 *
 * @see FSA#visitInPostOrder(StateVisitor)
 * @see FSA#visitInPreOrder(StateVisitor)
 */

@FunctionalInterface
public interface StateVisitor {

    public boolean accept(int state);
}
