package org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ICU-based tokenizer, optionally using ICU rbbi rules files.
 */
public class IcuTokenizerFactory extends AbstractTokenizerFactory {

    protected final IcuTokenizerConfig config;

    public IcuTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, settings, name);
        boolean cjkAsWords = settings.getAsBoolean("cjk_as_words", true);
        boolean myanmarAsWords = settings.getAsBoolean("myanmar_as_words", true);
        Map<Integer, String> tailored = new HashMap<>();
        List<String> scriptAndResourcePaths = settings.getAsList("rulefiles");
        if (scriptAndResourcePaths != null) {
            for (String scriptAndResourcePath : scriptAndResourcePaths) {
                // "rulefiles" : "Latn:my.Latin.rules.rbbi,Cyrl:my.Cyrillic.rules.rbbi"
                int colonPos = scriptAndResourcePath.indexOf(':');
                String scriptCode = scriptAndResourcePath.substring(0, colonPos).trim();
                String resourcePath = scriptAndResourcePath.substring(colonPos + 1).trim();
                tailored.put(UCharacter.getPropertyValueEnum(UProperty.SCRIPT, scriptCode), resourcePath);
            }
        }
        if (tailored.isEmpty()) {
            this.config = new DefaultIcuTokenizerConfig(cjkAsWords, myanmarAsWords);
        } else {
            final BreakIterator[] breakers = new BreakIterator[UCharacter.getIntPropertyMaxValue(UProperty.SCRIPT)];
            for (Map.Entry<Integer, String> entry : tailored.entrySet()) {
                int code = entry.getKey();
                String resourcePath = entry.getValue();
                StringBuilder rules = new StringBuilder();
                String line;
                try {
                    InputStream rulesStream = IcuTokenizerFactory.class.getResourceAsStream(resourcePath);
                    if (rulesStream == null) {
                        throw new ElasticsearchException("rules stream not found: " + resourcePath);
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(rulesStream, StandardCharsets.UTF_8));
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
            this.config = new DefaultIcuTokenizerConfig(cjkAsWords, myanmarAsWords) {

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
    public Tokenizer create() {
        return new IcuTokenizer(config);
    }
}
