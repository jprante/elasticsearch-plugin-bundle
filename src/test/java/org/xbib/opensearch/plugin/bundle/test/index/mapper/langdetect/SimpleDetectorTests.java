package org.xbib.opensearch.plugin.bundle.test.index.mapper.langdetect;

import org.opensearch.test.OpenSearchTestCase;
import org.xbib.opensearch.plugin.bundle.common.langdetect.LangdetectService;

public class SimpleDetectorTests extends OpenSearchTestCase {

    public void testDetector() throws Exception {
        LangdetectService detect = new LangdetectService();
        assertEquals("de", detect.detectAll("Das kann deutsch sein").get(0).getLanguage());
        assertEquals("en", detect.detectAll("This is a very small test").get(0).getLanguage());
    }
}
