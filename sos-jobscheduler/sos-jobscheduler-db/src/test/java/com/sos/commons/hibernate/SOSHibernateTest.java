package com.sos.commons.hibernate;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.db.history.DBItemLog;

public class SOSHibernateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateTest.class);

    public static void main(String[] args) {

        DBItemLog item = new DBItemLog();
        item.setId(new Long(1));
        item.setMasterId("jobscheduler2.0");
        item.setMainOrderId(new Long(123));
        item.setFileCompressed(String.valueOf("xxx").getBytes());
        item.setCreated(new Date());

        LOGGER.info(SOSHibernate.toString(item));

    }

}
