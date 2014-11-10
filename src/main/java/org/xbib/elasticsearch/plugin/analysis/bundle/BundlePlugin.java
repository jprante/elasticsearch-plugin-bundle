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
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.xbib.elasticsearch.index.analysis.baseform.BaseformAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.combo.ComboAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.concat.ConcatAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.german.GermanAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.icu.IcuAnalysisBinderProcessor;
import org.xbib.elasticsearch.module.langdetect.LangdetectModule;
import org.xbib.elasticsearch.module.langdetect.LangdetectService;
import org.xbib.elasticsearch.index.analysis.sortform.SortformAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardNumberAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WorddelimiterAnalysisBinderProcessor;
import org.xbib.elasticsearch.index.analysis.year.GregorianYearAnalysisBinderProcessor;
import org.xbib.elasticsearch.module.reference.ReferenceModule;

import java.util.Collection;

import static org.elasticsearch.common.collect.Lists.newArrayList;

public class BundlePlugin extends AbstractPlugin {

    private final static ESLogger logger = ESLoggerFactory.getLogger(BundlePlugin.class.getSimpleName());

    private final Settings settings;

    @Inject
    public BundlePlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "plugin-bundle-" +
                Build.getInstance().getVersion() + "-" +
                Build.getInstance().getShortHash();
    }

    @Override
    public String description() {
        return "A collection of useful plugins";
    }

    /**
     * Automatically called with the analysis module.
     */
    public void onModule(AnalysisModule module) {
        if (settings.getAsBoolean("plugins.analysis.baseform.enabled", true)) {
            module.addProcessor(new BaseformAnalysisBinderProcessor());
            logger.info("Baseform analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.combo.enabled", true)) {
            module.addProcessor(new ComboAnalysisBinderProcessor());
            logger.info("Combo analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.concat.enabled", true)) {
            module.addProcessor(new ConcatAnalysisBinderProcessor());
            logger.info("Concat analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.decompound.enabled", true)) {
            module.addProcessor(new DecompoundAnalysisBinderProcessor());
            logger.info("Decompound analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.german.enabled", true)) {
            module.addProcessor(new GermanAnalysisBinderProcessor());
            logger.info("german analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.hyphen.enabled", true)) {
            module.addProcessor(new HyphenAnalysisBinderProcessor());
            logger.info("Hyphen analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.icu.enabled", true)) {
            module.addProcessor(new IcuAnalysisBinderProcessor());
            logger.info("ICU analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.sortform.enabled", true)) {
            module.addProcessor(new SortformAnalysisBinderProcessor());
            logger.info("Sortform analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.standardnumber.enabled", true)) {
            module.addProcessor(new StandardNumberAnalysisBinderProcessor());
            logger.info("Standardnumber analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.worddelimiter.enabled", true)) {
            module.addProcessor(new WorddelimiterAnalysisBinderProcessor());
            logger.info("Word delimiter analysis plugin installed");
        }
        if (settings.getAsBoolean("plugins.analysis.year.enabled", true)) {
            module.addProcessor(new GregorianYearAnalysisBinderProcessor());
            logger.info("Gregorian year analysis plugin installed");
        }
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> services() {
        Collection<Class<? extends LifecycleComponent>> services = newArrayList();
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            services.add(LangdetectService.class);
        }
        return services;
    }

    @Override
    public Collection<Class<? extends Module>> indexModules() {
        Collection<Class<? extends Module>> modules = newArrayList();
        if (settings.getAsBoolean("plugins.langdetect.enabled", true)) {
            modules.add(LangdetectModule.class);
            logger.info("Langdetect plugin installed");
        }
        if (settings.getAsBoolean("plugins.reference.enabled", true)) {
            modules.add(ReferenceModule.class);
            logger.info("Referencer plugin installed");
        }
        return modules;
    }

}
