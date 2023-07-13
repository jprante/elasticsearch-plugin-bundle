package org.xbib.opensearch.plugin.bundle;

import org.apache.lucene.analysis.Analyzer;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionResponse;
import org.opensearch.cluster.metadata.IndexNameExpressionResolver;
import org.opensearch.cluster.node.DiscoveryNodes;
import org.opensearch.common.component.LifecycleComponent;
import org.opensearch.common.inject.Module;
import org.opensearch.common.io.stream.NamedWriteableRegistry;
import org.opensearch.common.settings.ClusterSettings;
import org.opensearch.common.settings.IndexScopedSettings;
import org.opensearch.common.settings.Setting;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.settings.SettingsFilter;
import org.opensearch.index.analysis.AnalyzerProvider;
import org.opensearch.index.analysis.CharFilterFactory;
import org.opensearch.index.analysis.TokenFilterFactory;
import org.opensearch.index.analysis.TokenizerFactory;
import org.opensearch.index.mapper.Mapper;
import org.opensearch.indices.analysis.AnalysisModule;
import org.opensearch.plugins.ActionPlugin;
import org.opensearch.plugins.AnalysisPlugin;
import org.opensearch.plugins.MapperPlugin;
import org.opensearch.plugins.Plugin;
import org.opensearch.plugins.SearchPlugin;
import org.opensearch.rest.RestController;
import org.opensearch.rest.RestHandler;
import org.opensearch.search.DocValueFormat;
import org.xbib.opensearch.plugin.bundle.action.isbnformat.ISBNFormatAction;
import org.xbib.opensearch.plugin.bundle.action.isbnformat.TransportISBNFormatAction;
import org.xbib.opensearch.plugin.bundle.action.langdetect.LangdetectAction;
import org.xbib.opensearch.plugin.bundle.action.langdetect.TransportLangdetectAction;
import org.xbib.opensearch.plugin.bundle.common.reference.ReferenceService;
import org.xbib.opensearch.plugin.bundle.common.standardnumber.StandardnumberService;
import org.xbib.opensearch.plugin.bundle.index.analysis.autophrase.AutoPhrasingTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.baseform.BaseformTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.concat.ConcatTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.concat.PairTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.decompound.fst.FstDecompoundTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.decompound.patricia.DecompoundTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.german.GermanNormalizationFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.hyphen.HyphenAnalyzerProvider;
import org.xbib.opensearch.plugin.bundle.index.analysis.hyphen.HyphenTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.hyphen.HyphenTokenizerFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuCollationKeyAnalyzerProvider;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuCollationTokenizerFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuFoldingCharFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuFoldingTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuNormalizerCharFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuNormalizerTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuNumberFormatTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.IcuTransformTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.icu.segmentation.IcuTokenizerFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.lemmatize.LemmatizeTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.naturalsort.NaturalSortKeyAnalyzerProvider;
import org.xbib.opensearch.plugin.bundle.index.analysis.naturalsort.NaturalSortKeyTokenizerFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.sortform.SortformAnalyzerProvider;
import org.xbib.opensearch.plugin.bundle.index.analysis.sortform.SortformTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.standardnumber.StandardnumberAnalyzerProvider;
import org.xbib.opensearch.plugin.bundle.index.analysis.standardnumber.StandardnumberTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.symbolname.SymbolnameTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.worddelimiter.WordDelimiterFilter2Factory;
import org.xbib.opensearch.plugin.bundle.index.analysis.worddelimiter.WordDelimiterFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.analysis.year.GregorianYearTokenFilterFactory;
import org.xbib.opensearch.plugin.bundle.index.mapper.icu.IcuCollationKeyFieldMapper;
import org.xbib.opensearch.plugin.bundle.index.mapper.langdetect.LangdetectMapper;
import org.xbib.opensearch.plugin.bundle.index.mapper.reference.ReferenceMapper;
import org.xbib.opensearch.plugin.bundle.index.mapper.reference.ReferenceMapperModule;
import org.xbib.opensearch.plugin.bundle.index.mapper.reference.ReferenceMapperTypeParser;
import org.xbib.opensearch.plugin.bundle.index.mapper.standardnumber.StandardnumberMapper;
import org.xbib.opensearch.plugin.bundle.index.mapper.standardnumber.StandardnumberMapperModule;
import org.xbib.opensearch.plugin.bundle.index.mapper.standardnumber.StandardnumberMapperTypeParser;
import org.xbib.opensearch.plugin.bundle.query.decompound.ExactPhraseQueryBuilder;
import org.xbib.opensearch.plugin.bundle.rest.action.isbnformat.RestISBNFormatterAction;
import org.xbib.opensearch.plugin.bundle.rest.action.langdetect.RestLangdetectAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Bundle plugin.
 */
