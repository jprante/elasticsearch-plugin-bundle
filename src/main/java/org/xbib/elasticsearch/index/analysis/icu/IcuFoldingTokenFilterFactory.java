package org.xbib.elasticsearch.index.analysis.icu;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.io.InputStream;

/**
 * Applies foldings from UTR#30 Character Foldings.
 * Can be filtered to handle certain characters in a specified way.
 * See http://icu-project.org/apiref/icu4j/com/ibm/icu/text/UnicodeSet.html
 * E.g national chars that should be retained, like unicode_set_filter : "[^åäöÅÄÖ]".
 */
public class IcuFoldingTokenFilterFactory extends IcuNormalizerTokenFilterFactory {

    public IcuFoldingTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                        Settings settings) {
        super(indexSettings, environment, name, settings);
    }

    @Override
    public Object getMultiTermComponent() {
        return this;
    }

    @Override
    protected String getNormalizationName(Settings settings) {
        return settings.get("normalization_name", "utr30");
    }

    @Override
    protected InputStream getNormalizationResource(Settings settings) {
        InputStream inputStream = null;
        if ("utr30".equals(getNormalizationName(settings))) {
            inputStream = getClass().getResourceAsStream("/icu/folding/utr30.nrm");
        }
        return inputStream;
    }
}
