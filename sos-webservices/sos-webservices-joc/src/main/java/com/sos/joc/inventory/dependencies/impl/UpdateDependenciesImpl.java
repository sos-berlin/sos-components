package com.sos.joc.inventory.dependencies.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.dependencies.DependencyResolver;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.dependencies.resource.IUpdateDependencies;
import com.sos.joc.model.inventory.common.ConfigurationType;

import jakarta.ws.rs.Path;


@Path("inventory/dependencies")
public class UpdateDependenciesImpl extends JOCResourceImpl implements IUpdateDependencies {

    private static final String API_CALL = "./inventory/dependencies/update";
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDependenciesImpl.class);
    private static final List<Integer> dependencyTypes = Collections.unmodifiableList(new ArrayList<Integer>() {
        private static final long serialVersionUID = 1L;
        {
            add(ConfigurationType.WORKFLOW.intValue());
            add(ConfigurationType.FILEORDERSOURCE.intValue());
            add(ConfigurationType.JOBRESOURCE.intValue());
            add(ConfigurationType.JOBTEMPLATE.intValue());
            add(ConfigurationType.LOCK.intValue());
            add(ConfigurationType.NOTICEBOARD.intValue());
            add(ConfigurationType.SCHEDULE.intValue());
            add(ConfigurationType.WORKINGDAYSCALENDAR.intValue());
            add(ConfigurationType.NONWORKINGDAYSCALENDAR.intValue());
        }
      });
    
    @Override
    public JOCDefaultResponse postUpdateDependencies(String xAccessToken) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, "".getBytes(), xAccessToken);
            final SOSHibernateSession session = Globals.createSosHibernateStatelessConnection(xAccessToken);
            hibernateSession = session;
            InventoryDBLayer dblayer = new InventoryDBLayer(hibernateSession);
            List<DBItemInventoryConfiguration> allConfigs = dblayer.getConfigurationsByType(DependencyResolver.dependencyTypes);
            Map<ConfigurationType, Map<String,DBItemInventoryConfiguration>> groupedItems = allConfigs.stream() .collect(
                    Collectors.groupingBy(DBItemInventoryConfiguration::getTypeAsEnum, 
                            Collectors.toMap(DBItemInventoryConfiguration::getName, Function.identity())));
            DependencyResolver.dependencyTypes.forEach(ctype -> groupedItems.putIfAbsent(ConfigurationType.fromValue(ctype), Collections.emptyMap()));

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
