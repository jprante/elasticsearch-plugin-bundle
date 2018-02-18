package org.xbib.elasticsearch.index.analysis.keyword;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PatternKeywordMarkerFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.Analysis;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A factory for creating keyword marker token filters that prevent tokens from
 * being modified by stemmers.  Two types of keyword marker filters are available:
 * the {@link SetKeywordMarkerFilter} and the {@link PatternKeywordMarkerFilter}.
 *
 * The {@link SetKeywordMarkerFilter} uses a set of keywords to denote which tokens
 * should be excluded from stemming.  This filter is created if the settings include
 * {@code keywords}, which contains the list of keywords, or {@code `keywords_path`},
 * which contains a path to a file in the config directory with the keywords.
 *
 * The {@link PatternKeywordMarkerFilter} uses a regular expression pattern to match
 * against tokens that should be excluded from stemming.  This filter is created if
 * the settings include {@code keywords_pattern}, which contains the regular expression
 * to match against.
 */
public class KeywordMarkerTokenFilterFactory extends AbstractTokenFilterFactory {

    private final CharArraySet keywordLookup;
    private final Pattern keywordPattern;

    public KeywordMarkerTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, name, settings);

        boolean ignoreCase =
            settings.getAsBoolean("ignore_case", false);
        String patternString = settings.get("keywords_pattern");
        if (patternString != null) {
            // a pattern for matching keywords is specified, as opposed to a
            // set of keyword strings to match against
            if (settings.get("keywords") != null || settings.get("keywords_path") != null) {
                throw new IllegalArgumentException(
                    "cannot specify both `keywords_pattern` and `keywords` or `keywords_path`");
            }
            keywordPattern = Pattern.compile(patternString);
            keywordLookup = null;
        } else {
            List<String> rules = Analysis.getWordList(env, settings, "keywords");
            if (rules != null) {
                // a list of keywords (or a path to them) is specified
                keywordLookup = new CharArraySet(rules, ignoreCase);
                keywordPattern = null;
            } else {
                // empty if nothing is configured.
                keywordLookup = new CharArraySet(Collections.emptyList(), ignoreCase);
                keywordPattern = null;
            }
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        if (keywordPattern != null) {
            return new PatternKeywordMarkerFilter(tokenStream, keywordPattern);
        } else {
            return new SetKeywordMarkerFilter(tokenStream, keywordLookup);
        }
    }

}
