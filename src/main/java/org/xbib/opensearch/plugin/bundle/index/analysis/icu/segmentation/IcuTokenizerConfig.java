package org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation;

import com.ibm.icu.text.BreakIterator;

/**
 * Class that allows for tailored Unicode Text Segmentation on
 * a per-writing system basis.
 */
public interface IcuTokenizerConfig {

    /**
     * Return a breakiterator capable of processing a given script.
     *
     * @param script script
     * @return iterator
     */
    BreakIterator getBreakIterator(int script);

    /**
     * Return a token type value for a given script and BreakIterator
     * rule status.
     *
     * @param script     script
     * @param ruleStatus rule status
     * @return type
     */
    String getType(int script, int ruleStatus);

    /**
     * @return true if Han, Hiragana, and Katakana scripts should all be returned as Japanese
     */
    boolean combineCJ();
}
