package org.xbib.elasticsearch.index.analysis.icu;

import com.ibm.icu.text.Transliterator;
import com.ibm.icu.text.UnicodeSet;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 * ICU transform token filter factory.
 */
public class IcuTransformTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Transliterator transliterator;

    public IcuTransformTokenFilterFactory(IndexSettings indexSettings, Environment environment, String name,
                                          Settings settings) {
        super(indexSettings, name, settings);
        String id = settings.get("id", "Null");
        String direction = settings.get("dir", "forward");
        int dir = "forward".equals(direction) ? Transliterator.FORWARD : Transliterator.REVERSE;
        String rules = settings.get("rules");
        this.transliterator = rules != null ?
                Transliterator.createFromRules(id, rules, dir) :
                Transliterator.getInstance(id, dir);
        String unicodeSetFilter = settings.get("unicodeSetFilter");
        if (unicodeSetFilter != null) {
            transliterator.setFilter(new UnicodeSet(unicodeSetFilter).freeze());
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new IcuTransformTokenFilter(tokenStream, transliterator);
    }
}
