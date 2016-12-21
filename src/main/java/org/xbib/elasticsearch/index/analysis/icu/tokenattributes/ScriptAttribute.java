package org.xbib.elasticsearch.index.analysis.icu.tokenattributes;

import org.apache.lucene.util.Attribute;

/**
 * This attribute stores the UTR #24 script value for a token of text.
 */
public interface ScriptAttribute extends Attribute {
    /**
     * Get the numeric code for this script value.
     * This is the constant value from {@link com.ibm.icu.lang.UScript}.
     *
     * @return numeric code
     */
    int getCode();

    /**
     * Set the numeric code for this script value.
     * This is the constant value from {@link com.ibm.icu.lang.UScript}.
     *
     * @param code numeric code
     */
    void setCode(int code);

    /**
     * Get the full name.
     *
     * @return UTR #24 full name.
     */
    String getName();

    /**
     * Get the abbreviated name.
     *
     * @return UTR #24 abbreviated name.
     */
    String getShortName();
}
