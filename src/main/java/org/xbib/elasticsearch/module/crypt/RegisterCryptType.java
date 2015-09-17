package org.xbib.elasticsearch.module.crypt;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;
import org.xbib.elasticsearch.index.mapper.crypt.CryptMapper;

class RegisterCryptType extends AbstractIndexComponent {

    @Inject
    RegisterCryptType(Index index, @IndexSettings Settings indexSettings, MapperService mapperService) {
        super(index, indexSettings);
        mapperService.documentMapperParser().putTypeParser(CryptMapper.CONTENT_TYPE, new CryptMapper.TypeParser());
    }
}
