package org.xbib.opensearch.plugin.bundle.test.index.mapper.langdetect;

import org.opensearch.common.io.Streams;
import org.opensearch.test.OpenSearchTestCase;
import org.xbib.opensearch.plugin.bundle.common.langdetect.LangdetectService;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class DetectLanguageTests extends OpenSearchTestCase {

    public void testEnglish() throws Exception {
        testLanguage("english.txt", "en");
    }

    public void testChinese() throws Exception {
        testLanguage("chinese.txt", "zh-cn");
    }

    public void testJapanese() throws Exception {
        testLanguage("japanese.txt", "ja");
    }

    public void testKorean() throws Exception {
        testLanguage("korean.txt", "ko");
    }

    private void testLanguage(String path, String lang) throws Exception {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream(path), StandardCharsets.UTF_8);
        Writer writer = new StringWriter();
        Streams.copy(reader, writer);
        reader.close();
        writer.close();
        LangdetectService detect = new LangdetectService();
        assertEquals(lang, detect.detectAll(writer.toString()).get(0).getLanguage());
    }

}
