package org.xbib.elasticsearch.index.analysis.worddelimiter;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.Analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class WordDelimiterFilterFactory extends AbstractTokenFilterFactory implements WordDelimiterFlags {

    // source => type
    private static final Pattern typePattern = Pattern.compile("(.*)\\s*=>\\s*(.*)\\s*$");

    private final char[] out = new char[256];

    private Set<String> protectedWords = null;

    private int flags;

    private byte[] typeTable = null;

    public WordDelimiterFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                      Settings settings) {
        super(indexSettings, name, settings);

        // Sample Format for the type table:
        // $ => DIGIT
        // % => DIGIT
        // . => DIGIT
        // \u002C => DIGIT
        // \u200D => ALPHANUM
        List<String> charTypeTableValues = Analysis.getWordList(environment, settings, "type_table");
        if (charTypeTableValues == null) {
            this.typeTable = WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE;
        } else {
            this.typeTable = parseTypes(charTypeTableValues);
        }

        // If 1, causes parts of words to be generated: "PowerShot" => "Power" "Shot"
        flags |= getFlag(GENERATE_WORD_PARTS, settings, "generate_word_parts", true);
        // If 1, causes number subwords to be generated: "500-42" => "500" "42"
        flags |= getFlag(GENERATE_NUMBER_PARTS, settings, "generate_number_parts", true);
        // If 1, causes maximum runs of word parts to be catenated: "wi-fi" => "wifi"
        flags |= getFlag(CATENATE_WORDS, settings, "catenate_words", false);
        // If 1, causes maximum runs of number parts to be catenated: "500-42" => "50042"
        flags |= getFlag(CATENATE_NUMBERS, settings, "catenate_numbers", false);
        // If 1, causes all subword parts to be catenated: "wi-fi-4000" => "wifi4000"
        flags |= getFlag(CATENATE_ALL, settings, "catenate_all", false);
        // If 1, causes "PowerShot" to be two tokens; ("Power-Shot" remains two parts regards)
        flags |= getFlag(SPLIT_ON_CASE_CHANGE, settings, "split_on_case_change", true);
        // If 1, causes "j2se" to be three tokens; "j" "2" "se"
        flags |= getFlag(SPLIT_ON_NUMERICS, settings, "split_on_numerics", true);
        // If 1, includes original words in subwords: "500-42" => "500" "42" "500-42"
        flags |= getFlag(PRESERVE_ORIGINAL, settings, "preserve_original", false);
        // If 1, causes trailing "'s" to be removed for each subword: "O'Neil's" => "O", "Neil"
        flags |= getFlag(STEM_ENGLISH_POSSESSIVE, settings, "stem_english_possessive", true);
        // If not null is the set of tokens to protect from being delimited
        List<String> protoWords = Analysis.getWordList(environment, settings, "protected_words");
        protectedWords = protoWords == null ? null : new HashSet<>(protoWords);
    }

    public WordDelimiterFilter2 create(TokenStream input) {
        return new WordDelimiterFilter2(input, typeTable, flags, protectedWords);
    }

    public int getFlag(int flag, Settings settings, String key, boolean defaultValue) {
        if (settings.getAsBoolean(key, defaultValue)) {
            return flag;
        }
        return 0;
    }

    // parses a list of MappingCharFilter style rules into a custom byte[] type table
    private byte[] parseTypes(List<String> rules) {
        SortedMap<Character, Byte> typeMap = new TreeMap<>();
        for (String rule : rules) {
            Matcher m = typePattern.matcher(rule);
            if (!m.find()) {
                throw new ElasticsearchException("invalid rule : [" + rule + "]");
            }
            String lhs = parseString(m.group(1).trim());
            Byte rhs = parseType(m.group(2).trim());
            if (lhs.length() != 1) {
                throw new ElasticsearchException("invalid rule : [" + rule + "]: only single character allowed");
            }
            if (rhs == null) {
                throw new ElasticsearchException("invalid rule : [" + rule + "]: type illegal");
            }
            typeMap.put(lhs.charAt(0), rhs);
        }

        // ensure the table is always at least as big as DEFAULT_WORD_DELIM_TABLE for performance
        byte[] types = new byte[Math.max(typeMap.lastKey() + 1, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE.length)];
        for (int i = 0; i < types.length; i++) {
            types[i] = WordDelimiterIterator.getType(i);
        }
        for (Map.Entry<Character, Byte> mapping : typeMap.entrySet()) {
            types[mapping.getKey()] = mapping.getValue();
        }
        return types;
    }

    private Byte parseType(String s) {
        switch (s) {
            case "LOWER":
                return LOWER;
            case "UPPER":
                return UPPER;
            case "ALPHA":
                return ALPHA;
            case "DIGIT":
                return DIGIT;
            case "ALPHANUM":
                return ALPHANUM;
            case "SUBWORD_DELIM":
                return SUBWORD_DELIM;
            default:
                return null;
        }
    }

    private String parseString(String s) {
        int readPos = 0;
        int len = s.length();
        int writePos = 0;
        while (readPos < len) {
            char c = s.charAt(readPos++);
            if (c == '\\') {
                if (readPos >= len) {
                    throw new ElasticsearchException("invalid escaped char in [" + s + "]");
                }
                c = s.charAt(readPos++);
                switch (c) {
                    case '\\':
                        c = '\\';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'u':
                        if (readPos + 3 >= len) {
                            throw new ElasticsearchException("invalid escaped char in [" + s + "]");
                        }
                        c = (char) Integer.parseInt(s.substring(readPos, readPos + 4), 16);
                        readPos += 4;
                        break;
                    default:
                        break;
                }
            }
            out[writePos++] = c;
        }
        return new String(out, 0, writePos);
    }
}
