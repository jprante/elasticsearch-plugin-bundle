package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;
import com.ibm.icu.util.ULocale;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.collation.ICUCollationAttributeFactory;
import org.apache.lucene.collation.tokenattributes.ICUCollatedTermAttributeImpl;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.FailedToResolveConfigException;

import java.io.IOException;
import java.io.Reader;

public class IcuCollationTokenizer extends Tokenizer {

    private final TokenStream tokenStream;

    protected IcuCollationTokenizer(Environment environment, Settings settings, Reader input) {
        super(new ICUCollationAttributeFactory(makeCollator(environment, settings)), input);
        this.tokenStream = new KeywordTokenizer(input);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (tokenStream.incrementToken()) {
            CharTermAttribute termAtt = getAttribute(CharTermAttribute.class);
            ICUCollatedTermAttributeImpl icutermAtt = getAttribute(ICUCollatedTermAttributeImpl.class);
            return true;
        } else {
            return false;
        }
    }

    public void reset() throws IOException {
        super.reset();
        tokenStream.reset();
    }

    public void close() throws IOException {
        super.close();
        tokenStream.close();
    }

    private static Collator makeCollator(Environment environment, Settings settings) {
        Collator collator;
        String rules = settings.get("rules");
        if (rules != null) {
            FailedToResolveConfigException failureToResolve = null;
            try {
                rules = environment.resolveConfigAndLoadToString(rules);
            } catch (FailedToResolveConfigException e) {
                failureToResolve = e;
            } catch (IOException e) {
                throw new ElasticsearchIllegalArgumentException("Failed to load collation rules", e);
            }
            try {
                collator = new RuleBasedCollator(rules);
            } catch (Exception e) {
                if (failureToResolve != null) {
                    throw new ElasticsearchIllegalArgumentException("Failed to resolve collation rules location", failureToResolve);
                } else {
                    throw new ElasticsearchIllegalArgumentException("Failed to parse collation rules", e);
                }
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
                System.err.println("locale=" + locale);
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
                throw new ElasticsearchIllegalArgumentException("Invalid strength: " + strength);
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
                throw new ElasticsearchIllegalArgumentException("Invalid decomposition: " + decomposition);
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
                throw new ElasticsearchIllegalArgumentException("Invalid alternate: " + alternate);
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
                throw new ElasticsearchIllegalArgumentException("Invalid caseFirst: " + caseFirst);
            }
        }

        Boolean numeric = settings.getAsBoolean("numeric", null);
        if (numeric != null) {
            rbc.setNumericCollation(numeric);
        }

        int maxVariable = settings.getAsInt("variableTop", Collator.ReorderCodes.DEFAULT);
        rbc.setMaxVariable(maxVariable);
        System.err.println("collator created " + collator);
        return collator;
    }

}
