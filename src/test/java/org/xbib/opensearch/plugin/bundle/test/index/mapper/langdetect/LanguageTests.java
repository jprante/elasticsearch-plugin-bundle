package org.xbib.opensearch.plugin.bundle.test.index.mapper.langdetect;

import org.opensearch.test.OpenSearchTestCase;
import org.xbib.opensearch.plugin.bundle.common.langdetect.Language;

public class LanguageTests extends OpenSearchTestCase {

    public final void testLanguage() {
        Language lang = new Language(null, 0);
        assertEquals(lang.getLanguage(), null);
        assertEquals(lang.getProbability(), 0.0, 0.0001);
        assertEquals(lang.getLanguage(), null);

        Language lang2 = new Language("en", 1.0);
        assertEquals(lang2.getLanguage(), "en");
        assertEquals(lang2.getProbability(), 1.0, 0.0001);
    }
}
