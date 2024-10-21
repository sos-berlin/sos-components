package com.sos.joc.inventory.dependencies.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.dependencies.resource.IUpdateDependencies;

import jakarta.ws.rs.Path;


@Path("inventory/dependencies")
public class UpdateDependenciesImpl extends JOCResourceImpl implements IUpdateDependencies {

    private static final String API_CALL = "./inventory/dependencies/update";
    
    @Override
    public JOCDefaultResponse postUpdateDependencies(String xAccessToken) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, "".getBytes(), xAccessToken);
            final SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(xAccessToken);
            hibernateSession = session;
            InventoryDBLayer dblayer = new InventoryDBLayer(hibernateSession);
            List<DBItemInventoryConfiguration> allConfigs = dblayer.getConfigurationsByType(DependencyResolver.dependencyTypes);
            
            DependencyResolver.updateDependencies(session, allConfigs);
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
