package com.sos.commons.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSHibernateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSQLCommandExtractorTest.class);

    @Ignore
    @Test
    public void toInTest() {
        List<String> strings = null;
        LOGGER.info(SOSHibernate.convertStrings(strings));

        strings = new ArrayList<>();
        strings.add("a");
        LOGGER.info(SOSHibernate.convertStrings(strings));
        strings.add("b");
        LOGGER.info(SOSHibernate.convertStrings(strings));

        List<Number> numbers = null;
        LOGGER.info(SOSHibernate.convertNumbers(numbers));

        numbers = new ArrayList<>();
        numbers.add(0L);
        LOGGER.info(SOSHibernate.convertNumbers(numbers));
        numbers.add(1);
        LOGGER.info(SOSHibernate.convertNumbers(numbers));
    }
}
