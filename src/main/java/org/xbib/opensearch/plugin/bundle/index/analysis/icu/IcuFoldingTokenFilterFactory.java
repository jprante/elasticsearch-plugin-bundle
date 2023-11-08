package org.xbib.opensearch.plugin.bundle.index.analysis.icu;

import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;

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
    protected String getNormalizationName(Settings settings) {
        return settings.get("normalization_name", "full");
    }

    @Override
    protected InputStream getNormalizationResource(Settings settings) {
        String string = getNormalizationName(settings);
        return getClass().getResourceAsStream(string + ".nrm");
    }
}
