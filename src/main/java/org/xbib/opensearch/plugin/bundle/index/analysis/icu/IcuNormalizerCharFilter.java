package org.xbib.opensearch.plugin.bundle.index.analysis.icu;

import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.charfilter.BaseCharFilter;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;

/**
 * Normalize token text with ICU {@link Normalizer2}.
 */
public final class IcuNormalizerCharFilter extends BaseCharFilter {

    private final Normalizer2 normalizer;
    private final StringBuilder inputBuffer = new StringBuilder();
    private final StringBuilder resultBuffer = new StringBuilder();
    private final char[] tmpBuffer;
    private boolean inputFinished;
    private boolean afterQuickCheckYes;
    private int checkedInputBoundary;
    private int charCount;

    /**
     * Create a new Normalizer2CharFilter with the specified Normalizer2.
     *
     * @param in         text
     * @param normalizer normalizer to use
     */
    public IcuNormalizerCharFilter(Reader in, Normalizer2 normalizer) {
        this(in, normalizer, 128);
    }

    private IcuNormalizerCharFilter(Reader in, Normalizer2 normalizer, int bufferSize) {
        super(in);
        this.normalizer = Objects.requireNonNull(normalizer);
        this.tmpBuffer = new char[bufferSize];
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (off < 0) {
            throw new IllegalArgumentException("off < 0");
        }
        if (off >= cbuf.length) {
            throw new IllegalArgumentException("off >= cbuf.length");
        }
        if (len <= 0) {
            throw new IllegalArgumentException("len <= 0");
        }
        while (!inputFinished || inputBuffer.length() > 0 || resultBuffer.length() > 0) {
            int retLen;
            if (resultBuffer.length() > 0) {
                retLen = outputFromResultBuffer(cbuf, off, len);
                if (retLen > 0) {
                    return retLen;
                }
            }
            int resLen = readAndNormalizeFromInput();
            if (resLen > 0) {
                retLen = outputFromResultBuffer(cbuf, off, len);
                if (retLen > 0) {
                    return retLen;
                }
            }
            readInputToBuffer();
        }
        return -1;
    }

    private int readInputToBuffer() throws IOException {
        final int len = input.read(tmpBuffer);
        if (len == -1) {
            inputFinished = true;
            return 0;
        }
        inputBuffer.append(tmpBuffer, 0, len);

        // if checkedInputBoundary was at the end of a buffer, we need to check that char again
        checkedInputBoundary = Math.max(checkedInputBoundary - 1, 0);
        // this loop depends on 'isInert' (changes under normalization) but looks only at characters.
        // so we treat all surrogates as non-inert for simplicity
        if (normalizer.isInert(tmpBuffer[len - 1]) && !Character.isSurrogate(tmpBuffer[len - 1])) {
            return len;
        } else {
            return len + readInputToBuffer();
        }
    }

    private int readAndNormalizeFromInput() {
        if (inputBuffer.length() <= 0) {
            afterQuickCheckYes = false;
            return 0;
        }
        if (!afterQuickCheckYes) {
            int resLen = readFromInputWhileSpanQuickCheckYes();
            afterQuickCheckYes = true;
            if (resLen > 0) {
                return resLen;
            }
        }
        int resLen = readFromIoNormalizeUptoBoundary();
        if (resLen > 0) {
            afterQuickCheckYes = false;
        }
        return resLen;
    }

    private int readFromInputWhileSpanQuickCheckYes() {
        int end = normalizer.spanQuickCheckYes(inputBuffer);
        if (end > 0) {
            resultBuffer.append(inputBuffer.subSequence(0, end));
            inputBuffer.delete(0, end);
            checkedInputBoundary = Math.max(checkedInputBoundary - end, 0);
            charCount += end;
        }
        return end;
    }

    private int readFromIoNormalizeUptoBoundary() {
        // if there's no buffer to normalize, return 0
        if (inputBuffer.length() <= 0) {
            return 0;
        }

        boolean foundBoundary = false;
        final int bufLen = inputBuffer.length();

        while (checkedInputBoundary <= bufLen - 1) {
            int charLen = Character.charCount(inputBuffer.codePointAt(checkedInputBoundary));
            checkedInputBoundary += charLen;
            if (checkedInputBoundary < bufLen && normalizer.hasBoundaryBefore(inputBuffer
                    .codePointAt(checkedInputBoundary))) {
                foundBoundary = true;
                break;
            }
        }
        if (!foundBoundary && checkedInputBoundary >= bufLen && inputFinished) {
            foundBoundary = true;
            checkedInputBoundary = bufLen;
        }

        if (!foundBoundary) {
            return 0;
        }

        return normalizeInputUpto(checkedInputBoundary);
    }

    private int normalizeInputUpto(final int length) {
        final int destOrigLen = resultBuffer.length();
        normalizer.normalizeSecondAndAppend(resultBuffer,
                inputBuffer.subSequence(0, length));
        inputBuffer.delete(0, length);
        checkedInputBoundary = Math.max(checkedInputBoundary - length, 0);
        final int resultLength = resultBuffer.length() - destOrigLen;
        recordOffsetDiff(length, resultLength);
        return resultLength;
    }

    private void recordOffsetDiff(int inputLength, int outputLength) {
        if (inputLength == outputLength) {
            charCount += outputLength;
            return;
        }
        final int diff = inputLength - outputLength;
        final int cumuDiff = getLastCumulativeDiff();
        if (diff < 0) {
            for (int i = 1; i <= -diff; ++i) {
                addOffCorrectMap(charCount + i, cumuDiff - i);
            }
        } else {
            addOffCorrectMap(charCount + outputLength, cumuDiff + diff);
        }
        charCount += outputLength;
    }

    private int outputFromResultBuffer(char[] cbuf, int begin, int l) {
        int len = Math.min(resultBuffer.length(), l);
        resultBuffer.getChars(0, len, cbuf, begin);
        if (len > 0) {
            resultBuffer.delete(0, len);
        }
        return len;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IcuNormalizerCharFilter;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
