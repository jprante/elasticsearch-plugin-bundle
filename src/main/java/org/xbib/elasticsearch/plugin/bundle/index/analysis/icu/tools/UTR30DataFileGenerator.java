package org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.tools;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.text.UnicodeSetIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.SuppressForbidden;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates UTR30 data files from icu-project.org.
 * <ol>
 * <li>
 * Downloads nfc.txt, nfkc.txt and nfkc_cf.txt from icu-project.org,
 * </li>
 * <li>
 * Converts round-trip mappings in nfc.txt (containing '=')
 * that map to at least one [:Diacritic:] character
 * into one-way mappings ('&gt;' instead of '=').
 * </li>
 * </ol>
 */
public class UTR30DataFileGenerator {

    private static final Logger logger = LogManager.getLogger(UTR30DataFileGenerator.class.getName());

    private static final String ICU_SVN_TAG_URL
            = "http://source.icu-project.org/repos/icu/icu/tags";

    private static final String ICU_DATA_NORM2_PATH = "icu4c/source/data/unidata/norm2";

    private static final String NFC_TXT = "nfc.txt";

    private static final String NFKC_TXT = "nfkc.txt";

    private static final String NFKC_CF_TXT = "nfkc_cf.txt";

    private static final Pattern ROUND_TRIP_MAPPING_LINE_PATTERN
            = Pattern.compile("^\\s*([^=]+?)\\s*=\\s*(.*)$");

    private static final Pattern VERBATIM_RULE_LINE_PATTERN
            = Pattern.compile("^#\\s*Rule:\\s*verbatim\\s*$", Pattern.CASE_INSENSITIVE);

    private static final Pattern RULE_LINE_PATTERN
            = Pattern.compile("^#\\s*Rule:\\s*(.*)>(.*)", Pattern.CASE_INSENSITIVE);

    private static final Pattern BLANK_OR_COMMENT_LINE_PATTERN
            = Pattern.compile("^\\s*(?:#.*)?$");

    private static final Pattern NUMERIC_VALUE_PATTERN
            = Pattern.compile("Numeric[-\\s_]*Value", Pattern.CASE_INSENSITIVE);

    private static byte[] bytes = new byte[8192];

    public void execute(String releaseTag, String dir) throws IOException {
        getNFKCDataFilesFromIcuProject(releaseTag, dir);
        expandRulesInUTR30DataFiles(dir);
    }

