package org.xbib.elasticsearch.index.analysis.combo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.IOException;
import java.io.Reader;

public class ReusableTokenStreamComponents extends Analyzer.TokenStreamComponents {

    protected TokenStream sink;
    protected final String fieldName;
    protected final ComboAnalyzer analyzer;

    public ReusableTokenStreamComponents(String fieldName, ComboAnalyzer analyzer) {
        super(DummyTokenizer.INSTANCE);
        this.fieldName = fieldName;
        this.analyzer = analyzer;
    }

    public void setTokenStream(TokenStream sink) {
        this.sink = sink;
    }

    @Override
    protected void setReader(Reader reader) throws IOException {
        // This ReusableTokenStreamComponents comes from a ReuseStrategy,
        // which uses a ThreadLocal, hence the ComboAnalyzer will reuse
        // this instance and make it ready.
        analyzer.createComponents(fieldName, reader);
    }

    @Override
    public TokenStream getTokenStream() {
        return sink;
    }

    protected static final class DummyTokenizer extends Tokenizer {

        public static final DummyTokenizer INSTANCE = new DummyTokenizer();

        public DummyTokenizer() {
            super(DummyReader.INSTANCE);
        }

        @Override
        public boolean incrementToken() throws IOException {
            return false;
        }

    }

    protected static class DummyReader extends Reader {

        public static final DummyReader INSTANCE = new DummyReader();

        public DummyReader() {
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return 0;
        }

        @Override
        public void close() throws IOException {
        }

    }

}
