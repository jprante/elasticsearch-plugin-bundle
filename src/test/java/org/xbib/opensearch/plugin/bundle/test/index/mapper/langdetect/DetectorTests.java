package org.xbib.opensearch.plugin.bundle.test.index.mapper.langdetect;

import org.opensearch.common.settings.Settings;
import org.opensearch.test.OpenSearchTestCase;
import org.xbib.opensearch.plugin.bundle.common.langdetect.LangProfile;
import org.xbib.opensearch.plugin.bundle.common.langdetect.LangdetectService;

/**
 * Detector test.
 */
public class DetectorTests extends OpenSearchTestCase {

    private static final String TRAINING_EN = "a a a b b c c d e";

    private static final String TRAINING_FR = "a b b c c c d d d";

    private static final String TRAINING_JA = "\u3042 \u3042 \u3042 \u3044 \u3046 \u3048 \u3048";

    public static LangdetectService create() throws Exception {
        LangdetectService detect = new LangdetectService(Settings.EMPTY);
        LangProfile profile_en = new LangProfile();
        profile_en.setName("en_test");
        for (String w : TRAINING_EN.split(" ")) {
            profile_en.add(w);
        }
        detect.addProfile(profile_en, 0, 3);
        LangProfile profile_fr = new LangProfile();
        profile_fr.setName("fr_test");
        for (String w : TRAINING_FR.split(" ")) {
            profile_fr.add(w);
        }
        detect.addProfile(profile_fr, 1, 3);
        LangProfile profile_ja = new LangProfile();
        profile_ja.setName("ja_test");
        for (String w : TRAINING_JA.split(" ")) {
            profile_ja.add(w);
        }
        detect.addProfile(profile_ja, 2, 3);
        return detect;
    }

    public void testDetector1() throws Exception {
        LangdetectService detect = create();
        assertEquals(detect.detectAll("a").get(0).getLanguage(), "en_test");
    }

    public void testDetector2() throws Exception {
        LangdetectService detect = create();
        assertEquals(detect.detectAll("b d").get(0).getLanguage(), "fr_test");
    }

    public void testDetector3() throws Exception {
        LangdetectService detect = create();
        assertEquals(detect.detectAll("d e").get(0).getLanguage(), "en_test");
    }

    public void testDetector4() throws Exception {
        LangdetectService detect = create();
        assertEquals(detect.detectAll("\u3042\u3042\u3042\u3042a").get(0).getLanguage(), "ja_test");
    }

    public void testPunctuation() throws Exception {
        LangdetectService detect = create();
        assertTrue(detect.detectAll("...").isEmpty());
    }
}
