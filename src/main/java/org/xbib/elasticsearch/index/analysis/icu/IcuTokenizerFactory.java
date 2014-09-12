package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.icu.segmentation.DefaultICUTokenizerConfig;
import org.apache.lucene.analysis.icu.segmentation.ICUTokenizer;
import org.apache.lucene.analysis.icu.segmentation.ICUTokenizerConfig;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;

import static org.elasticsearch.common.collect.Maps.newHashMap;

/**
 * ICU-based tokenizer, optionally using ICU rbbi rules files.
 */
public class IcuTokenizerFactory extends AbstractTokenizerFactory {

    private final ICUTokenizerConfig config;

    @Inject
    public IcuTokenizerFactory(Index index,
                               @IndexSettings Settings indexSettings,
                               @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        boolean cjkAsWords = settings.getAsBoolean("cjk_as_words", true);
        Map<Integer, String> tailored = newHashMap();
        String[] scriptAndResourcePaths = settings.getAsArray("rulefiles");
        if (scriptAndResourcePaths != null) {
            for (String scriptAndResourcePath : scriptAndResourcePaths) {
                // "rulefiles" : "Latn:my.Latin.rules.rbbi,Cyrl:my.Cyrillic.rules.rbbi"
                int colonPos = scriptAndResourcePath.indexOf(":");
                String scriptCode = scriptAndResourcePath.substring(0, colonPos).trim();
                String resourcePath = scriptAndResourcePath.substring(colonPos+1).trim();
                tailored.put(UCharacter.getPropertyValueEnum(UProperty.SCRIPT, scriptCode), resourcePath);
            }
        }
        if (tailored.isEmpty()) {
            this.config = new DefaultICUTokenizerConfig(cjkAsWords);
        } else {
            final BreakIterator breakers[] = new BreakIterator[UScript.CODE_LIMIT];
            for (Map.Entry<Integer,String> entry : tailored.entrySet()) {
                int code = entry.getKey();
                String resourcePath = entry.getValue();
                StringBuilder rules = new StringBuilder();
                String line;
                try {
                    InputStream rulesStream = getClass().getResourceAsStream("/" + resourcePath);
                    if (rulesStream == null) {
                        throw new ElasticsearchIllegalArgumentException("rules stream not found: " + resourcePath);
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(rulesStream, Charset.forName("UTF-8")));
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("#")) {
                            rules.append(line);
                        }
                        rules.append('\n');
                    }
                    reader.close();
                } catch (IOException e) {
                    logger.error("unable to parse rules", e);
                }
                breakers[code] = new RuleBasedBreakIterator(rules.toString());
            }
            this.config = new DefaultICUTokenizerConfig(cjkAsWords) {

                @Override
                public BreakIterator getBreakIterator(int script) {
                    if (breakers[script] != null) {
                        return (BreakIterator) breakers[script].clone();
                    } else {
                        return super.getBreakIterator(script);
                    }
                }
            };
        }
    }

    @Override
    public Tokenizer create(Reader reader) {
        return new ICUTokenizer(reader, config);
    }
}
