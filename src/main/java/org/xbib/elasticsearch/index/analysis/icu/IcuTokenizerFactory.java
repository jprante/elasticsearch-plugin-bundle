/*
 * Copyright (C) 2014 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.RuleBasedBreakIterator;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.xbib.elasticsearch.index.analysis.icu.segmentation.DefaultIcuTokenizerConfig;
import org.xbib.elasticsearch.index.analysis.icu.segmentation.IcuTokenizer;
import org.xbib.elasticsearch.index.analysis.icu.segmentation.IcuTokenizerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;


/**
 * ICU-based tokenizer, optionally using ICU rbbi rules files.
 */
public class IcuTokenizerFactory extends AbstractTokenizerFactory {

    private final IcuTokenizerConfig config;

    @Inject
    public IcuTokenizerFactory(Index index,
                               IndexSettingsService indexSettingsService,
                               @Assisted String name,
                               @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        boolean cjkAsWords = settings.getAsBoolean("cjk_as_words", true);
        Map<Integer, String> tailored = new HashMap<>();
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
            this.config = new DefaultIcuTokenizerConfig(cjkAsWords);
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
                        throw new ElasticsearchException("rules stream not found: " + resourcePath);
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
            this.config = new DefaultIcuTokenizerConfig(cjkAsWords) {

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
