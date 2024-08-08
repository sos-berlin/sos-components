package com.sos.commons.util;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.commons.util.common.helper.TestArguments;

public class SOSStringTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSStringTest.class);

    @Ignore
    @Test
    public void testToString() throws Exception {

        char charSample = 1;
        String stringSample = "My String Sample";
        Date dateSample = new Date();

        // Arrays
        String[] stringArraySample = new String[] { "a", "b" };
        File[] fileArraySample = new File[] { new File("a.txt"), new File("b.txt") };
        Long[] longArraySample = new Long[] { 0L, 1L };
        byte[] byteArraySample = new byte[] { 0, 1 };

        // Collections
        Set<String> setSample = Stream.of("a", "b", "c").collect(Collectors.toCollection(HashSet::new));
        List<String> listSample = Stream.of("a", "b", "c").collect(Collectors.toCollection(ArrayList::new));
        Map<String, String> mapSample = Stream.of(new String[][] { { "a", "av" }, { "b", "bv" }, }).collect(Collectors.toMap(data -> data[0],
                data -> data[1]));

        // Others
        DisplayMode enumSample = DisplayMode.UNMASKED;
        SOSCommandResult objectSample1 = new SOSCommandResult("echo 123", StandardCharsets.UTF_8, new SOSTimeout(10, TimeUnit.MINUTES));
        TestArguments objectSample2 = new TestArguments();
        objectSample2.getMyPathArray().setValue(new Path[] { Paths.get("xxx.txt") });
        objectSample2.getMyURLArray().setValue(new URL[] { new URL("http://localhost") });

        Set<?> setObjectsSample = Stream.of(enumSample, objectSample1, objectSample2).collect(Collectors.toCollection(HashSet::new));
        List<?> listObjectsSample = Stream.of(enumSample, objectSample1, objectSample2).collect(Collectors.toCollection(ArrayList::new));
        Map<Object, Object> mapObjectsSample = Stream.of(new Object[][] { { "a", enumSample }, { "b", objectSample1 }, }).collect(Collectors.toMap(
                data -> data[0], data -> data[1]));

        // OUTPUT
        LOGGER.info(String.format("[CHAR]%s", SOSString.toString(charSample)));
        LOGGER.info(String.format("[STRING]%s", SOSString.toString(stringSample)));
        LOGGER.info(String.format("[DATE]%s", SOSString.toString(dateSample)));
        // Arrays
        LOGGER.info(String.format("[ARRAY][STRING]%s", SOSString.toString(stringArraySample)));
        LOGGER.info(String.format("[ARRAY][FILE]%s", SOSString.toString(fileArraySample)));
        LOGGER.info(String.format("[ARRAY][LONG]%s", SOSString.toString(longArraySample)));
        LOGGER.info(String.format("[ARRAY][BYTE]%s", SOSString.toString(byteArraySample)));
        // Collections
        LOGGER.info(String.format("[SET]%s", SOSString.toString(setSample)));
        LOGGER.info(String.format("[LIST]%s", SOSString.toString(listSample)));
        LOGGER.info(String.format("[MAP]%s", SOSString.toString(mapSample)));
        // Others
        LOGGER.info(String.format("[ENUM]%s", SOSString.toString(enumSample)));
        LOGGER.info(String.format("[OBJECT]%s", SOSString.toString(objectSample1)));
        LOGGER.info(String.format("[OBJECT]%s", SOSString.toString(objectSample2)));

        LOGGER.info(String.format("[SET][OBJECTS]%s", SOSString.toString(setObjectsSample)));
        LOGGER.info(String.format("[LIST][OBJECTS]%s", SOSString.toString(listObjectsSample)));
        LOGGER.info(String.format("[MAP][OBJECTS]%s", SOSString.toString(mapObjectsSample)));
    }

    @Ignore
    @Test
    public void testTrim() throws Exception {
        String input = " && 'XYZ'";
        String result = SOSString.trim(input, "&&", "||");
        LOGGER.info(String.format("[result]%s", result));
    }

}
