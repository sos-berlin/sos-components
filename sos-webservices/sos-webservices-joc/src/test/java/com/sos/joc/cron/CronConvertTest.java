package com.sos.joc.cron;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.inventory.convert.cron.CronUtils;
import com.sos.joc.model.inventory.workflow.WorkflowEdit;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CronConvertTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronConvertTest.class);
    private SOSHibernateFactory factory = null;
    private SOSHibernateSession session = null;
    
    @Test
    @Ignore
    public void testParseFile () throws Exception {
        BufferedReader bufferedReader = 
                Files.newBufferedReader(Paths.get(getClass().getClassLoader().getResource("cron/crontest").toURI()), StandardCharsets.UTF_8);
        Calendar cal = new Calendar();
        try {
            factory = new SOSHibernateFactory("./src/test/resources/sp.hibernate.cfg.xml");
            factory.addClassMapping(DBLayer.getJocClassMapping());
            factory.build();
            session = factory.openStatelessSession("parseFile_test");
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            Map<WorkflowEdit, ScheduleEdit> scheduledWorkflows = CronUtils.cronFile2Workflows(dbLayer, bufferedReader, cal, "agentRC3", "Europe/Berlin" ,true);
            Comparator<Map.Entry<WorkflowEdit,ScheduleEdit>> wfComp = new Comparator<Map.Entry<WorkflowEdit,ScheduleEdit>>() {

                @Override
                public int compare(Entry<WorkflowEdit, ScheduleEdit> o1, Entry<WorkflowEdit, ScheduleEdit> o2) {
                    String o1Number = o1.getKey().getName().substring(o1.getKey().getName().length() -2, o1.getKey().getName().length());
                    if (o1Number.startsWith("-")) {
                        o1Number = o1.getKey().getName().substring(o1.getKey().getName().length() -1, o1.getKey().getName().length());
                    }
                    String o2Number = o2.getKey().getName().substring(o2.getKey().getName().length() -2, o2.getKey().getName().length() );
                    if (o2Number.startsWith("-")) {
                        o2Number = o2.getKey().getName().substring(o2.getKey().getName().length() -1, o2.getKey().getName().length() );
                    }
                    if(Integer.parseInt(o1Number) < Integer.parseInt(o2Number)) {
                        return -1;
                    } else if (o1.getKey().getName().equals(o2.getKey().getName())) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                
            };
            scheduledWorkflows.entrySet().stream().sorted(wfComp).forEach(item -> {
                try {
                    String workflow = Globals.prettyPrintObjectMapper.writeValueAsString(item.getKey());
                    String schedule = Globals.prettyPrintObjectMapper.writeValueAsString(item.getValue());
                    LOGGER.info("Workflow with name: " + item.getKey().getName());
                    LOGGER.info(workflow);
                    LOGGER.info("and with schedule with name: " + item.getValue().getName());
                    LOGGER.info(schedule);
                    LOGGER.info("------------------------------------------------------------------------------------------------------");
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            session.close();
            if (factory != null) {
                factory.close();
            }
        }
    }
}
