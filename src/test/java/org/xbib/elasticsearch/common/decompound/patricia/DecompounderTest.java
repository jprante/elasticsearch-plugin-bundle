package org.xbib.elasticsearch.common.decompound.patricia;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertTrue;

public class DecompounderTest {

    @SuppressForbidden(value = "execute this to test decompounder cache performance")
    @Test
    public void testWikipediaSample() throws IOException, XMLStreamException {
        String forward = "/decompound/patricia/kompVVic.tree";
        String backward = "/decompound/patricia/kompVHic.tree";
        String reduce = "/decompound/patricia/grfExt.tree";
        double threshold = 0.51d;
        Decompounder decompounder = new Decompounder(getClass().getResourceAsStream(forward),
                getClass().getResourceAsStream(backward),
                getClass().getResourceAsStream(reduce),
                threshold);
        long partCount = 0;
        long t0 = System.currentTimeMillis();
        try (InputStream inputStream = new GZIPInputStream(getClass().getResourceAsStream("wpd13_sample.i5.xml.gz"))) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream);
            String name = null;
            while (streamReader.hasNext()) {
                streamReader.next();
                if (streamReader.isStartElement()) {
                    name = streamReader.getLocalName();
                }
                if (streamReader.hasText()) {
                    if ("s".equals(name)) {
                        for (String string : streamReader.getText().split("\\s+")) {
                            List<String> list = decompounder.decompound(string);
                            partCount += list.size();
                        }
                    }
                }
            }
        }
        long t1 = System.currentTimeMillis();
        long uncachedPerf = partCount * 1000 / (t1 - t0);

        LFUCache<String, List<String>> cache = new LFUCache<>(100000, 0.90f);
        long partCacheCount = 0;
        long t2 = System.currentTimeMillis();
        try (InputStream inputStream = new GZIPInputStream(getClass().getResourceAsStream("wpd13_sample.i5.xml.gz"))) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(inputStream);
            String name = null;
            while (streamReader.hasNext()) {
                streamReader.next();
                if (streamReader.isStartElement()) {
                    name = streamReader.getLocalName();
                }
                if (streamReader.hasText()) {
                    if ("s".equals(name)) {
                        for (String string : streamReader.getText().split("\\s+")) {
                            List<String> list = cache.computeIfAbsent(string, decompounder::decompound);
                            partCacheCount += list.size();
                        }
                    }
                }
            }
        }
        long t3 = System.currentTimeMillis();
        long cachedPerf = partCacheCount * 1000 / (t3 - t2);

        double factor = (double)cachedPerf / uncachedPerf;

        assertTrue(factor >= 3.0);
    }
}
