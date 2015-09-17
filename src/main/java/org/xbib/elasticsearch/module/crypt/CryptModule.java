package org.xbib.elasticsearch.module.crypt;

import org.elasticsearch.common.inject.Binder;
import org.elasticsearch.common.inject.Module;

public class CryptModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(RegisterCryptType.class).asEagerSingleton();
    }
}
