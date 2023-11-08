package org.xbib.opensearch.plugin.bundle.common.reference;

import org.opensearch.client.Client;
import org.opensearch.common.lifecycle.AbstractLifecycleComponent;
import org.opensearch.common.inject.Inject;
import org.opensearch.common.inject.Injector;
import org.xbib.opensearch.plugin.bundle.index.mapper.reference.ReferenceMapperTypeParser;

/**
 * Reference service.
 */
public class ReferenceService extends AbstractLifecycleComponent {

    private final Injector injector;

    @Inject
    public ReferenceService(Injector injector) {
        super();
        this.injector = injector;
    }

    @Override
    protected void doStart() {
        // get the client from the injector
        Client client = injector.getInstance(Client.class);
        // copy the client to the mapper type parser
        ReferenceMapperTypeParser referenceMapperTypeParser = injector.getInstance(ReferenceMapperTypeParser.class);
        referenceMapperTypeParser.setClient(client);
    }

    @Override
    protected void doStop() {
        // nothing to stop
    }

    @Override
    protected void doClose() {
        // nothing to close
    }
}
