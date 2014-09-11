package org.xbib.elasticsearch.plugin.analysis.german;

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.xbib.elasticsearch.index.analysis.combo.ComboAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.german.GermanAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.icu.IcuAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.langdetect.LangdetectModule;
import org.xbib.elasticsearch.index.analysis.langdetect.LangdetectService;

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

    /**
     * Automatically called with the analysis module.
     */
    public void onModule(AnalysisModule module) {
        module.addProcessor(new ComboAnalysisBinderProcessor());
        module.addProcessor(new IcuAnalysisBinderProcessor());
        module.addProcessor(new GermanAnalysisBinderProcessor());
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
