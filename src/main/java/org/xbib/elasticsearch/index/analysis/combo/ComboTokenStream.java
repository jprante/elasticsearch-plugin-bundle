
package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.Attribute;

import java.io.IOException;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * A TokenStream combining the output of multiple sub-TokenStreams.
 *
 * This class copies the attributes from the last sub-TokenStream that
 * was read from. If attributes are not uniform between sub-TokenStreams,
 * extraneous attributes will stay untouched.
 */
public class ComboTokenStream extends TokenStream {

    /**
     * Whether or not to continue with the current TokenStream
     * if it has multiple terms at same position, minimizing
     * queue moves, or to enforce strict order (position, offsets)
     */
    static final boolean KEEP_STREAM_IF_SAME_POSITION = false;

    private int lastPosition;
    // Position tracked sub-TokenStreams
    private final PositionedTokenStream[] positionedTokenStreams;
    // Reading queue, using the reading order from PositionedTokenStream
    private final AbstractQueue<PositionedTokenStream> readQueue;
    // Flag for lazy initialization and reset
    private boolean readQueueResetted;

    public ComboTokenStream(TokenStream... tokenStreams) {
        // Load the TokenStreams, track their position, and register their attributes
        this.positionedTokenStreams = new PositionedTokenStream[tokenStreams.length];
        for (int i = tokenStreams.length - 1; i >= 0; --i) {
            if (tokenStreams[i] == null) {
                continue;
            }
            this.positionedTokenStreams[i] = new PositionedTokenStream(tokenStreams[i]);
            // Add each and every token seen in the current sub AttributeSource
            Iterator<Class<? extends Attribute>> iterator = this.positionedTokenStreams[i].getAttributeClassesIterator();
            while (iterator.hasNext()) {
                addAttribute(iterator.next());
            }
        }
        this.lastPosition = 0;
        // Create an initially empty queue.
        // It will be filled at first incrementToken() call, because
        // it needs to call the same function on each sub-TokenStreams.
        this.readQueue = new PriorityQueue<PositionedTokenStream>(tokenStreams.length);
        readQueueResetted = false;
    }

    /*
     * TokenStream multiplexed methods
     */

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();

        // Fill the queue on first call
        if (!readQueueResetted) {
            readQueueResetted = true;
            readQueue.clear();
            for (PositionedTokenStream pts : positionedTokenStreams) {
                if (pts == null) {
                    continue;
                }
                // Read first token
                pts.clearAttributes();
                if (pts.incrementToken()) {
                    // PositionedTokenStream.incrementToken() initialized internal
                    // variables to perform proper ordering.
                    // Therefore we can only add it to the queue now!
                    readQueue.add(pts);
                } // no token left (no token at all)
            }
        }

        // Read from the first token
        PositionedTokenStream toRead = readQueue.peek();
        if (toRead == null) {
            return false; // end of streams
        }
        // Look position to see if it will be increased, see usage a bit below
        int pos = toRead.getPosition();

        // Copy the current token attributes from the sub-TokenStream to our AttributeSource
        restoreState(toRead.captureState());
        // Override the PositionIncrementAttribute
        this.getAttribute(PositionIncrementAttribute.class).setPositionIncrement(Math.max(0, pos - lastPosition));

        // Prepare next read
        // We did not remove the TokenStream from the queue yet,
        // because if we have another token available at the same position,
        // we can save a queue movement.
        toRead.clearAttributes();
        if (!toRead.incrementToken()) {
            // No more token to read, remove from the queue
            readQueue.poll();
        } else {
            // Check if token position changed
            if (readQueue.size() > 1) {
                // If yes, re-enter in the priority queue
                readQueue.add(readQueue.poll());
            }   // Otherwise, next call will continue with the same TokenStream (less queue movements)
        }

        lastPosition = pos;

        return true;
    }

    @Override
    public void end() throws IOException {
        super.end();
        lastPosition = 0;
        // Apply on each sub-TokenStream
        for (PositionedTokenStream pts : positionedTokenStreams) {
            if (pts == null) {
                continue;
            }
            pts.end();
        }
        readQueueResetted = false;
        readQueue.clear();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        clearAttributes();
        lastPosition = 0;
        // Apply on each sub-TokenStream
        for (PositionedTokenStream pts : positionedTokenStreams) {
            if (pts == null) {
                continue;
            }
            pts.reset();
        }
        readQueueResetted = false;
        readQueue.clear();
    }

    @Override
    public void close() throws IOException {
        super.close();
        lastPosition = 0;
        // Apply on each sub-TokenStream
        for (PositionedTokenStream pts : positionedTokenStreams) {
            if (pts == null) {
                continue;
            }
            pts.close();
        }
        readQueueResetted = false;
        readQueue.clear();
    }

}
