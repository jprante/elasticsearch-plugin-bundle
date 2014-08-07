/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.BaseTokenStreamTest;

import java.io.IOException;

/**
 * Testcase for {@link ComboTokenStream}.
 */
public class ComboTokenStreamTests extends BaseTokenStreamTest {

    /**
     * A TokenStream that takes the same input as the assertTokenStreamContents() function,
     * and merely passes the corresponding assert.
     */
    public final class ReplayTokenStream extends TokenStream {

        int index;
        int length;
        String[] outputs;
        int[] positionIncrements;
        int[] startOffsets;
        int[] endOffsets;
        CharTermAttribute output;
        PositionIncrementAttribute positionIncrement;
        OffsetAttribute offset;

        ReplayTokenStream(String[] outputs, int[] startOffsets, int[] endOffsets, int[] positionIncrements) {
            index = 0;
            this.outputs = outputs;
            this.startOffsets = startOffsets;
            this.endOffsets = endOffsets;
            this.positionIncrements = positionIncrements;
            if (outputs != null) {
                this.length = outputs.length;
                output = addAttribute(CharTermAttribute.class);
            } else {
                throw new NullPointerException("Outputs is null");
            }
            if (startOffsets != null || endOffsets != null) {
                if (startOffsets == null || startOffsets.length != length) throw new IllegalArgumentException("Bad startOffsets");
                if (endOffsets == null || endOffsets.length != length) throw new IllegalArgumentException("Bad endOffsets");
                offset = addAttribute(OffsetAttribute.class);
            }
            if (positionIncrements != null) {
                if (positionIncrements.length != length) throw new IllegalArgumentException("Bad positionIncrements");
                positionIncrement = addAttribute(PositionIncrementAttribute.class);
            }
        }

        @Override
        public final boolean incrementToken() throws IOException {
            clearAttributes();
            if (index >= length) return false;
            if (output != null) {
                char[] buffer = outputs[index].toCharArray();
                output.copyBuffer(buffer, 0, buffer.length);
            }
            if (offset != null)
                offset.setOffset(startOffsets[index], endOffsets[index]);
            if (positionIncrement != null)
                positionIncrement.setPositionIncrement(positionIncrements[index]);
            index++;
            return true;
        }

    }



    @Test
    public void testReplayTokenStream() throws IOException {
        TokenStream ts = new ReplayTokenStream(
                new String[]{"ab", "cd", "ef"},
                new int[]{ 0,  3,  5},
                new int[]{ 2,  4,  6},
                new int[]{ 1,  1,  1});
        assertTokenStreamContents(ts,
                new String[]{"ab", "cd", "ef"},
                new int[]{ 0,  3,  5},
                new int[]{ 2,  4,  6},
                new int[]{ 1,  1,  1});
    }

    @Test
    public void testSingleTokenStream() throws IOException {
        ComboTokenStream cts = new ComboTokenStream(
                new ReplayTokenStream(
                        new String[]{"ab", "cd", "ef"},
                        new int[]{ 0,  3,  5},
                        new int[]{ 2,  4,  6},
                        new int[]{ 1,  1,  1})
        );
        cts.addAttribute(CheckClearAttributesAttribute.class);
        assertTokenStreamContents(cts,
                new String[]{"ab", "cd", "ef"},
                new int[]{ 0,  3,  5},
                new int[]{ 2,  4,  6},
                new int[]{ 1,  1,  1});
    }

