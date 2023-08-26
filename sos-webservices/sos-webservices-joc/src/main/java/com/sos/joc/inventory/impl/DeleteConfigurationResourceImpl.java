package com.sos.joc.inventory.impl;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.dailyplan.impl.DailyPlanDeleteOrdersImpl;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.ADeleteConfiguration;
import com.sos.joc.inventory.resource.IDeleteConfigurationResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.delete.RequestFilters;
import com.sos.joc.model.inventory.delete.RequestFolder;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class DeleteConfigurationResourceImpl extends ADeleteConfiguration implements IDeleteConfigurationResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteConfigurationResourceImpl.class);

    @Override
    public JOCDefaultResponse remove(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilters.class, true);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = remove(accessToken, in, IMPL_PATH_DELETE);
                //deletePlannedOrdersForRemovedSchedule(in, accessToken);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse removeFolder(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_FOLDER_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFolder.class, true);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = removeFolder(accessToken, in, IMPL_PATH_FOLDER_DELETE);
                //deletePlannedOrdersForRemovedSchedule(in, accessToken);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse delete(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFilters.class, true);
            RequestFilters in = Globals.objectMapper.readValue(inBytes, RequestFilters.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = delete(accessToken, in, IMPL_PATH_TRASH_DELETE);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse deleteFolder(final String accessToken, final byte[] inBytes) {
        try {
            // don't use JsonValidator.validateFailFast because of anyOf-Requirements
            initLogging(IMPL_PATH_TRASH_DELETE, inBytes, accessToken);
            JsonValidator.validate(inBytes, RequestFolder.class, true);
            RequestFolder in = Globals.objectMapper.readValue(inBytes, RequestFolder.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (response == null) {
                response = deleteFolder(accessToken, in, IMPL_PATH_TRASH_DELETE);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void deletePlannedOrdersForRemovedSchedule(RequestFilters in, String accessToken) {
        DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
        DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
        if("now".equals(in.getCancelOrdersDateFrom().toLowerCase())) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
            orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
        } else {
            orderFilter.setDailyPlanDateFrom(in.getCancelOrdersDateFrom());
        }
        for(RequestFilter filter : in.getObjects()) {
            if(filter.getObjectType().equals(ConfigurationType.SCHEDULE)) {
                if(orderFilter.getSchedulePaths() == null) {
                    orderFilter.setSchedulePaths(new ArrayList<String>());
                }
                orderFilter.getSchedulePaths().add(filter.getPath());
            }
        }
        try {
            boolean successful = deleteOrdersImpl.deleteOrders(orderFilter, accessToken, false, false, false);
            if (!successful) {
                JocError je = getJocError();
                if (je != null && je.printMetaInfo() != null) {
                    LOGGER.info(je.printMetaInfo());
                }
                LOGGER.warn("Removing planned order failed.");
            }
        } catch (SOSHibernateException e) {
            // ignore error, should not break process
        }
    }
    
    private void deletePlannedOrdersForRemovedSchedule(RequestFolder request, String accessToken) {
        DailyPlanDeleteOrdersImpl deleteOrdersImpl = new DailyPlanDeleteOrdersImpl();
        DailyPlanOrderFilterDef orderFilter = new DailyPlanOrderFilterDef();
        if("now".equals(request.getCancelOrdersDateFrom().toLowerCase())) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
            orderFilter.setDailyPlanDateFrom(sdf.format(Date.from(Instant.now())));
        } else {
            orderFilter.setDailyPlanDateFrom(request.getCancelOrdersDateFrom());
        }
        if(orderFilter.getScheduleFolders() == null) {
            Folder folder = new Folder();
            folder.setFolder(request.getPath());
            folder.setRecursive(request.getRecursive());
            orderFilter.setScheduleFolders(Arrays.asList(folder));
        }
        try {
            boolean successful = deleteOrdersImpl.deleteOrders(orderFilter, accessToken, false, false, false);
            if (!successful) {
                JocError je = getJocError();
                if (je != null && je.printMetaInfo() != null) {
                    LOGGER.info(je.printMetaInfo());
                }
                LOGGER.warn("Removing planned order failed.");
            }
        } catch (SOSHibernateException e) {
            // ignore error, should not break process
        }
    }
}
