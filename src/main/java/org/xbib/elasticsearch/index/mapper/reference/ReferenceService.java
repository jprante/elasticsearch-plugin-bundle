package org.xbib.elasticsearch.index.mapper.reference;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.settings.Settings;

/**
 *
 */
public class ReferenceService extends AbstractLifecycleComponent {

    private final Injector injector;

    @Inject
    public ReferenceService(Settings settings, Injector injector) {
        super(settings);
        this.injector = injector;
    }

    @Override
    protected void doStart() {
        ReferenceMapperTypeParser referenceMapperTypeParser = injector.getInstance(ReferenceMapperTypeParser.class);
        Client client = injector.getInstance(Client.class);
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
