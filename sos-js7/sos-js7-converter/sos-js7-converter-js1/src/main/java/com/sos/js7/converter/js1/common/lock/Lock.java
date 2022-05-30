package com.sos.js7.converter.js1.common.lock;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Node;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class Lock {

    private static final String ATTR_MAX_NON_EXCLUSIVE = "max_non_exclusive";
    private static final String ATTR_NAME = "name";

    private final Integer maxNonExclusive;
    private final String name;

    public Lock(Path file) throws Exception {
        Node node = JS7ConverterHelper.getDocumentRoot(file);

        Map<String, String> m = JS7ConverterHelper.attribute2map(node);
        this.maxNonExclusive = JS7ConverterHelper.integerValue(m.get(ATTR_MAX_NON_EXCLUSIVE));
        this.name = m.get(ATTR_NAME);
    }

    public Integer getMaxNonExclusive() {
        return maxNonExclusive;
    }

    public String getName() {
        return name;
    }

}
