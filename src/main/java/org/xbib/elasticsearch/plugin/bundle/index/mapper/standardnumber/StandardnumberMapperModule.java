package org.xbib.elasticsearch.plugin.bundle.index.mapper.standardnumber;

import org.elasticsearch.common.inject.AbstractModule;
import org.xbib.elasticsearch.plugin.bundle.common.standardnumber.StandardnumberService;

/**
 * Standard number field mapper module.
 */
public class StandardnumberMapperModule extends AbstractModule {

    private final StandardnumberMapper.TypeParser typeParser;

    public StandardnumberMapperModule(StandardnumberMapper.TypeParser typeParser) {
        this.typeParser = typeParser;
    }

    @Override
    protected void configure() {
        bind(StandardnumberService.class).asEagerSingleton();
        bind(StandardnumberMapper.TypeParser.class).toInstance(typeParser);
    }

}
