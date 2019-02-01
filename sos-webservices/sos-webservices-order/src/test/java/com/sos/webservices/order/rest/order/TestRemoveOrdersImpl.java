package com.sos.webservices.order.rest.order;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sos.webservices.order.rest.order.impl.RemovePlansImpl;

public class TestRemoveOrdersImpl {

    @Test
    public void testGetDayFrom() {
        RemovePlansImpl removePlansImpl = new RemovePlansImpl();

        // Test: Next year
        int fromDayOfYear = 222;
        int fromYear = 2018;
        int toYear = 2019;
        int day = removePlansImpl.testGetDayFrom(2018, fromYear, fromDayOfYear, toYear);
        assertEquals("testGetDayFrom", 222, day);

        // Test: same year
        toYear = 2018;
        day = removePlansImpl.testGetDayFrom(2018, fromYear, fromDayOfYear, toYear);
        assertEquals("testGetDayFrom", 222, day);

        // Test: middle of 3 years
        toYear = 2020;
        day = removePlansImpl.testGetDayFrom(2019, fromYear, fromDayOfYear, toYear);
        assertEquals("testGetDayFrom", 1, day);

    }

    @Test
    public void testGetDayTo() {
        RemovePlansImpl removePlansImpl = new RemovePlansImpl();
        // Test: Next year
        int fromDayOfYear = 222;
        int toDayOfYear = 3;
        int fromYear = 2018;
        int toYear = 2019;
        int day = removePlansImpl.testGetDayTo(2018, fromYear, toDayOfYear, toYear);
        assertEquals("testGetDayFrom", 365, day);

        day = removePlansImpl.testGetDayTo(2019, fromYear, toDayOfYear, toYear);
        assertEquals("testGetDayFrom", 3, day);

        // Test: same year
        toDayOfYear = 333;
        toYear = 2018;
        day = removePlansImpl.testGetDayTo(2018, fromYear, toDayOfYear, toYear);
        assertEquals("testGetDayFrom", 333, day);

        // Test: middle of 3 years
        toYear = 2020;
        day = removePlansImpl.testGetDayTo(2019, fromYear, toDayOfYear, toYear);
        assertEquals("testGetDayFrom", 365, day);

    }

}
