package org.xbib.elasticsearch.plugin.analysis.german;

import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.xbib.elasticsearch.index.analysis.baseform.BaseformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.combo.ComboAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.concat.ConcatTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.german.GermanNormalizationFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.langdetect.LangdetectModule;
import org.xbib.elasticsearch.index.analysis.langdetect.LangdetectService;
import org.xbib.elasticsearch.index.analysis.sortform.SortformAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.sortform.SortformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilter2Factory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilterFactory;
import org.xbib.elasticsearch.index.analysis.year.GregorianYearTokenFilterFactory;
import org.xbib.elasticsearch.indices.analysis.icu.IcuIndicesAnalysisModule;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.newArrayList;

public class AnalysisGermanPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "analysis-german-" +
                Build.getInstance().getVersion() + "-" +
                Build.getInstance().getShortHash();
    }

    @Override
    public String description() {
        return "German language related analysis support";
    }

    @Override
    public Collection<Class<? extends Module>> modules() {
        return ImmutableList.<Class<? extends Module>>of(IcuIndicesAnalysisModule.class);
    }

    /**
     * Automatically called with the analysis module.
     */
    public void onModule(AnalysisModule module) {
        module.addProcessor(new IcuAnalysisBinderProcessor());
        module.addProcessor(new ComboAnalysisBinderProcessor());
        module.addAnalyzer("sortform", SortformAnalyzerProvider.class);
        module.addTokenFilter("german_normalize", GermanNormalizationFilterFactory.class);
        module.addTokenFilter("decompound", DecompoundTokenFilterFactory.class);
        module.addTokenFilter("baseform", BaseformTokenFilterFactory.class);
        module.addTokenFilter("worddelimiter", WordDelimiterFilterFactory.class);
        module.addTokenFilter("worddelimiter2", WordDelimiterFilter2Factory.class);
        module.addTokenFilter("sortform", SortformTokenFilterFactory.class);
        module.addTokenFilter("concat", ConcatTokenFilterFactory.class);
        module.addTokenFilter("year", GregorianYearTokenFilterFactory.class);
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        services.add(LangdetectService.class);
        return services;
    }

    @Override
    public Collection<Class<? extends Module>> indexModules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        modules.add(LangdetectModule.class);
        return modules;
    }

}
