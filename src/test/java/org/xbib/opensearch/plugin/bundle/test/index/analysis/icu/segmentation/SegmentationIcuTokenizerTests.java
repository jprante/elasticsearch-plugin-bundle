package org.xbib.opensearch.plugin.bundle.test.index.analysis.icu.segmentation;

import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.Normalizer2;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.opensearch.test.OpenSearchTokenStreamTestCase;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuNormalizerFilter;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.DefaultIcuTokenizerConfig;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizer;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.tokenattributes.ScriptAttribute;

import java.io.StringReader;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * ICU tokenizer tests.
 */
public class SegmentationIcuTokenizerTests extends OpenSearchTokenStreamTestCase {

    private static Analyzer createAnalyzer() {
        return new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                        new DefaultIcuTokenizerConfig(false, true));
                TokenFilter filter = new IcuNormalizerFilter(tokenizer,
                        Normalizer2.getInstance(null, "nfkc_cf", Normalizer2.Mode.COMPOSE));
                return new TokenStreamComponents(tokenizer, filter);
            }
        };
    }

    private static void destroyAnalzyer(Analyzer a) {
        a.close();
    }

    public void testHugeDoc() throws Exception {
        StringBuilder sb = new StringBuilder();
        char whitespace[] = new char[4094];
        Arrays.fill(whitespace, ' ');
        sb.append(whitespace);
        sb.append("testing 1234");
        String input = sb.toString();
        IcuTokenizer tokenizer = new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                new DefaultIcuTokenizerConfig(false, true));
        tokenizer.setReader(new StringReader(input));
        assertTokenStreamContents(tokenizer, new String[] { "testing", "1234" });
    }

    public void testHugeTerm2() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40960; i++) {
            sb.append('a');
        }
        String input = sb.toString();
        IcuTokenizer tokenizer = new IcuTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY,
                new DefaultIcuTokenizerConfig(false, true));
        tokenizer.setReader(new StringReader(input));
        char token[] = new char[4096];
        Arrays.fill(token, 'a');
        String expectedToken = new String(token);
        String expected[] = {
                expectedToken, expectedToken, expectedToken,
                expectedToken, expectedToken, expectedToken,
                expectedToken, expectedToken, expectedToken,
                expectedToken
        };
        assertTokenStreamContents(tokenizer, expected);
    }

    public void testArmenian() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "Վիքիպեդիայի 13 միլիոն հոդվածները (4,600` հայերեն վիքիպեդիայում) գրվել են կամավորների կողմից" +
                        " ու համարյա բոլոր հոդվածները կարող է խմբագրել ցանկաց մարդ ով կարող է բացել Վիքիպեդիայի կայքը։",
                new String[] { "վիքիպեդիայի", "13", "միլիոն", "հոդվածները", "4,600", "հայերեն", "վիքիպեդիայում", "գրվել",
                        "են", "կամավորների", "կողմից",
                        "ու", "համարյա", "բոլոր", "հոդվածները", "կարող", "է", "խմբագրել", "ցանկաց", "մարդ", "ով", "կարող",
                        "է", "բացել", "վիքիպեդիայի", "կայքը" } );
        destroyAnalzyer(a);
    }

    public void testAmharic() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ዊኪፔድያ የባለ ብዙ ቋንቋ የተሟላ ትክክለኛና ነጻ መዝገበ ዕውቀት (ኢንሳይክሎፒዲያ) ነው። ማንኛውም",
                new String[] { "ዊኪፔድያ", "የባለ", "ብዙ", "ቋንቋ", "የተሟላ", "ትክክለኛና", "ነጻ", "መዝገበ", "ዕውቀት",
                        "ኢንሳይክሎፒዲያ", "ነው", "ማንኛውም" } );
        destroyAnalzyer(a);
    }

    public void testArabic() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "الفيلم الوثائقي الأول عن ويكيبيديا يسمى \"الحقيقة بالأرقام: قصة " +
                        "ويكيبيديا\" (بالإنجليزية: Truth in Numbers: The Wikipedia Story)، سيتم إطلاقه في 2008.",
                new String[] { "الفيلم", "الوثائقي", "الأول", "عن", "ويكيبيديا", "يسمى", "الحقيقة", "بالأرقام", "قصة", "ويكيبيديا",
                        "بالإنجليزية", "truth", "in", "numbers", "the", "wikipedia", "story", "سيتم", "إطلاقه", "في", "2008" } );
        destroyAnalzyer(a);
    }

    public void testAramaic() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ܘܝܩܝܦܕܝܐ (ܐܢܓܠܝܐ: Wikipedia)" +
                        " ܗܘ ܐܝܢܣܩܠܘܦܕܝܐ ܚܐܪܬܐ ܕܐܢܛܪܢܛ ܒܠܫܢ̈ܐ ܣܓܝܐ̈ܐ܂ ܫܡܗ ܐܬܐ ܡܢ ܡ̈ܠܬܐ ܕ\"ܘܝܩܝ\" ܘ\"ܐܝܢܣܩܠܘܦܕܝܐ\"܀",
                new String[] { "ܘܝܩܝܦܕܝܐ", "ܐܢܓܠܝܐ", "wikipedia", "ܗܘ", "ܐܝܢܣܩܠܘܦܕܝܐ", "ܚܐܪܬܐ", "ܕܐܢܛܪܢܛ", "ܒܠܫܢ̈ܐ", "ܣܓܝܐ̈ܐ", "ܫܡܗ",
                        "ܐܬܐ", "ܡܢ", "ܡ̈ܠܬܐ", "ܕ", "ܘܝܩܝ", "ܘ", "ܐܝܢܣܩܠܘܦܕܝܐ"});
        destroyAnalzyer(a);
    }

    public void testBengali() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "এই বিশ্বকোষ পরিচালনা করে উইকিমিডিয়া ফাউন্ডেশন (একটি অলাভজনক সংস্থা)। উইকিপিডিয়ার শুরু ১৫ " +
                        "জানুয়ারি, ২০০১ সালে। এখন পর্যন্ত ২০০টিরও বেশী ভাষায় উইকিপিডিয়া রয়েছে।",
                new String[] { "এই", "বিশ্বকোষ", "পরিচালনা", "করে", "উইকিমিডিয়া", "ফাউন্ডেশন", "একটি", "অলাভজনক",
                        "সংস্থা", "উইকিপিডিয়ার", "শুরু", "১৫", "জানুয়ারি", "২০০১", "সালে", "এখন", "পর্যন্ত", "২০০টিরও",
                        "বেশী", "ভাষায়", "উইকিপিডিয়া", "রয়েছে" });
        destroyAnalzyer(a);
    }

    public void testFarsi() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ویکی پدیای انگلیسی در تاریخ ۲۵ دی ۱۳۷۹ به صورت مکملی برای دانشنامهٔ تخصصی نوپدیا نوشته شد.",
                new String[] { "ویکی", "پدیای", "انگلیسی", "در", "تاریخ", "۲۵", "دی", "۱۳۷۹", "به", "صورت", "مکملی",
                        "برای", "دانشنامهٔ", "تخصصی", "نوپدیا", "نوشته", "شد" });
        destroyAnalzyer(a);
    }

    public void testGreek() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "Γράφεται σε συνεργασία από εθελοντές με το λογισμικό wiki, κάτι που σημαίνει ότι" +
                        " άρθρα μπορεί να προστεθούν ή να αλλάξουν από τον καθένα.",
                new String[] { "γράφεται", "σε", "συνεργασία", "από", "εθελοντέσ", "με", "το", "λογισμικό", "wiki", "κάτι", "που",
                        "σημαίνει", "ότι", "άρθρα", "μπορεί", "να", "προστεθούν", "ή", "να", "αλλάξουν", "από", "τον", "καθένα" });
        destroyAnalzyer(a);
    }

    public void testKhmer() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ផ្ទះស្កឹមស្កៃបីបួនខ្នងនេះ", new String[] { "ផ្ទះ", "ស្កឹមស្កៃ", "បី", "បួន", "ខ្នង", "នេះ" });
        destroyAnalzyer(a);
    }

    public void testLao() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "ກວ່າດອກ", new String[] { "ກວ່າ", "ດອກ" });
        assertAnalyzesTo(a, "ພາສາລາວ", new String[] { "ພາສາ", "ລາວ"}, new String[] { "<ALPHANUM>", "<ALPHANUM>" });
        destroyAnalzyer(a);
    }

    public void testMyanmar() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "သက်ဝင်လှုပ်ရှားစေပြီး", new String[] { "သက်ဝင်", "လှုပ်ရှား", "စေ", "ပြီး" });
        destroyAnalzyer(a);
    }

    public void testThai() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "การที่ได้ต้องแสดงว่างานดี. แล้วเธอจะไปไหน? ๑๒๓๔",
                new String[] { "การ", "ที่", "ได้", "ต้อง", "แสดง", "ว่า", "งาน", "ดี", "แล้ว", "เธอ", "จะ", "ไป", "ไหน", "๑๒๓๔"});
        destroyAnalzyer(a);
    }

    public void testTibetan() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "སྣོན་མཛོད་དང་ལས་འདིས་བོད་ཡིག་མི་ཉམས་གོང་འཕེལ་དུ་གཏོང་བར་ཧ་ཅང་དགེ་མཚན་མཆིས་སོ། །",
                new String[] { "སྣོན", "མཛོད", "དང", "ལས", "འདིས", "བོད", "ཡིག", "མི", "ཉམས", "གོང", "འཕེལ", "དུ", "གཏོང", "བར", "ཧ",
                        "ཅང", "དགེ", "མཚན", "མཆིས", "སོ" });
        destroyAnalzyer(a);
    }

    public void testChinese() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "我是中国人。 １２３４ Ｔｅｓｔｓ ",
                new String[] { "我", "是", "中", "国", "人", "1234", "tests"});
        destroyAnalzyer(a);
    }

    public void testHebrew() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "דנקנר תקף את הדו\"ח",
                new String[] { "דנקנר", "תקף", "את", "הדו\"ח" });
        assertAnalyzesTo(a, "חברת בת של מודי'ס",
                new String[] { "חברת", "בת", "של", "מודי'ס" });
        destroyAnalzyer(a);
    }

    public void testEmpty() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "", new String[] {});
        assertAnalyzesTo(a, ".", new String[] {});
        assertAnalyzesTo(a, " ", new String[] {});
        destroyAnalzyer(a);
    }

    public void testLUCENE1545() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "moͤchte", new String[] { "moͤchte" });
        destroyAnalzyer(a);
    }

    public void testAlphanumericSA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "B2B", new String[]{"b2b"});
        assertAnalyzesTo(a, "2B", new String[]{"2b"});
        destroyAnalzyer(a);
    }

    public void testDelimitersSA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "some-dashed-phrase", new String[]{"some", "dashed", "phrase"});
        assertAnalyzesTo(a, "dogs,chase,cats", new String[]{"dogs", "chase", "cats"});
        assertAnalyzesTo(a, "ac/dc", new String[]{"ac", "dc"});
        destroyAnalzyer(a);
    }

    public void testApostrophesSA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "O'Reilly", new String[]{"o'reilly"});
        assertAnalyzesTo(a, "you're", new String[]{"you're"});
        assertAnalyzesTo(a, "she's", new String[]{"she's"});
        assertAnalyzesTo(a, "Jim's", new String[]{"jim's"});
        assertAnalyzesTo(a, "don't", new String[]{"don't"});
        assertAnalyzesTo(a, "O'Reilly's", new String[]{"o'reilly's"});
        destroyAnalzyer(a);
    }

    public void testNumericSA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "21.35", new String[]{"21.35"});
        assertAnalyzesTo(a, "R2D2 C3PO", new String[]{"r2d2", "c3po"});
        assertAnalyzesTo(a, "216.239.63.104", new String[]{"216.239.63.104"});
        assertAnalyzesTo(a, "216.239.63.104", new String[]{"216.239.63.104"});
        destroyAnalzyer(a);
    }

    public void testTextWithNumbersSA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "David has 5000 bones", new String[]{"david", "has", "5000", "bones"});
        destroyAnalzyer(a);
    }

    public void testVariousTextSA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "C embedded developers wanted", new String[]{"c", "embedded", "developers", "wanted"});
        assertAnalyzesTo(a, "foo bar FOO BAR", new String[]{"foo", "bar", "foo", "bar"});
        assertAnalyzesTo(a, "foo      bar .  FOO <> BAR", new String[]{"foo", "bar", "foo", "bar"});
        assertAnalyzesTo(a, "\"QUOTED\" word", new String[]{"quoted", "word"});
        destroyAnalzyer(a);
    }

    public void testKoreanSA() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "안녕하세요 한글입니다", new String[]{"안녕하세요", "한글입니다"});
        destroyAnalzyer(a);
    }

    public void testReusableTokenStream() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "སྣོན་མཛོད་དང་ལས་འདིས་བོད་ཡིག་མི་ཉམས་གོང་འཕེལ་དུ་གཏོང་བར་ཧ་ཅང་དགེ་མཚན་མཆིས་སོ། །",
                new String[] { "སྣོན", "མཛོད", "དང", "ལས", "འདིས", "བོད", "ཡིག", "མི", "ཉམས", "གོང",
                        "འཕེལ", "དུ", "གཏོང", "བར", "ཧ", "ཅང", "དགེ", "མཚན", "མཆིས", "སོ" });
        destroyAnalzyer(a);
    }

    public void testOffsets() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "David has 5000 bones",
                new String[] {"david", "has", "5000", "bones"},
                new int[] {0, 6, 10, 15},
                new int[] {5, 9, 14, 20});
        destroyAnalzyer(a);
    }

    public void testTypes() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "David has 5000 bones",
                new String[] {"david", "has", "5000", "bones"},
                new String[] { "<ALPHANUM>", "<ALPHANUM>", "<NUM>", "<ALPHANUM>" });
        destroyAnalzyer(a);
    }

    public void testKorean() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "훈민정음",
                new String[] { "훈민정음" },
                new String[] { "<HANGUL>" });
        destroyAnalzyer(a);
    }

    public void testJapanese() throws Exception {
        Analyzer a = createAnalyzer();
        assertAnalyzesTo(a, "仮名遣い カタカナ",
                new String[] { "仮", "名", "遣", "い", "カタカナ" },
                new String[] { "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<IDEOGRAPHIC>", "<HIRAGANA>", "<KATAKANA>" });
        destroyAnalzyer(a);
    }

    public void testTokenAttributes() throws Exception {
        Analyzer a = createAnalyzer();
        try (TokenStream ts = a.tokenStream("dummy", "This is a test")) {
            ScriptAttribute scriptAtt = ts.addAttribute(ScriptAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                assertEquals(UScript.LATIN, scriptAtt.getCode());
                assertEquals(UScript.getName(UScript.LATIN), scriptAtt.getName());
                assertEquals(UScript.getShortName(UScript.LATIN), scriptAtt.getShortName());
                assertTrue(ts.reflectAsString(false).contains("script=Latin"));
            }
            ts.end();
        }
        destroyAnalzyer(a);
    }

    public void testICUConcurrency() throws Exception {
        int numThreads = 8;
        final CountDownLatch startingGun = new CountDownLatch(1);
        Thread threads[] = new Thread[numThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                @Override
                public void run() {
                    try {
                        startingGun.await();
                        long tokenCount = 0;
                        final String contents = "英 เบียร์ ビール ເບຍ abc";
                        for (int i = 0; i < 1000; i++) {
                            try (Tokenizer tokenizer = new IcuTokenizer()) {
                                tokenizer.setReader(new StringReader(contents));
                                tokenizer.reset();
                                while (tokenizer.incrementToken()) {
                                    tokenCount++;
                                }
                                tokenizer.end();
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            threads[i].start();
        }
        startingGun.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
