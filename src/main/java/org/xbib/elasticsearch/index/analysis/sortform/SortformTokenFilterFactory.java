package org.xbib.elasticsearch.index.analysis.sortform;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.util.List;
import java.util.regex.Pattern;

import static org.elasticsearch.common.collect.Lists.newLinkedList;

public class SortformTokenFilterFactory extends AbstractTokenFilterFactory {

    private final List<Pattern> patterns;

    @Inject
    public SortformTokenFilterFactory(Index index,
                                      @IndexSettings Settings indexSettings,
                                      @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        patterns = newLinkedList();
        String[] patternStrs = settings.getAsArray("pattern");
        if (patternStrs == null || patternStrs.length == 0) {
            // default patterns
            patternStrs = new String[]{
                    "\\s*<<.*?>>\\s*",
                    "\\s*<.*?>\\s*",
                    "\\s*\u0098.*?\u009C\\s*",
                    "\\s*\u02BE.*?\u02BB\\s*",
                    "\\s*\u00AC.*?\u00AC\\s*"
            };
        }
        for (String patternStr : patternStrs) {
            patterns.add(Pattern.compile(patternStr));
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new SortformTokenFilter(tokenStream, patterns);
    }

}