    @SuppressForbidden(reason = "fetching resources from ICU repository is trusted")
    private static void getNFKCDataFilesFromIcuProject(String releaseTag, String dir) throws IOException {
        URL icuTagsURL = new URL(ICU_SVN_TAG_URL + "/");
        URL icuReleaseTagURL = new URL(icuTagsURL, releaseTag + "/");
        URL norm2url = new URL(icuReleaseTagURL, ICU_DATA_NORM2_PATH + "/");
        logger.info("Downloading " + NFKC_TXT + " ... ");
        download(new URL(norm2url, NFKC_TXT), dir + NFKC_TXT);
        logger.info("done.");
        logger.info("Downloading " + NFKC_CF_TXT + " ... ");
        download(new URL(norm2url, NFKC_CF_TXT), dir + NFKC_CF_TXT);
        logger.info("done.");
        logger.info("Downloading " + NFKC_CF_TXT + " and making diacritic rules one-way ... ");
        URLConnection connection = openConnection(new URL(norm2url, NFC_TXT));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),
                StandardCharsets.UTF_8));
             Writer writer = new OutputStreamWriter(new FileOutputStream(dir + NFC_TXT), StandardCharsets.UTF_8)) {
            String line;
            while (null != (line = reader.readLine())) {
                Matcher matcher = ROUND_TRIP_MAPPING_LINE_PATTERN.matcher(line);
                if (matcher.matches()) {
                    final String leftHandSide = matcher.group(1);
                    final String rightHandSide = matcher.group(2).trim();
                    List<String> diacritics = new ArrayList<>();
                    for (String outputCodePoint : rightHandSide.split("\\s+")) {
                        int ch = Integer.parseInt(outputCodePoint, 16);
                        if (UCharacter.hasBinaryProperty(ch, UProperty.DIACRITIC)
                                // gennorm2 fails if U+0653-U+0656 are included in round-trip mappings
                                || (ch >= 0x653 && ch <= 0x656)) {
                            diacritics.add(outputCodePoint);
                        }
                    }
                    if (!diacritics.isEmpty()) {
                        StringBuilder replacementLine = new StringBuilder();
                        replacementLine.append(leftHandSide).append(">").append(rightHandSide);
                        replacementLine.append("  # one-way: diacritic");
                        if (diacritics.size() > 1) {
                            replacementLine.append("s");
                        }
                        for (String diacritic : diacritics) {
                            replacementLine.append(" ").append(diacritic);
                        }
                        line = replacementLine.toString();
                    }
                }
                writer.write(line);
                writer.write("\n");
            }
        }
        logger.info("done.");
    }

    @SuppressForbidden(reason = "fetching resources from ICU repository is trusted")
    private static void download(URL url, String outputFile) throws IOException {
        final URLConnection connection = openConnection(url);
        int numBytes;
        try (InputStream inputStream = connection.getInputStream();
             OutputStream outputStream = new FileOutputStream(outputFile)) {
            while (-1 != (numBytes = inputStream.read(bytes))) {
                outputStream.write(bytes, 0, numBytes);
            }
        }
    }

    @SuppressForbidden(reason = "fetching resources from ICU repository is trusted")
    private static URLConnection openConnection(URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        connection.addRequestProperty("Cache-Control", "no-cache");
        connection.connect();
        return connection;
    }

    @SuppressForbidden(reason = "nio file porting will be done later")
    private static void expandRulesInUTR30DataFiles(String dir) throws IOException {
        FileFilter filter = pathname -> {
            String name = pathname.getName();
            return pathname.isFile() && name.matches(".*\\.(?s:txt)")
                    && !name.equals(NFC_TXT) && !name.equals(NFKC_TXT) && !name.equals(NFKC_CF_TXT);
        };
        if (dir != null) {
            File dirFile = new File(dir);
            File[] files = dirFile.listFiles(filter);
            if (files != null) {
                for (File file : files) {
                    expandDataFileRules(file);
                }
            }
        }
    }

    @SuppressForbidden(reason = "nio file porting will be done later")
    private static void expandDataFileRules(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        String line;
        boolean verbatim = false;
        boolean modified = false;
        int lineNum = 0;
        try (InputStream inputStream = new FileInputStream(file);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            while (null != (line = bufferedReader.readLine())) {
                ++lineNum;
                if (VERBATIM_RULE_LINE_PATTERN.matcher(line).matches()) {
                    verbatim = true;
                    builder.append(line).append("\n");
                } else {
                    Matcher ruleMatcher = RULE_LINE_PATTERN.matcher(line);
                    if (ruleMatcher.matches()) {
                        verbatim = false;
                        builder.append(line).append("\n");
                        try {
                            String leftHandSide = ruleMatcher.group(1).trim();
                            String rightHandSide = ruleMatcher.group(2).trim();
                            expandSingleRule(builder, leftHandSide, rightHandSide);
                        } catch (IllegalArgumentException e) {
                            logger.error("ERROR in " + file.getName() + " line #" + lineNum + ":", e);
                            throw e;
                        }
                        modified = true;
                    } else {
                        if (BLANK_OR_COMMENT_LINE_PATTERN.matcher(line).matches()) {
                            builder.append(line).append("\n");
                        } else {
                            if (verbatim) {
                                builder.append(line).append("\n");
                            } else {
                                modified = true;
                            }
                        }
                    }
                }
            }
        }
        if (modified) {
            logger.info("Expanding rules in and overwriting " + file.getName());
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file, false),
                    StandardCharsets.UTF_8)) {
                writer.write(builder.toString());
            }
        }
    }

    private static void expandSingleRule(StringBuilder builder, String leftHandSide, String rightHandSide) {
        UnicodeSet set = new UnicodeSet(leftHandSide, UnicodeSet.IGNORE_SPACE);
        boolean numericValue = NUMERIC_VALUE_PATTERN.matcher(rightHandSide).matches();
        for (UnicodeSetIterator it = new UnicodeSetIterator(set); it.nextRange(); ) {
            if (it.codepoint != UnicodeSetIterator.IS_STRING) {
                if (numericValue) {
                    for (int cp = it.codepoint; cp <= it.codepointEnd; ++cp) {
                        builder.append(String.format(Locale.ROOT, "%04X", cp)).append('>');
                        builder.append(String.format(Locale.ROOT, "%04X", 0x30 + UCharacter.getNumericValue(cp)));
                        builder.append("   # ").append(UCharacter.getName(cp));
                        builder.append("\n");
                    }
                } else {
                    builder.append(String.format(Locale.ROOT, "%04X", it.codepoint));
                    if (it.codepointEnd > it.codepoint) {
                        builder.append("..").append(String.format(Locale.ROOT, "%04X", it.codepointEnd));
                    }
                    builder.append('>').append(rightHandSide).append("\n");
                }
            } else {
                logger.error("ERROR: String '" + it.getString() + "' found in UnicodeSet");
            }
        }
    }
}
