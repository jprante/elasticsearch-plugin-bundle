package org.xbib.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class MockAnalyzer extends Analyzer {

    private final CharacterRunAutomaton runAutomaton;

    private final boolean lowerCase;

    private final CharacterRunAutomaton filter;

    private int positionIncrementGap;

    private Integer offsetGap;

    private Map<String, Integer> previousMappings = new HashMap<>();

    private int maxTokenLength = MockTokenizer.DEFAULT_MAX_TOKEN_LENGTH;

    public MockAnalyzer(CharacterRunAutomaton runAutomaton, boolean lowerCase) {
        this(runAutomaton, lowerCase, MockTokenFilter.EMPTY_STOPSET);
    }

    public MockAnalyzer(CharacterRunAutomaton runAutomaton, boolean lowerCase, CharacterRunAutomaton filter) {
        super(PER_FIELD_REUSE_STRATEGY);
        this.runAutomaton = runAutomaton;
        this.lowerCase = lowerCase;
        this.filter = filter;
    }

    @Override
    public TokenStreamComponents createComponents(String fieldName) {
        MockTokenizer tokenizer = new MockTokenizer(runAutomaton, lowerCase, maxTokenLength);
        MockTokenFilter filt = new MockTokenFilter(tokenizer, filter);
        return new TokenStreamComponents(tokenizer, maybePayload(filt, fieldName));
    }

    private synchronized TokenFilter maybePayload(TokenFilter stream, String fieldName) {
        Integer val = previousMappings.get(fieldName);
        if (val == null) {
            val = -1; // no payloads
            previousMappings.put(fieldName, val); // save it so we are consistent for this field
        }
        return stream;
    }

    public void setPositionIncrementGap(int positionIncrementGap) {
        this.positionIncrementGap = positionIncrementGap;
    }

    @Override
    public int getPositionIncrementGap(String fieldName) {
        return positionIncrementGap;
    }

    /**
     * Set a new offset gap which will then be added to the offset when several fields with the same name are indexed
     *
     * @param offsetGap The offset gap that should be used.
     */
    public void setOffsetGap(int offsetGap) {
        this.offsetGap = offsetGap;
    }

    /**
     * Get the offset gap between tokens in fields if several fields with the same name were added.
     *
     * @param fieldName Currently not used, the same offset gap is returned for each field.
     */
    @Override
    public int getOffsetGap(String fieldName) {
        return offsetGap == null ? super.getOffsetGap(fieldName) : offsetGap;
    }

    /**
     * Toggle maxTokenLength for MockTokenizer
     */
    public void setMaxTokenLength(int length) {
        this.maxTokenLength = length;
    }
}
