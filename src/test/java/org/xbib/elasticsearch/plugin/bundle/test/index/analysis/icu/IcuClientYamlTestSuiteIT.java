package org.xbib.elasticsearch.plugin.bundle.test.index.analysis.icu;

import com.carrotsearch.randomizedtesting.annotations.Name;
import com.carrotsearch.randomizedtesting.annotations.ParametersFactory;

import org.elasticsearch.testframework.rest.yaml.ClientYamlTestCandidate;
import org.elasticsearch.testframework.rest.yaml.ESClientYamlSuiteTestCase;

public class IcuClientYamlTestSuiteIT extends ESClientYamlSuiteTestCase {

    public IcuClientYamlTestSuiteIT(@Name("yaml") ClientYamlTestCandidate testCandidate) {
        super(testCandidate);
    }

    @ParametersFactory
    public static Iterable<Object[]> parameters() throws Exception {
        return ESClientYamlSuiteTestCase.createParameters(IcuClientYamlTestSuiteIT.class.getClassLoader());
    }
}
