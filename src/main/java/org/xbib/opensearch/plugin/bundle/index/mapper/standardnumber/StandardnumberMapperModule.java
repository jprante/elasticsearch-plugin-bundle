package org.xbib.opensearch.plugin.bundle.index.mapper.standardnumber;

import org.opensearch.common.inject.AbstractModule;
import org.xbib.opensearch.plugin.bundle.common.standardnumber.StandardnumberService;

/**
 * Standard number field mapper module.
 */
public class StandardnumberMapperModule extends AbstractModule {

    private final StandardnumberMapperTypeParser typeParser;

    public StandardnumberMapperModule(StandardnumberMapperTypeParser typeParser) {
        this.typeParser = typeParser;
    }

    @Override
    protected void configure() {
        bind(StandardnumberService.class).asEagerSingleton();
        bind(StandardnumberMapperTypeParser.class).toInstance(typeParser);
    }

}
