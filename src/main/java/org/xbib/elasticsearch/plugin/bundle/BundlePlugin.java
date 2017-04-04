package org.xbib.elasticsearch.plugin.bundle;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.ClusterSettings;
import org.elasticsearch.common.settings.IndexScopedSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsFilter;
import org.elasticsearch.index.analysis.AnalyzerProvider;
import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.xbib.elasticsearch.action.isbnformat.ISBNFormatAction;
import org.xbib.elasticsearch.action.isbnformat.TransportISBNFormatAction;
import org.xbib.elasticsearch.action.langdetect.LangdetectAction;
import org.xbib.elasticsearch.action.langdetect.TransportLangdetectAction;
import org.xbib.elasticsearch.index.analysis.autophrase.AutoPhrasingTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.baseform.BaseformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.concat.ConcatTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.concat.PairTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.decompound.patricia.DecompoundTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.decompound.fst.FstDecompoundTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.lemmatize.LemmatizeTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.german.GermanNormalizationFilterFactory;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.hyphen.HyphenTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuCollationKeyAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.icu.IcuCollationTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuFoldingCharFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuFoldingTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuNormalizerCharFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuNormalizerTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuNumberFormatTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.IcuTransformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.icu.segmentation.IcuTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.naturalsort.NaturalSortKeyAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.naturalsort.NaturalSortKeyTokenizerFactory;
import org.xbib.elasticsearch.index.analysis.sortform.SortformAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.sortform.SortformTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardnumberAnalyzerProvider;
import org.xbib.elasticsearch.index.analysis.standardnumber.StandardnumberTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.symbolname.SymbolnameTokenFilterFactory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilter2Factory;
import org.xbib.elasticsearch.index.analysis.worddelimiter.WordDelimiterFilterFactory;
import org.xbib.elasticsearch.index.analysis.year.GregorianYearTokenFilterFactory;
import org.xbib.elasticsearch.index.mapper.crypt.CryptMapper;
import org.xbib.elasticsearch.index.mapper.langdetect.LangdetectMapper;
import org.xbib.elasticsearch.index.mapper.reference.ReferenceMapper;
import org.xbib.elasticsearch.index.mapper.reference.ReferenceMapperModule;
import org.xbib.elasticsearch.index.mapper.reference.ReferenceMapperTypeParser;
import org.xbib.elasticsearch.common.reference.ReferenceService;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapper;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapperModule;
import org.xbib.elasticsearch.index.mapper.standardnumber.StandardnumberMapperTypeParser;
import org.xbib.elasticsearch.common.standardnumber.StandardnumberService;
import org.xbib.elasticsearch.rest.action.isbnformat.RestISBNFormatterAction;
import org.xbib.elasticsearch.rest.action.langdetect.RestLangdetectAction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 */
public class BundlePlugin extends Plugin implements AnalysisPlugin, MapperPlugin, ActionPlugin {

    private static final StandardnumberMapperTypeParser standardNumberTypeParser =
            new StandardnumberMapperTypeParser();

    private static final ReferenceMapperTypeParser referenceMapperTypeParser =
            new ReferenceMapperTypeParser();

    private final Settings settings;

