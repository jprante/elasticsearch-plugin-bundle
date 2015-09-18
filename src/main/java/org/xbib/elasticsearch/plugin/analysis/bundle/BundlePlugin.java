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
package org.xbib.elasticsearch.plugin.analysis.bundle;

import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.Plugin;
import org.xbib.elasticsearch.index.analysis.baseform.BaseformAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.concat.ConcatAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.german.GermanAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.icu.IcuAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.symbolname.SymbolnameAnalysisBinderProcessor;
import org.xbib.elasticsearch.module.langdetect.LangdetectModule;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;
import org.xbib.elasticsearch.index.analysis.sortform.SortformAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardnumberAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WorddelimiterAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.year.GregorianYearAnalysisBinderProcessor;
import org.xbib.elasticsearch.module.reference.ReferenceModule;
import org.xbib.elasticsearch.module.standardnumber.StandardnumberIndexModule;
import org.xbib.elasticsearch.module.crypt.CryptModule;

import java.util.ArrayList;
import java.util.Collection;

public class BundlePlugin extends Plugin {

    private final Settings settings;

    @Inject
    public BundlePlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "plugin-bundle";
    }

    @Override
    public String description() {
        return "A collection of useful plugins";
    }

    /**
     * Automatically called with the analysis module.
     * @param module the analysis module
     */
    public void onModule(AnalysisModule module) {
        if (settings.getAsBoolean("plugins.baseform.enabled", true)) {
            module.addProcessor(new BaseformAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.concat.enabled", true)) {
            module.addProcessor(new ConcatAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.decompound.enabled", true)) {
            module.addProcessor(new DecompoundAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.german.enabled", true)) {
            module.addProcessor(new GermanAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.hyphen.enabled", true)) {
            module.addProcessor(new HyphenAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.icu.enabled", true)) {
            module.addProcessor(new IcuAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.sortform.enabled", true)) {
            module.addProcessor(new SortformAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.standardnumber.enabled", true)) {
            module.addProcessor(new StandardnumberAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.worddelimiter.enabled", true)) {
            module.addProcessor(new WorddelimiterAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.year.enabled", true)) {
            module.addProcessor(new GregorianYearAnalysisBinderProcessor());
        }
        if (settings.getAsBoolean("plugins.symbolname.enabled", true)) {
            module.addProcessor(new SymbolnameAnalysisBinderProcessor());
        }
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> nodeServices() {
        Collection<Class<? extends LifecycleComponent>> services = new ArrayList<>();
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            services.add(LangdetectService.class);
        }
        return services;
    }

    @Override
    public Collection<Module> indexModules(Settings indexSettings) {
        Collection<Module> modules = new ArrayList<>();
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            modules.add(new LangdetectModule());
        }
        if (settings.getAsBoolean("plugins.reference.enabled", true)) {
            modules.add(new ReferenceModule());
        }
        if (settings.getAsBoolean("plugins.standardnumber.enabled", true)) {
            modules.add(new StandardnumberIndexModule());
        }
        if (settings.getAsBoolean("plugins.crypt.enabled", true)) {
            modules.add(new CryptModule());
        }
        return modules;
    }

}
