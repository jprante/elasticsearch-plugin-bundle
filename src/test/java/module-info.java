module org.xbib.elasticsearch.plugin.bundle.test {

    exports org.xbib.elasticsearch.plugin.bundle.test.common.decompound.patricia;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.autophrase;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.baseform;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.concat;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.decompound.fst;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.decompound.patricia;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.german;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.hyphen;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu.segmentation;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu.tools;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.lemmatize;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.naturalsort;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.sortform;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.symbolname;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.analysis.worddelimiter;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.mapper.langdetect;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.mapper.reference;
    exports org.xbib.elasticsearch.plugin.bundle.test.index.mapper.standardnumber;
    exports org.xbib.elasticsearch.plugin.bundle.test.query.decompound;

    opens org.xbib.elasticsearch.plugin.bundle.test.query.decompound;
    opens org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu.segmentation;

    requires junit;
    requires hamcrest.all;
    requires httpcore;
    requires org.xbib.elasticsearch.testframework;
    requires org.xbib.elasticsearch.lucene;
    requires org.xbib.elasticsearch.lucene.testframework;
    requires org.xbib.elasticsearch.netty;
    requires org.xbib.elasticsearch.client.rest;
    requires org.xbib.elasticsearch.server;
    requires org.xbib.elasticsearch.log4j;
    requires org.xbib.elasticsearch.randomizedtesting;
    requires org.xbib.elasticsearch.randomizedtesting.junit.ant;
    requires icu4j;

    requires org.xbib.elasticsearch.plugin.bundle;
    requires org.xbib.elasticsearch.analysis.common;
    requires org.xbib.elasticsearch.transport.nettyfour;
}
