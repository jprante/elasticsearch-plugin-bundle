module org.xbib.elasticsearch.plugin.bundle {

    exports org.xbib.elasticsearch.plugin.bundle;
    exports org.xbib.elasticsearch.plugin.bundle.action.isbnformat;
    exports org.xbib.elasticsearch.plugin.bundle.action.langdetect;
    exports org.xbib.elasticsearch.plugin.bundle.common.decompound.patricia;
    exports org.xbib.elasticsearch.plugin.bundle.common.fsa;
    exports org.xbib.elasticsearch.plugin.bundle.common.langdetect;
    exports org.xbib.elasticsearch.plugin.bundle.common.reference;
    exports org.xbib.elasticsearch.plugin.bundle.common.standardnumber;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.autophrase;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.baseform;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.concat;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.decompound.fst;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.decompound.patricia;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.german;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.hyphen;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.segmentation;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.tokenattributes;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.icu.tools;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.icu;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.lemmatize;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.naturalsort;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.sortform;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.standardnumber;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.symbolname;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.worddelimiter;
    exports org.xbib.elasticsearch.plugin.bundle.index.analysis.year;
    exports org.xbib.elasticsearch.plugin.bundle.index.mapper.icu;
    exports org.xbib.elasticsearch.plugin.bundle.index.mapper.langdetect;
    exports org.xbib.elasticsearch.plugin.bundle.index.mapper.reference;
    exports org.xbib.elasticsearch.plugin.bundle.index.mapper.standardnumber;
    exports org.xbib.elasticsearch.plugin.bundle.query.decompound;

    requires static org.xbib.elasticsearch.server;
    requires static org.xbib.elasticsearch.lucene;
    requires static org.xbib.elasticsearch.jackson;
    requires static org.xbib.elasticsearch.joda;
    requires static org.xbib.elasticsearch.log4j;

    requires icu4j;
}