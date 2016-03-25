package org.xbib.elasticsearch.index.analysis.icu.segmentation;

import com.ibm.icu.text.BreakIterator;

/**
 * Class that allows for tailored Unicode Text Segmentation on
 * a per-writing system basis.
 */
public abstract class IcuTokenizerConfig {

    /**
     * Sole constructor. (For invocation by subclass
     * constructors, typically implicit.)
     */
    public IcuTokenizerConfig() {
    }

    /**
     * Return a breakiterator capable of processing a given script.
     *
     * @param script script
     * @return iterator
     */
    public abstract BreakIterator getBreakIterator(int script);

    /**
     * Return a token type value for a given script and BreakIterator
     * rule status.
     *
     * @param script     script
     * @param ruleStatus rule status
     * @return type
     */
    public abstract String getType(int script, int ruleStatus);

    /**
     * @return true if Han, Hiragana, and Katakana scripts should all be returned as Japanese
     */
    public abstract boolean combineCJ();
}