    @Test
    public void testDoubleTokenStream() throws IOException {
        ComboTokenStream cts = new ComboTokenStream(
                new ReplayTokenStream(
                        new String[]{"ab", "cd", "ef"},
                        new int[]{ 0,  3,  5},
                        new int[]{ 2,  4,  6},
                        new int[]{ 1,  1,  1}),
                new ReplayTokenStream(
                        new String[]{"B", "D", "F"},
                        new int[]{ 1,  4,  6},
                        new int[]{ 2,  4,  6},
                        new int[]{ 1,  1,  1})
        );
        assertTokenStreamContents(cts,
                new String[]{"ab", "B", "cd", "D", "ef", "F"},
                new int[]{ 0,  1,  3,  4,  5,  6},
                new int[]{ 2,  2,  4,  4,  6,  6},
                new int[]{ 1,  0,  1,  0,  1,  0});
        // Now in reversed order
        cts = new ComboTokenStream(
                new ReplayTokenStream(
                        new String[]{"B", "D", "F"},
                        new int[]{ 1,  4,  6},
                        new int[]{ 2,  4,  6},
                        new int[]{ 1,  1,  1}),
                new ReplayTokenStream(
                        new String[]{"ab", "cd", "ef"},
                        new int[]{ 0,  3,  5},
                        new int[]{ 2,  4,  6},
                        new int[]{ 1,  1,  1})
        );
        assertTokenStreamContents(cts,
                new String[]{"ab", "B", "cd", "D", "ef", "F"},
                new int[]{ 0,  1,  3,  4,  5,  6},
                new int[]{ 2,  2,  4,  4,  6,  6},
                new int[]{ 1,  0,  1,  0,  1,  0});
    }

    @Test
    public void testDoubleTokenStreamMultipleAtSamePosition() throws IOException {
        ComboTokenStream cts = new ComboTokenStream(
                new ReplayTokenStream(
                        new String[]{"ab", "cd", "ef"},
                        new int[]{ 0,  3,  5},
                        new int[]{ 2,  4,  6},
                        new int[]{ 1,  1,  1}),
                new ReplayTokenStream(
                        new String[]{"A", "B", "C", "D", "E", "F"},
                        new int[]{ 0,  1,  3,  4,  5,  6},
                        new int[]{ 1,  2,  3,  4,  5,  6},
                        new int[]{ 1,  0,  1,  0,  1,  0})
        );
        if (ComboTokenStream.KEEP_STREAM_IF_SAME_POSITION)
            assertTokenStreamContents(cts,
                    new String[]{"A", "B", "ab", "C", "D", "cd", "E", "F", "ef"},
                    new int[]{ 0,  1,  0,  3,  4,  3,  5,  6,  5},
                    new int[]{ 1,  2,  2,  3,  4,  4,  5,  6,  6},
                    new int[]{ 1,  0,  0,  1,  0,  0,  1,  0,  0});
        else
            assertTokenStreamContents(cts,
                    new String[]{"A", "ab", "B", "C", "cd", "D", "E", "ef", "F"},
                    new int[]{ 0,  0,  1,  3,  3,  4,  5,  5,  6},
                    new int[]{ 1,  2,  2,  3,  4,  4,  5,  6,  6},
                    new int[]{ 1,  0,  0,  1,  0,  0,  1,  0,  0});
        // Now in reversed order
        cts = new ComboTokenStream(
                new ReplayTokenStream(
                        new String[]{"A", "B", "C", "D", "E", "F"},
                        new int[]{ 0,  1,  3,  4,  5,  6},
                        new int[]{ 1,  2,  3,  4,  5,  6},
                        new int[]{ 1,  0,  1,  0,  1,  0}),
                new ReplayTokenStream(
                        new String[]{"ab", "cd", "ef"},
                        new int[]{ 0,  3,  5},
                        new int[]{ 2,  4,  6},
                        new int[]{ 1,  1,  1})
        );
        if (ComboTokenStream.KEEP_STREAM_IF_SAME_POSITION)
            assertTokenStreamContents(cts,
                    new String[]{"A", "B", "ab", "C", "D", "cd", "E", "F", "ef"},
                    new int[]{ 0,  1,  0,  3,  4,  3,  5,  6,  5},
                    new int[]{ 1,  2,  2,  3,  4,  4,  5,  6,  6},
                    new int[]{ 1,  0,  0,  1,  0,  0,  1,  0,  0});
        else
            assertTokenStreamContents(cts,
                    new String[]{"A", "ab", "B", "C", "cd", "D", "E", "ef", "F"},
                    new int[]{ 0,  0,  1,  3,  3,  4,  5,  5,  6},
                    new int[]{ 1,  2,  2,  3,  4,  4,  5,  6,  6},
                    new int[]{ 1,  0,  0,  1,  0,  0,  1,  0,  0});
    }

}
