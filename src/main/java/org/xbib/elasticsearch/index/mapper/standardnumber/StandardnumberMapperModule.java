package org.xbib.elasticsearch.index.mapper.standardnumber;

import org.elasticsearch.common.inject.AbstractModule;

/**
 *
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
