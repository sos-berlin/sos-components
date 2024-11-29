package com.sos.joc.monitoring.impl;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.model.monitoring.AgentsAnswer;

public class AgentImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentImplTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;

        try {

            factory = createFactory();
            session = factory.openStatelessSession();

            Set<String> allowedControllers = Collections.singleton("js7.x-2.5.x-6444");
            Date dateFrom = SOSDate.getDateTime("2024-11-04 00:00:00");
            Date dateTo = SOSDate.getDateTime("2024-11-10 23:59:59");

            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);

            AgentsImpl impl = new AgentsImpl();
            Map<String, Map<String, Map<String, Date>>> inventoryAgents = dbLayer.getActiveInventoryAgents();
            Map<String, Set<String>> historyTimeZones = new HashMap<>();
            Map<String, Map<String, List<DBItemHistoryAgent>>> historyAgents = impl.getHistoryAgents(dbLayer, historyTimeZones, allowedControllers,
                    dateFrom, dateTo);
            factory.close(session);
            session = null;
            factory = null;

            impl.mergeInventoryAgentsIfNotInHistory(historyTimeZones, historyAgents, inventoryAgents);

            AgentsAnswer answer = new AgentsAnswer();
            answer.setControllers(impl.getItems(historyAgents, inventoryAgents, dateFrom, dateTo));
            answer.setDeliveryDate(new Date());

            LOGGER.info(Globals.objectMapper.writeValueAsString(answer));

        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
        factory.addClassMapping(DBItemHistoryController.class);
        factory.addClassMapping(DBItemHistoryAgent.class);
        factory.addClassMapping(DBItemInventoryAgentInstance.class);
        factory.build();
        return factory;
    }

}
