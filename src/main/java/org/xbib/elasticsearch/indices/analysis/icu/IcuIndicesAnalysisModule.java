
package org.xbib.elasticsearch.indices.analysis.icu;

import org.elasticsearch.common.inject.AbstractModule;

public class IcuIndicesAnalysisModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IcuIndicesAnalysis.class).asEagerSingleton();
    }
}
