package com.sos.joc.classes.calendar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.joc.model.calendar.Dates;

public class FrequencyResolverTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrequencyResolverTest.class);

    @Before
    public void setUp() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Ignore
    @Test
    public void restrictionsTest() throws Exception {
        String from = "2022-02-03";
        String to = "2022-02-04";
        Calendar baseCalendar = getCalendar(Paths.get("src/test/resources/calendar/calendar.json"));
        Calendar restrictions = getCalendar(Paths.get("src/test/resources/calendar/restrictions.json"));

        FrequencyResolver fr = new FrequencyResolver();
        Dates dates = fr.resolveRestrictions(baseCalendar, restrictions, from, to);
        LOGGER.info("DATES: " + String.join(",", dates.getDates()));
    }

    private Calendar getCalendar(Path json) throws JsonParseException, JsonMappingException, IOException {
        return new ObjectMapper().readValue(Files.readAllBytes(json), Calendar.class);
    }

}
