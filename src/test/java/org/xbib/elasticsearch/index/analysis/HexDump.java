package org.xbib.elasticsearch.index.analysis;

import java.io.IOException;
import java.io.Writer;

public class HexDump {

    private static final String EOL = System.getProperty("line.separator");

    private static final char[] hexcodes = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private static final int[] shifts = { 28, 24, 20, 16, 12, 8, 4, 0 };

    private HexDump() {
        super();
    }

    public static void dump(byte[] data,  Writer writer) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int offset = 0;
        for (int j = 0; j < data.length; j += 16) {
            int chars_read = data.length - j;
            if (chars_read > 16) {
                chars_read = 16;
            }
            dump(buffer, offset).append(' ');
            for (int k = 0; k < 16; k++) {
                if (k < chars_read) {
                    dump(buffer, data[k + j]);
                } else {
                    buffer.append(' ');
                }
                buffer.append(' ');
            }
            for (int k = 0; k < chars_read; k++) {
                if ((data[k + j] >= ' ') && (data[k + j] < 127)) {
                    buffer.append((char) data[k + j]);
                } else {
                    buffer.append('.');
                }
            }
            buffer.append(EOL);
            writer.write(buffer.toString());
            buffer.setLength(0);
            offset += chars_read;
        }
    }

    /**
     * Dump a long value into a StringBuilder.
     *
     * @param sb the StringBuilder to dump the value in
     * @param value  the long value to be dumped
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder dump(StringBuilder sb, long value) {
        for (int j = 0; j < 8; j++) {
            sb.append(hexcodes[((int) (value >> shifts[j])) & 15]);
        }
        return sb;
    }

    /**
     * Dump a byte value into a StringBuilder.
     *
     * @param sb the StringBuilder to dump the value in
     * @param value  the byte value to be dumped
     * @return StringBuilder containing the dumped value.
     */
    private static StringBuilder dump(StringBuilder sb, byte value) {
        for (int j = 0; j < 2; j++) {
            sb.append(hexcodes[(value >> shifts[j + 6]) & 15]);
        }
        return sb;
    }

}
