package org.xbib.elasticsearch.index.analysis.combo;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

public class ComboAnalyzerProvider extends AbstractIndexAnalyzerProvider<ComboAnalyzerWrapper> {

    private final Injector injector;

    private final Settings settings;

    private final String name;

    @Inject
    ComboAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings, Injector injector) {
        super(index, indexSettings, name, settings);
        this.injector = injector;
        this.settings = settings;
        this.name = name;
    }

    @Override
    public ComboAnalyzerWrapper get() {
        return new ComboAnalyzerWrapper(version, name, settings, injector);
    }

}
