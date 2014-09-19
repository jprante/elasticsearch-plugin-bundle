/*
 * Copyright (C) 2014 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.elasticsearch.index.analysis.langdetect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class LangdetectService extends AbstractLifecycleComponent<LangdetectService> {

    private final static Pattern word = Pattern.compile("[\\P{IsWord}]", Pattern.UNICODE_CHARACTER_CLASS);

    private final static String[] DEFAULT_LANGUAGES = new String[] {
            "af",
            "ar",
            "bg",
            "bn",
            "cs",
            "da",
            "de",
            "el",
            "en",
            "es",
            "et",
            "fa",
            "fi",
            "fr",
            "gu",
            "he",
            "hi",
            "hr",
            "hu",
            "id",
            "it",
            "ja",
            "kn",
            "ko",
            "lt",
            "lv",
            "mk",
            "ml",
            "mr",
            "ne",
            "nl",
            "no",
            "pa",
            "pl",
            "pt",
            "ro",
            "ru",
            "sk",
            "sl",
            "so",
            "sq",
            "sv",
            "sw",
            "ta",
            "te",
            "th",
            "tl",
            "tr",
            "uk",
            "ur",
            "vi",
            "zh-cn",
            "zh-tw"
    };

    private Map<String, double[]> wordLangProbMap = new HashMap<String, double[]>();

    private List<String> langlist = new LinkedList<String>();

    private Map<String,String> langmap = new HashMap<String,String>();

    private double alpha;

    private double alpha_width;

    private int n_trial;

    private double[] priorMap;

    private int iteration_limit;

    private double prob_threshold;

    private double conv_threshold;

    private int base_freq;

    private Pattern filterPattern;

    @Inject
    public LangdetectService(Settings settings) {
        super(settings);
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        load(settings);
        this.priorMap = null;
        this.n_trial = settings.getAsInt("number_of_trials", 7);
        this.alpha = settings.getAsDouble("alpha", 0.5);
        this.alpha_width = settings.getAsDouble("alpha_width", 0.05);
        this.iteration_limit = settings.getAsInt("iteration_limit", 10000);
        this.prob_threshold = settings.getAsDouble("prob_threshold", 0.1);
        this.conv_threshold = settings.getAsDouble("conv_threshold",  0.99999);
        this.base_freq = settings.getAsInt("base_freq", 10000);
        this.filterPattern = settings.get("pattern") != null ?
                Pattern.compile(settings.get("pattern"),Pattern.UNICODE_CHARACTER_CLASS) : null;
    }

    @Override
    protected void doStop() throws ElasticsearchException {
    }

    @Override
    protected void doClose() throws ElasticsearchException {
    }

    public Settings getSettings() {
        return settings;
    }

    private void load(Settings settings) {
        try {
            String[] keys = settings.getAsArray("languages");
            if (keys == null || keys.length == 0) {
                keys = DEFAULT_LANGUAGES;
            }
            int index = 0;
            int size = keys.length;
            for (String key : keys) {
                loadProfileFromResource(key, index++, size);
            }
            logger.debug("language detection service installed for {}", langlist);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ElasticsearchException(e.getMessage());
        }
        try {
            // map by settings
            Settings map = ImmutableSettings.EMPTY;
            if (settings.getByPrefix("map.") != null) {
                map = ImmutableSettings.settingsBuilder().put(settings.getByPrefix("map.")).build();
            }
            if (map.getAsMap().isEmpty()) {
                // is in "map" a resource name?
                String s = settings.get("map") != null ? settings.get("map") : "/langdetect/language.json";
                InputStream in = getClass().getResourceAsStream(s);
                if (in != null) {
                    map = ImmutableSettings.settingsBuilder().loadFromStream(s, in).build();
                }
            }
            this.langmap = map.getAsMap();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ElasticsearchException(e.getMessage());
        }
    }

    public void loadProfileFromResource(String resource, int index, int langsize) throws IOException {
        InputStream in = getClass().getResourceAsStream("/langdetect/" + resource);
        if (in == null) {
            throw new IOException("profile '" + resource + "' not found");
        }
        ObjectMapper mapper = new ObjectMapper();
        LangProfile profile = mapper.readValue(in, LangProfile.class);
        addProfile(profile, index, langsize);
    }

    public void addProfile(LangProfile profile, int index, int langsize) throws IOException {
        String lang = profile.getName();
        if (langlist.contains(lang)) {
            throw new IOException("duplicate of the same language profile: " + lang);
        }
        langlist.add(lang);
        for (String word : profile.getFreq().keySet()) {
            if (!wordLangProbMap.containsKey(word)) {
                wordLangProbMap.put(word, new double[langsize]);
            }
            int length = word.length();
            if (length >= 1 && length <= 3) {
                double prob = profile.getFreq().get(word).doubleValue() / profile.getNWords()[length - 1];
                wordLangProbMap.get(word)[index] = prob;
            }
        }
    }

    /**
     * Set prior information about language probabilities.
     *
     * @param priorMap the priorMap to set
     * @throws LanguageDetectionException
     */
    public void setPriorMap(HashMap<String, Double> priorMap) throws LanguageDetectionException {
        this.priorMap = new double[langlist.size()];
        double sump = 0;
        for (int i = 0; i < this.priorMap.length; ++i) {
            String lang = langlist.get(i);
            if (priorMap.containsKey(lang)) {
                double p = priorMap.get(lang);
                if (p < 0) {
                    throw new LanguageDetectionException("Prior probability must be non-negative");
                }
                this.priorMap[i] = p;
                sump += p;
            }
        }
        if (sump <= 0) {
            throw new LanguageDetectionException("More one of prior probability must be non-zero");
        }
        for (int i = 0; i < this.priorMap.length; ++i) {
            this.priorMap[i] /= sump;
        }
    }

    public List<Language> detectAll(String text) throws LanguageDetectionException {
        List<Language> languages = new ArrayList<Language>();
        if (filterPattern != null && !filterPattern.matcher(text).matches()) {
            return languages;
        }
        List<String> list = new ArrayList<String>();
        languages = sortProbability(languages, detectBlock(list, text));
        return languages.subList(0, Math.min(languages.size(), settings.getAsInt("max", languages.size())));
    }

    private double[] detectBlock(List<String> list, String text) throws LanguageDetectionException {
        // clean all non-work characters from text
        text = text.replaceAll(word.pattern(), " ");
        extractNGrams(list, text);
        if (list.isEmpty()) {
            throw new LanguageDetectionException("no features in text");
        }
        double[] langprob = new double[langlist.size()];
        Random rand = new Random();
        Long seed = 0L;
        rand.setSeed(seed);
        for (int t = 0; t < n_trial; ++t) {
            double[] prob = initProbability();
            double a = this.alpha + rand.nextGaussian() * alpha_width;
            for (int i = 0; ; ++i) {
                int r = rand.nextInt(list.size());
                updateLangProb(prob, list.get(r), a);
                if (i % 5 == 0) {
                    if (normalizeProb(prob) > conv_threshold || i >= iteration_limit) {
                        break;
                    }
                }
            }
            for (int j = 0; j < langprob.length; ++j) {
                langprob[j] += prob[j] / n_trial;
            }
        }
        return langprob;
    }

    private double[] initProbability() {
        double[] prob = new double[langlist.size()];
        if (priorMap != null) {
            System.arraycopy(priorMap, 0, prob, 0, prob.length);
        } else {
            for (int i = 0; i < prob.length; ++i) {
                prob[i] = 1.0 / langlist.size();
            }
        }
        return prob;
    }

    private void extractNGrams(List<String> list, String text) {
        NGram ngram = new NGram();
        for (int i = 0; i < text.length(); ++i) {
            ngram.addChar(text.charAt(i));
            for (int n = 1; n <= NGram.N_GRAM; ++n) {
                String w = ngram.get(n);
                if (w != null && wordLangProbMap.containsKey(w)) {
                    list.add(w);
                }
            }
        }
    }

    private boolean updateLangProb(double[] prob, String word, double alpha) {
        if (word == null || !wordLangProbMap.containsKey(word)) {
            return false;
        }
        double[] langProbMap = wordLangProbMap.get(word);
        double weight = alpha / base_freq;
        for (int i = 0; i < prob.length; ++i) {
            prob[i] *= weight + langProbMap[i];
        }
        return true;
    }

    private double normalizeProb(double[] prob) {
        double maxp = 0, sump = 0;
        for (double aProb : prob) {
            sump += aProb;
        }
        for (int i = 0; i < prob.length; ++i) {
            double p = prob[i] / sump;
            if (maxp < p) {
                maxp = p;
            }
            prob[i] = p;
        }
        return maxp;
    }

    private List<Language> sortProbability(List<Language> list, double[] prob) {
        for (int j = 0; j < prob.length; ++j) {
            double p = prob[j];
            if (p > prob_threshold) {
                for (int i = 0; i <= list.size(); ++i) {
                    if (i == list.size() || list.get(i).getProbability() < p) {
                        String code = langlist.get(j);
                        if (langmap != null && langmap.containsKey(code)) {
                            code = langmap.get(code);
                        }
                        list.add(i, new Language(code, p));
                        break;
                    }
                }
            }
        }
        return list;
    }

}
