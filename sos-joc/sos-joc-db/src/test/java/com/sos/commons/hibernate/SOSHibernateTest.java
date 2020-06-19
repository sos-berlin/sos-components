package com.sos.commons.hibernate;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.history.DBItemHistoryLog;

public class SOSHibernateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateTest.class);

    public static void main(String[] args) {

        DBItemHistoryLog item = new DBItemHistoryLog();
        item.setId(new Long(1));
        item.setJobSchedulerId("jobscheduler2.0");
        item.setMainOrderId(new Long(123));
        item.setCompressed(false);
        item.setFileContent(String.valueOf("xxx").getBytes());
        item.setCreated(new Date());

        LOGGER.info(SOSHibernate.toString(item));

    }

}
