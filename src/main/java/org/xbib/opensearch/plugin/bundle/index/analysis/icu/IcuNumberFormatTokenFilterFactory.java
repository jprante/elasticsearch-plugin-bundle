package org.xbib.opensearch.plugin.bundle.index.analysis.icu;

import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;
import org.apache.lucene.analysis.TokenStream;
import org.opensearch.common.settings.Settings;
import org.opensearch.env.Environment;
import org.opensearch.index.IndexSettings;
import org.opensearch.index.analysis.AbstractTokenFilterFactory;

import java.util.Locale;

/**
 * ICU number format token filter factory.
 */
public class IcuNumberFormatTokenFilterFactory extends AbstractTokenFilterFactory {

    private final ULocale locale;

    private final int format;

    private final boolean lenient;

    private final boolean grouping;

    public IcuNumberFormatTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                             Settings settings) {
        super(indexSettings, name, settings);
        this.locale = settings.get("locale") != null ? new ULocale(settings.get("locale")) : ULocale.getDefault();
        String formatStr = settings.get("format", "SPELLOUT");
        switch (formatStr.toUpperCase(Locale.ROOT)) {
            case "DURATION":
                format = RuleBasedNumberFormat.DURATION;
                break;
            case "NUMBERING_SYSTEM":
                format = RuleBasedNumberFormat.NUMBERING_SYSTEM;
                break;
            case "NUMBERSTYLE":
                format = RuleBasedNumberFormat.NUMBERSTYLE;
                break;
            case "ORDINAL":
                format = RuleBasedNumberFormat.ORDINAL;
                break;
            case "SPELLOUT":
            default:
                format = RuleBasedNumberFormat.SPELLOUT;
                break;
        }
        // RBNF parsing is incredibly slow when lenient is enabled but the only method to parse compound number words
        this.lenient = settings.getAsBoolean("lenient", true);
        this.grouping = settings.getAsBoolean("grouping", true);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        // create a new number format instance for each token stream
        RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(locale, format);
        ruleBasedNumberFormat.setLenientParseMode(lenient);
        ruleBasedNumberFormat.setGroupingUsed(grouping);
        return new IcuNumberFormatTokenFilter(tokenStream, ruleBasedNumberFormat);
    }
}
