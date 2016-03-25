package org.xbib.elasticsearch.index.analysis.icu.segmentation;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import com.ibm.icu.text.UTF16;

import java.text.CharacterIterator;

/**
 * Contain all the issues surrounding BreakIterators in ICU in one place.
 * Basically this boils down to the fact that they aren't very friendly to any
 * sort of OO design.
 *
 * http://bugs.icu-project.org/trac/ticket/5901: RBBI.getRuleStatus(), hoist to
 * BreakIterator from RuleBasedBreakIterator
 *
 * DictionaryBasedBreakIterator is a subclass of RuleBasedBreakIterator, but
 * doesn't actually behave as a subclass: it always returns 0 for
 * getRuleStatus():
 * http://bugs.icu-project.org/trac/ticket/4730: Thai RBBI, no boundary type
 * tags
 */
abstract class BreakIteratorWrapper {

    protected final CharArrayIterator textIterator = new CharArrayIterator();

    protected char text[];

    protected int start;

    protected int length;

    /**
     * If its a RuleBasedBreakIterator, the rule status can be used for token type. If its
     * any other BreakIterator, the rulestatus method is not available, so treat
     * it like a generic BreakIterator.
     */
    static BreakIteratorWrapper wrap(BreakIterator breakIterator) {
        if (breakIterator instanceof RuleBasedBreakIterator) {
            return new RBBIWrapper((RuleBasedBreakIterator) breakIterator);
        } else {
            return new BIWrapper(breakIterator);
        }
    }

    abstract int next();

    abstract int current();

    abstract int getRuleStatus();

    abstract void setText(CharacterIterator text);

    void setText(char text[], int start, int length) {
        this.text = text;
        this.start = start;
        this.length = length;
        textIterator.setText(text, start, length);
        setText(textIterator);
    }

    /**
     * RuleBasedBreakIterator wrapper: RuleBasedBreakIterator (as long as its not
     * a DictionaryBasedBreakIterator) behaves correctly.
     */
    static final class RBBIWrapper extends BreakIteratorWrapper {
        private final RuleBasedBreakIterator rbbi;

        RBBIWrapper(RuleBasedBreakIterator rbbi) {
            this.rbbi = rbbi;
        }

        @Override
        int current() {
            return rbbi.current();
        }

        @Override
        int getRuleStatus() {
            return rbbi.getRuleStatus();
        }

        @Override
        int next() {
            return rbbi.next();
        }

        @Override
        void setText(CharacterIterator text) {
            rbbi.setText(text);
        }
    }

    /**
     * Generic BreakIterator wrapper: Either the rulestatus method is not
     * available or always returns 0. Calculate a rulestatus here so it behaves
     * like RuleBasedBreakIterator.
     * <p/>
     * Note: This is slower than RuleBasedBreakIterator.
     */
    static final class BIWrapper extends BreakIteratorWrapper {
        private final BreakIterator bi;
        private int status;

        BIWrapper(BreakIterator bi) {
            this.bi = bi;
        }

        @Override
        int current() {
            return bi.current();
        }

        @Override
        int getRuleStatus() {
            return status;
        }

        @Override
        int next() {
            int current = bi.current();
            int next = bi.next();
            status = calcStatus(current, next);
            return next;
        }

        private int calcStatus(int current, int next) {
            if (current == BreakIterator.DONE || next == BreakIterator.DONE) {
                return RuleBasedBreakIterator.WORD_NONE;
            }
            int begin = start + current;
            int end = start + next;
            int codepoint;
            for (int i = begin; i < end; i += UTF16.getCharCount(codepoint)) {
                codepoint = UTF16.charAt(text, 0, end, begin);
                if (UCharacter.isDigit(codepoint)) {
                    return RuleBasedBreakIterator.WORD_NUMBER;
                } else if (UCharacter.isLetter(codepoint)) {
                    return RuleBasedBreakIterator.WORD_LETTER;
                }
            }
            return RuleBasedBreakIterator.WORD_NONE;
        }

        @Override
        void setText(CharacterIterator text) {
            bi.setText(text);
            status = RuleBasedBreakIterator.WORD_NONE;
        }
    }
}
