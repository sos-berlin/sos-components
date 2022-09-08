package com.sos.joc2;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.classes.workflow.WorkflowPaths;

import jakarta.servlet.ServletException;

public class JocServletContainer extends ServletContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocServletContainer.class);

    private static final long serialVersionUID = 1L;

    public JocServletContainer() {
        super();
    }

    @Override
    public void init() throws ServletException {
        LOGGER.debug("----> init on starting JOC");
        super.init();

        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.setJocSecurityLevel(MapUrls.getSecurityLevelByUser());
        Proxies.startAll(Globals.sosCockpitProperties, 0, ProxyUser.JOC, MapUrls.getUrlMapperByUser());
        WorkflowPaths.init();
        SOSShell.printSystemInfos();
        SOSShell.printJVMInfos();
        Globals.readUnmodifiables();
        Globals.setProperties();
        JOCJsonCommand.urlMapper = MapUrls.getUrlMapperByUser();
        AgentHelper.testMode = true;
        //JocClusterService.getInstance().start();
    }

    @Override
    public void destroy() {
        LOGGER.debug("----> destroy on close JOC");
        super.destroy();

        Proxies.closeAll();
        //JocClusterService.getInstance().stop(true);

        if (Globals.sosHibernateFactory != null) {
            LOGGER.info("----> closing DB Connections");
            Globals.sosHibernateFactory.close();
        }
    }

}
