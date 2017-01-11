package org.xbib.elasticsearch.index.analysis.icu.tokenattributes;

import com.ibm.icu.lang.UScript;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeReflector;

/**
 * Implementation of {@link ScriptAttribute} that stores the script as an integer.
 */
public class ScriptAttributeImpl extends AttributeImpl implements ScriptAttribute, Cloneable {
    private int code = UScript.COMMON;

    public ScriptAttributeImpl() {}

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return UScript.getName(code);
    }

    @Override
    public String getShortName() {
        return UScript.getShortName(code);
    }

    @Override
    public void clear() {
        code = UScript.COMMON;
    }

    @Override
    public void copyTo(AttributeImpl target) {
        ScriptAttribute t = (ScriptAttribute) target;
        t.setCode(code);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ScriptAttributeImpl &&
                ((ScriptAttributeImpl) other).code == code;
    }

    @Override
    public ScriptAttributeImpl clone() {
        ScriptAttributeImpl attribute = (ScriptAttributeImpl) super.clone();
        attribute.code = this.code;
        return attribute;
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public void reflectWith(AttributeReflector reflector) {
        String name = code == UScript.JAPANESE ? "Chinese/Japanese" : getName();
        reflector.reflect(ScriptAttribute.class, "script", name);
    }
}
