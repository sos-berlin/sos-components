package com.sos.joc2;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSShell;
import com.sos.joc.Globals;
import com.sos.joc.classes.DependencyUpdate;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JocCertificate;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.proxy.ClusterWatch;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.db.cluster.CheckInstance;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;

import jakarta.servlet.ServletException;

public class JocServletContainer extends ServletContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocServletContainer.class);

    private static final long serialVersionUID = 1L;
    
    private static final boolean withClusterService = true;

    public JocServletContainer() {
        super();
    }

    @Override
    public void init() throws ServletException {
        LOGGER.debug("----> init on starting JOC");
        super.init();

        Globals.sosCockpitProperties = new JocCockpitProperties(MapUrls.getJocProperties());
        Globals.setJocSecurityLevel(MapUrls.getSecurityLevelByUser());
        Globals.readUnmodifiables();
        JOCJsonCommand.urlMapper = MapUrls.getUrlMapperByUser();
        AgentHelper.testMode = true;
        
        try {
            CheckInstance.check();
        } catch (JocConfigurationException | SOSHibernateException e) {
            if (Globals.sosHibernateFactory != null) {
                LOGGER.info("----> closing DB Connections");
                Globals.sosHibernateFactory.close();
            }
            CheckInstance.stopJOC();
            throw new ServletException(e);
        }
        
        updateCertificate();
        ClusterWatch.init(MapUrls.getUrlMapperByUser());
        OrderTags.getInstance();
        
        try {
            Globals.setProperties();
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
        
        new Thread(() -> {
            Proxies.startAll(Globals.sosCockpitProperties, 0, ProxyUser.JOC, MapUrls.getUrlMapperByUser());
            new Thread(() -> {
                InventorySubagentClustersDBLayer.fillEmptyControllerIds();
                WorkflowPaths.init();
                WorkflowRefs.init();
                SOSShell.printSystemInfos();
                SOSShell.printJVMInfos();
            }, "servlet-init2").start();
            if (withClusterService) {
                JocClusterService.getInstance().start(StartupMode.automatic, true);
            }
            DependencyUpdate.getInstance().updateThreaded();
        }, "servlet-init").start();
        
    }

    @Override
    public void destroy() {
        LOGGER.debug("----> destroy on close JOC");

        // 1 - stop cluster
        if (withClusterService) {
            JocClusterService.getInstance().stop(StartupMode.automatic, true, true);
            JocClusterServiceLogger.clearAllLoggers();
        }
        // 2 - close proxies
        QuickSearchStore.close(); //insert
        Proxies.closeAll();
        DependencyUpdate.getInstance().close();

        if (Globals.sosHibernateFactory != null) {
            LOGGER.info("----> closing DB Connections");
            Globals.sosHibernateFactory.close();
        }
        super.destroy();
    }

    private void updateCertificate() {
        System.setProperty("jetty.base", "C:\\ProgramData\\sos-berlin.com\\js7\\joc\\jetty_base");
        JocCertificate.updateCertificate();
    }
    
}
