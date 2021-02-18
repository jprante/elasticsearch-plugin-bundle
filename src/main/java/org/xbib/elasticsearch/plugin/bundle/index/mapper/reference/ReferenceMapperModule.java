package org.xbib.elasticsearch.plugin.bundle.index.mapper.reference;

import org.elasticsearch.common.inject.AbstractModule;

/**
 * Reference field mapper module.
 */
public class ReferenceMapperModule extends AbstractModule {

    private final ReferenceMapper.TypeParser typeParser;

    public ReferenceMapperModule(ReferenceMapper.TypeParser typeParser) {
        this.typeParser = typeParser;
    }

    @Override
    protected void configure() {
        bind(ReferenceMapper.TypeParser.class).toInstance(typeParser);
    }
}
