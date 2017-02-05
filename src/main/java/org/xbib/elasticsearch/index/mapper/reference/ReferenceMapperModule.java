package org.xbib.elasticsearch.index.mapper.reference;

import org.elasticsearch.common.inject.AbstractModule;
import org.xbib.elasticsearch.common.reference.ReferenceService;

/**
 *
 */
public class ReferenceMapperModule extends AbstractModule {

    private final ReferenceMapperTypeParser typeParser;

    public ReferenceMapperModule(ReferenceMapperTypeParser typeParser) {
        this.typeParser = typeParser;
    }

    @Override
    protected void configure() {
        bind(ReferenceService.class).asEagerSingleton();
        bind(ReferenceMapperTypeParser.class).toInstance(typeParser);
    }
}