    public BundlePlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<CharFilterFactory>> getCharFilters() {
        Map<String, AnalysisModule.AnalysisProvider<CharFilterFactory>> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.put("icu_normalizer", IcuNormalizerCharFilterFactory::new);
            extra.put("icu_folding", IcuFoldingCharFilterFactory::new);
        }
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.put("icu_normalizer", IcuNormalizerTokenFilterFactory::new);
            extra.put("icu_folding", IcuFoldingTokenFilterFactory::new);
            extra.put("icu_transform", IcuTransformTokenFilterFactory::new);
            extra.put("icu_numberformat", IcuNumberFormatTokenFilterFactory::new);
        }
        extra.put("auto_phrase", AutoPhrasingTokenFilterFactory::new);
        extra.put("baseform", BaseformTokenFilterFactory::new);
        extra.put("concat", ConcatTokenFilterFactory::new);
        extra.put("pair", PairTokenFilterFactory::new);
        extra.put("decompound", DecompoundTokenFilterFactory::new);
        extra.put("german_normalize", GermanNormalizationFilterFactory::new);
        extra.put("hyphen", HyphenTokenFilterFactory::new);
        extra.put("sortform", SortformTokenFilterFactory::new);
        extra.put("standardnumber", (indexSettings, environment, name, factorySettings) ->
                new StandardnumberTokenFilterFactory(indexSettings, environment, name, factorySettings, standardNumberTypeParser));
        extra.put("fst_decompound", FstDecompoundTokenFilterFactory::new);
        extra.put("worddelimiter", WordDelimiterFilterFactory::new);
        extra.put("worddelimiter2", WordDelimiterFilter2Factory::new);
        extra.put("symbolname", SymbolnameTokenFilterFactory::new);
        extra.put("year", GregorianYearTokenFilterFactory::new);
        extra.put("lemmatize", LemmatizeTokenFilterFactory::new);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.put("icu_collation_tokenizer", IcuCollationTokenizerFactory::new);
            extra.put("icu_tokenizer", IcuTokenizerFactory::new);
        }
        extra.put("hyphen", HyphenTokenizerFactory::new);
        extra.put("naturalsort", NaturalSortKeyTokenizerFactory::new);
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.put("icu_collation", IcuCollationKeyAnalyzerProvider::new);
        }
        extra.put("hyphen", HyphenAnalyzerProvider::new);
        extra.put("naturalsort", NaturalSortKeyAnalyzerProvider::new);
        extra.put("sortform", SortformAnalyzerProvider::new);
        extra.put("standardnumber", (indexSettings, environment, name, factorySettings) ->
                new StandardnumberAnalyzerProvider(indexSettings, environment, name, factorySettings, standardNumberTypeParser));
        return extra;
    }

    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        Map<String, Mapper.TypeParser> extra = new LinkedHashMap<>();
        extra.put(StandardnumberMapper.MAPPER_TYPE, standardNumberTypeParser);
        extra.put(ReferenceMapper.MAPPER_TYPE, referenceMapperTypeParser);
        extra.put(CryptMapper.MAPPER_TYPE, new CryptMapper.TypeParser());
        extra.put(LangdetectMapper.MAPPER_TYPE, new LangdetectMapper.TypeParser());
        return extra;
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> extra = new ArrayList<>();
        extra.add(new ActionHandler<>(ISBNFormatAction.INSTANCE, TransportISBNFormatAction.class));
        extra.add(new ActionHandler<>(LangdetectAction.INSTANCE, TransportLangdetectAction.class));
        return extra;
    }

    @Override
    public List<RestHandler> getRestHandlers(Settings settings,
                                             RestController restController,
                                             ClusterSettings clusterSettings,
                                             IndexScopedSettings indexScopedSettings,
                                             SettingsFilter settingsFilter,
                                             IndexNameExpressionResolver indexNameExpressionResolver,
                                             Supplier<DiscoveryNodes> nodesInCluster) {
        List<RestHandler> extra = new ArrayList<>();
        extra.add(new RestISBNFormatterAction(settings, restController));
        extra.add(new RestLangdetectAction(settings, restController));
        return extra;
    }

    @Override
    public Collection<Module> createGuiceModules() {
        Collection<Module> extra = new ArrayList<>();
        extra.add(new ReferenceMapperModule(referenceMapperTypeParser));
        extra.add(new StandardnumberMapperModule(standardNumberTypeParser));
        return extra;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> getGuiceServiceClasses() {
        Collection<Class<? extends LifecycleComponent>> extra = new ArrayList<>();
        extra.add(ReferenceService.class);
        extra.add(StandardnumberService.class);
        return extra;
    }
}
