package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;
import com.ibm.icu.util.ULocale;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

/**
 * An ICU collation analyzer provider.
 * There are two ways to configure collation:
 * The first is simply specifying the locale (defaults to the default locale).
 * The <tt>language</tt> parameter is the lowercase two-letter ISO-639 code.
 * An additional <tt>country</tt> and <tt>variant</tt> can be provided.
 * The second option is to specify collation rules as defined in the
 * <a href="http://www.icu-project.org/userguide/Collate_Customization.html">
 * Collation customization</a> chapter in icu docs. The <tt>rules</tt> parameter can either
 * embed the rules definition in the settings or refer to an external location
 * (preferable located under the <tt>config</tt> location, relative to it).
 */
public class IcuCollationKeyAnalyzerProvider extends AbstractIndexAnalyzerProvider<IcuCollationKeyAnalyzer> {

    private final Collator collator;

    public IcuCollationKeyAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        this.collator = createCollator(settings);
    }

    public static Collator createCollator(Settings settings) {
        Collator collator;
        String rules = settings.get("rules");
        if (rules != null) {
            try {
                collator = new RuleBasedCollator(rules);
            } catch (Exception e) {
                throw new ElasticsearchException("Failed to parse collation rules", e);
            }
        } else {
            String language = settings.get("language");
            if (language != null) {
                ULocale locale;
                String country = settings.get("country");
                if (country != null) {
                    String variant = settings.get("variant");
                    if (variant != null) {
                        locale = new ULocale(language, country, variant);
                    } else {
                        locale = new ULocale(language, country);
                    }
                } else {
                    locale = new ULocale(language);
                }
                collator = Collator.getInstance(locale);
            } else {
                collator = Collator.getInstance();
            }
        }

        // set the strength flag, otherwise it will be the default.
        String strength = settings.get("strength");
        if (strength != null) {
            if (strength.equalsIgnoreCase("primary")) {
                collator.setStrength(Collator.PRIMARY);
            } else if (strength.equalsIgnoreCase("secondary")) {
                collator.setStrength(Collator.SECONDARY);
            } else if (strength.equalsIgnoreCase("tertiary")) {
                collator.setStrength(Collator.TERTIARY);
            } else if (strength.equalsIgnoreCase("quaternary")) {
                collator.setStrength(Collator.QUATERNARY);
            } else if (strength.equalsIgnoreCase("identical")) {
                collator.setStrength(Collator.IDENTICAL);
            } else {
                throw new ElasticsearchException("Invalid strength: " + strength);
            }
        }

        // set the decomposition flag, otherwise it will be the default.
        String decomposition = settings.get("decomposition");
        if (decomposition != null) {
            if (decomposition.equalsIgnoreCase("no")) {
                collator.setDecomposition(Collator.NO_DECOMPOSITION);
            } else if (decomposition.equalsIgnoreCase("canonical")) {
                collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            } else {
                throw new ElasticsearchException("Invalid decomposition: " + decomposition);
            }
        }

        // expert options: concrete subclasses are always a RuleBasedCollator
        RuleBasedCollator rbc = (RuleBasedCollator) collator;
        String alternate = settings.get("alternate");
        if (alternate != null) {
            if (alternate.equalsIgnoreCase("shifted")) {
                rbc.setAlternateHandlingShifted(true);
            } else if (alternate.equalsIgnoreCase("non-ignorable")) {
                rbc.setAlternateHandlingShifted(false);
            } else {
                throw new ElasticsearchException("Invalid alternate: " + alternate);
            }
        }

        Boolean caseLevel = settings.getAsBoolean("caseLevel", null);
        if (caseLevel != null) {
            rbc.setCaseLevel(caseLevel);
        }

        String caseFirst = settings.get("caseFirst");
        if (caseFirst != null) {
            if (caseFirst.equalsIgnoreCase("lower")) {
                rbc.setLowerCaseFirst(true);
            } else if (caseFirst.equalsIgnoreCase("upper")) {
                rbc.setUpperCaseFirst(true);
            } else {
                throw new ElasticsearchException("invalid caseFirst: " + caseFirst);
            }
        }

        Boolean numeric = settings.getAsBoolean("numeric", null);
        if (numeric != null) {
            rbc.setNumericCollation(numeric);
        }

        int maxVariable = settings.getAsInt("variableTop", Collator.ReorderCodes.DEFAULT);
        rbc.setMaxVariable(maxVariable);
        return collator;
    }

    @Override
    public IcuCollationKeyAnalyzer get() {
        return new IcuCollationKeyAnalyzer(collator);
    }

}
