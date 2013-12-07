
package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * A {@link org.apache.lucene.analysis.TokenStream} wrapper that keeps track of
 * the current term position of the given TokenStream,
 * and defines a comparison order.
 */
public class PositionedTokenStream extends TokenFilter implements Comparable<PositionedTokenStream> {

    // Attributes to track
    private final OffsetAttribute offsetAttr;
    private final PositionIncrementAttribute posAttr;
    /**
     * Position tracker.
     */
    private int position;

    public PositionedTokenStream(TokenStream input) {
        super(input);

        // Force loading/adding these attributes
        // won't do much bad if they're not read/written
        offsetAttr = input.addAttribute(OffsetAttribute.class);
        posAttr = input.addAttribute(PositionIncrementAttribute.class);

        this.position = 0;
    }

    /**
     * Returns the tracked current token position.
     *
     * @return The accumulated position increment attribute values.
     */
    public int getPosition() {
        return position;
    }

    /*
     * "TokenStream interface"
     */

    public final boolean incrementToken() throws IOException {
        boolean rtn = input.incrementToken();
        if (!rtn) {
            position = Integer.MAX_VALUE;
        }
        // Track accumulated position
        position += posAttr.getPositionIncrement();
        return rtn;
    }

    public void end() throws IOException {
        input.end();
        position = 0;
    }

    public void reset() throws IOException {
        input.reset();
        position = 0;
    }

    public void close() throws IOException {
        input.close();
        position = 0;
    }

    /**
     * Permit ordering by reading order: term position, then term offsets (start, then end).
     */
    @Override
    public int compareTo(PositionedTokenStream that) {
        // Nullity checks
        if (that == null) {
            return 1;
        }
        // Position checks
        if (this.position != that.position) {
            return this.position - that.position;
        }
        // TokenStream nullity checks
        if (that.input == null) {
            if (this.input == null) {
                return 0;
            } else {
                return 1;
            }
        } else if (this.input == null) {
            return -1;
        }
        // Order by reading order, using offsets
        if (this.offsetAttr != null && that.offsetAttr != null) {
            int a = this.offsetAttr.startOffset();
            int b = that.offsetAttr.startOffset();
            if (a != b) {
                return a - b;
            }
            a = this.offsetAttr.endOffset();
            b = that.offsetAttr.endOffset();
            return a - b;
        } else if (that.offsetAttr == null) {
            if (this.offsetAttr == null) {
                return 0;
            }
            return 1;
        } else {
            return -1;
        }
    }

}
