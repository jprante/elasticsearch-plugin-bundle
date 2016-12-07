package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 *
 */
public class IcuNumberFormatTokenFilterFactory extends AbstractTokenFilterFactory {

    private final NumberFormat numberFormat;

    public IcuNumberFormatTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                             Settings settings) {
        super(indexSettings, name, settings);
        ULocale locale = settings.get("locale") != null ? new ULocale(settings.get("locale")) : ULocale.getDefault();
        String formatStr = settings.get("format", "SPELLOUT");
        int format;
        switch (formatStr.toUpperCase()) {
            case "SPELLOUT":
                format = RuleBasedNumberFormat.SPELLOUT;
                break;
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
            default:
                format = RuleBasedNumberFormat.SPELLOUT;
                break;
        }
        RuleBasedNumberFormat ruleBasedNumberFormat = new RuleBasedNumberFormat(locale, format);
        // RBNF parsing is incredibly slow when lenient is enabled but the only method to parse compound number words
        ruleBasedNumberFormat.setLenientParseMode(settings.getAsBoolean("lenient", true));
        ruleBasedNumberFormat.setGroupingUsed(settings.getAsBoolean("grouping", true));
        this.numberFormat = ruleBasedNumberFormat;
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new IcuNumberFormatTokenFilter(tokenStream, numberFormat);
    }
}
