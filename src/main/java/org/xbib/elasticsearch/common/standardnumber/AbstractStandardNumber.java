package org.xbib.elasticsearch.common.standardnumber;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
public abstract class AbstractStandardNumber implements StandardNumber {

    @Override
    public Collection<String> getTypedVariants() {
        return Arrays.asList(
                type().toUpperCase() + " " + format(),
                type().toUpperCase() + " " + normalizedValue());
    }
}
