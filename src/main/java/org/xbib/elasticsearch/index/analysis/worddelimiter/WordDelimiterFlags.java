package org.xbib.elasticsearch.index.analysis.worddelimiter;

/**
 *
 */
public interface WordDelimiterFlags {
    int LOWER = 0x01;

    int UPPER = 0x02;

    int DIGIT = 0x04;

    int SUBWORD_DELIM = 0x08;

    int ALPHA = 0x03;

    int ALPHANUM = 0x07;

    /**
     * Causes parts of words to be generated:
     * "PowerShot" =&gt; "Power" "Shot"
     */
    int GENERATE_WORD_PARTS = 1;

    /**
     * Causes number subwords to be generated:
     * "500-42" =&gt; "500" "42"
     */
    int GENERATE_NUMBER_PARTS = 2;

    /**
     * Causes maximum runs of word parts to be catenated:
     * "wi-fi" =&gt; "wifi"
     */
    int CATENATE_WORDS = 4;

    /**
     * Causes maximum runs of word parts to be catenated:
     * "wi-fi" =&gt;"wifi"
     */
    int CATENATE_NUMBERS = 8;

    /**
     * Causes all subword parts to be catenated:
     * "wi-fi-4000" =&gt; "wifi4000"
     */
    int CATENATE_ALL = 16;

    /**
     * Causes original words are preserved and added to the subword list (Defaults to false)
     * "500-42" =&gt; "500" "42" "500-42"
     */
    int PRESERVE_ORIGINAL = 32;

    /**
     * If not set, causes case changes to be ignored (subwords will only be generated
     * given SUBWORD_DELIM tokens)
     */
    int SPLIT_ON_CASE_CHANGE = 64;

    /**
     * If not set, causes numeric changes to be ignored (subwords will only be generated
     * given SUBWORD_DELIM tokens).
     */
    int SPLIT_ON_NUMERICS = 128;

    /**
     * Causes trailing "'s" to be removed for each subword
     * "O'Neil's" =&gt; "O", "Neil"
     */
    int STEM_ENGLISH_POSSESSIVE = 256;

    /**
     * Causes every parts to share the same position.
     * The default is off and causes each intermediate part to take its own position.
     */
    int ALL_PARTS_AT_SAME_POSITION = 512;

}
