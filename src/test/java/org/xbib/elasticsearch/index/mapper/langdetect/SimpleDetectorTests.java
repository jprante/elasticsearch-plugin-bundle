package org.xbib.elasticsearch.index.mapper.langdetect;

import org.elasticsearch.test.ESTestCase;
import org.xbib.elasticsearch.common.langdetect.LangdetectService;

public class SimpleDetectorTests extends ESTestCase {

    public void testDetector() throws Exception {
        LangdetectService detect = new LangdetectService();
        assertEquals("de", detect.detectAll("Das kann deutsch sein").get(0).getLanguage());
        assertEquals("en", detect.detectAll("This is a very small test").get(0).getLanguage());
    }
}