public class BundlePlugin extends Plugin implements AnalysisPlugin, MapperPlugin, SearchPlugin, ActionPlugin {

    private static final StandardnumberMapperTypeParser standardNumberTypeParser =
            new StandardnumberMapperTypeParser();

    private static final ReferenceMapperTypeParser referenceMapperTypeParser =
            new ReferenceMapperTypeParser();

    private final Settings settings;

    public BundlePlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Arrays.asList(
                new Setting<>("plugins.xbib.icu.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.autophrase.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.baseform.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.concat.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.pair.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.decompound.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.german_normalize.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.hyphen.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.sortform.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.standardnumber.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.fst_decompound.enabled", "false", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.worddelimiter.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.worddelimiter2.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.symbolname.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.year.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.lemmatize.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.naturalsort.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.reference.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.langdetect.enabled", "true", Function.identity(), Setting.Property.NodeScope),
                new Setting<>("plugins.xbib.isbnformat.enabled", "true", Function.identity(), Setting.Property.NodeScope)
                );
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
        if (settings.getAsBoolean("plugins.xbib.autophrase.enabled", true)) {
            extra.put("auto_phrase", AutoPhrasingTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.baseform.enabled", true)) {
            extra.put("baseform", BaseformTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.concat.enabled", true)) {
            extra.put("concat", ConcatTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.pair.enabled", true)) {
            extra.put("pair", PairTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.decompound.enabled", true)) {
            extra.put("decompound", DecompoundTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.german_normalize.enabled", true)) {
            extra.put("german_normalize", GermanNormalizationFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.hyphen.enabled", true)) {
            extra.put("hyphen", HyphenTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.sortform.enabled", true)) {
            extra.put("sortform", SortformTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.standardnumber.enabled", true)) {
            extra.put("standardnumber", (indexSettings, environment, name, factorySettings) ->
                    new StandardnumberTokenFilterFactory(indexSettings, environment, name, factorySettings, standardNumberTypeParser));
        }
        if (settings.getAsBoolean("plugins.xbib.fst_decompound.enabled", false)) {
            extra.put("fst_decompound", FstDecompoundTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.worddelimiter.enabled", true)) {
            extra.put("worddelimiter", WordDelimiterFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.worddelimiter2.enabled", true)) {
            extra.put("worddelimiter2", WordDelimiterFilter2Factory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.symbolname.enabled", true)) {
            extra.put("symbolname", SymbolnameTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.year.enabled", true)) {
            extra.put("year", GregorianYearTokenFilterFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.lemmatize.enabled", true)) {
            extra.put("lemmatize", LemmatizeTokenFilterFactory::new);
        }
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.put("icu_collation_tokenizer", IcuCollationTokenizerFactory::new);
            extra.put("icu_tokenizer", IcuTokenizerFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.hyphen.enabled", true)) {
            extra.put("hyphen", HyphenTokenizerFactory::new);
        }
        if (settings.getAsBoolean("plugins.xbib.naturalsort.enabled", true)) {
            extra.put("naturalsort", NaturalSortKeyTokenizerFactory::new);
        }
        return extra;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> getAnalyzers() {
        Map<String, AnalysisModule.AnalysisProvider<AnalyzerProvider<? extends Analyzer>>> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.put("icu_collation", IcuCollationKeyAnalyzerProvider::new);
        }
        if (settings.getAsBoolean("plugins.xbib.hyphen.enabled", true)) {
            extra.put("hyphen", HyphenAnalyzerProvider::new);
        }
        if (settings.getAsBoolean("plugins.xbib.naturalsort.enabled", true)) {
            extra.put("naturalsort", NaturalSortKeyAnalyzerProvider::new);
        }
        if (settings.getAsBoolean("plugins.xbib.sortform.enabled", true)) {
            extra.put("sortform", SortformAnalyzerProvider::new);
        }
        if (settings.getAsBoolean("plugins.xbib.standardnumber.enabled", true)) {
            extra.put("standardnumber", (indexSettings, environment, name, factorySettings) ->
                    new StandardnumberAnalyzerProvider(indexSettings, environment, name, factorySettings, standardNumberTypeParser));
        }
        return extra;
    }

    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        Map<String, Mapper.TypeParser> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.standardnumber.enabled", true)) {
            extra.put(StandardnumberMapper.MAPPER_TYPE, standardNumberTypeParser);
        }
        if (settings.getAsBoolean("plugins.xbib.reference.enabled", true)) {
            extra.put(ReferenceMapper.CONTENT_TYPE, referenceMapperTypeParser);
        }
        if (settings.getAsBoolean("plugins.xbib.langdetect.enabled", true)) {
            extra.put(LangdetectMapper.CONTENT_TYPE, new LangdetectMapper.TypeParser());
        }
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.put(IcuCollationKeyFieldMapper.CONTENT_TYPE, new IcuCollationKeyFieldMapper.TypeParser());
        }
        return extra;
    }

    @Override
    public List<QuerySpec<?>> getQueries() {
        return Collections.singletonList(new QuerySpec<>(ExactPhraseQueryBuilder.NAME,
                ExactPhraseQueryBuilder::new,
                ExactPhraseQueryBuilder::fromXContent));
    }

    @Override
    public List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> getActions() {
        List<ActionHandler<? extends ActionRequest, ? extends ActionResponse>> extra = new ArrayList<>();
        if (settings.getAsBoolean("plugins.xbib.isbnformat.enabled", true)) {
            extra.add(new ActionHandler<>(ISBNFormatAction.INSTANCE, TransportISBNFormatAction.class));
        }
        if (settings.getAsBoolean("plugins.xbib.langdetect.enabled", true)) {
            extra.add(new ActionHandler<>(LangdetectAction.INSTANCE, TransportLangdetectAction.class));
        }
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
        if (settings.getAsBoolean("plugins.xbib.isbnformat.enabled", true)) {
            extra.add(new RestISBNFormatterAction());
        }
        if (settings.getAsBoolean("plugins.xbib.langdetect.enabled", true)) {
            extra.add(new RestLangdetectAction());
        }
        return extra;
    }

    @Override
    public Collection<Module> createGuiceModules() {
        Collection<Module> extra = new ArrayList<>();
        if (settings.getAsBoolean("plugins.xbib.reference.enabled", true)) {
            extra.add(new ReferenceMapperModule(referenceMapperTypeParser));
        }
        if (settings.getAsBoolean("plugins.xbib.standardnumber.enabled", true)) {
            extra.add(new StandardnumberMapperModule(standardNumberTypeParser));
        }
        return extra;
    }

    @Override
    public Collection<Class<? extends LifecycleComponent>> getGuiceServiceClasses() {
        Collection<Class<? extends LifecycleComponent>> extra = new ArrayList<>();
        if (settings.getAsBoolean("plugins.xbib.reference.enabled", true)) {
            extra.add(ReferenceService.class);
        }
        if (settings.getAsBoolean("plugins.xbib.standardnumber.enabled", true)) {
            extra.add(StandardnumberService.class);
        }
        return extra;
    }

    @Override
    public List<NamedWriteableRegistry.Entry> getNamedWriteables() {
        List<NamedWriteableRegistry.Entry> extra = new ArrayList<>();
        if (settings.getAsBoolean("plugins.xbib.icu.enabled", true)) {
            extra.add(new NamedWriteableRegistry.Entry(
                            DocValueFormat.class,
                            IcuCollationKeyFieldMapper.CollationFieldType.COLLATE_FORMAT.getWriteableName(),
                            in -> IcuCollationKeyFieldMapper.CollationFieldType.COLLATE_FORMAT
                    )
            );
        }
        return extra;
    }
}
