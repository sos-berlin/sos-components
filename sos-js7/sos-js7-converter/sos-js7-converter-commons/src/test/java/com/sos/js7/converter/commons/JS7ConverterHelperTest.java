package com.sos.js7.converter.commons;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JS7ConverterHelperTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterHelperTest.class);

    @Ignore
    @Test
    public void test() {
        // see com.sos.joc.classes.JExpressionTest
        List<String> values = new ArrayList<>();
        values.add("");
        values.add("true");
        values.add("1");
        values.add("$var");
        values.add("my_value");
        values.add("my_\"_value");
        values.add("my_'_value");
        values.add("my_\"_'_value");
        values.add("/a/b/c");
        values.add("\\a\\b\\c");
        values.add("C:\\\\a\\b\\c");
        for (String value : values) {
            LOGGER.info(String.format("[%s]%s", value, JS7ConverterHelper.quoteValue4JS7(value)));
        }
    }
}
