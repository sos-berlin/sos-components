package com.sos.joc.audit.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.hibernate.ScrollableResults;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.audit.resource.IAuditLogResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.audit.AuditLogDBFilter;
import com.sos.joc.db.audit.AuditLogDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.AuditLog;
import com.sos.joc.model.audit.AuditLogFilter;
import com.sos.joc.model.audit.AuditLogItem;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

@Path("audit_log")
public class AuditLogResourceImpl extends JOCResourceImpl implements IAuditLogResource {

    private static final String API_CALL = "./audit_log";

    @Override
    public JOCDefaultResponse postAuditLog(String accessToken, byte[] bytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, bytes, accessToken);
            JsonValidator.validateFailFast(bytes, AuditLogFilter.class);
            AuditLogFilter auditLogFilter = Globals.objectMapper.readValue(bytes, AuditLogFilter.class);
            
            String controllerId = auditLogFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAuditLog().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            Set<String> allowedControllers = Collections.emptySet();
            boolean controllerCategoryIsPermitted = false;
            boolean deployCategoryIsPermitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getView()).collect(Collectors.toSet());
                controllerCategoryIsPermitted = !allowedControllers.isEmpty();
                deployCategoryIsPermitted = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getDeployments().getView()).count() > 0L;
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                controllerCategoryIsPermitted = getControllerPermissions(controllerId, accessToken).getView();
                deployCategoryIsPermitted = getControllerPermissions(controllerId, accessToken).getDeployments().getView();
                if (controllerCategoryIsPermitted) {
                    allowedControllers = Collections.singleton(controllerId);
                }
            }
              
            Set<CategoryType> allowedCategories = EnumSet.allOf(CategoryType.class).stream().filter(c -> {
                switch (c) {
                case CONTROLLER:
                    return true; //depends on ControllerId
                case CERTIFICATES:
                    return getJocPermissions(accessToken).getAdministration().getCertificates().getView();
                case DAILYPLAN:
                    return getJocPermissions(accessToken).getDailyPlan().getView();
                case DEPLOYMENT:
                    return true; //depends on ControllerId
                case DOCUMENTATIONS:
                    return getJocPermissions(accessToken).getDocumentations().getView();
                case INVENTORY:
                    return getJocPermissions(accessToken).getInventory().getView();
                default:
                    return true;
                }
            }).collect(Collectors.toSet());
            
            if (!controllerCategoryIsPermitted) {
                allowedCategories.remove(CategoryType.CONTROLLER);
            }
            if (!deployCategoryIsPermitted) {
                allowedCategories.remove(CategoryType.DEPLOYMENT);
            }
            if (auditLogFilter.getCategories() != null && !auditLogFilter.getCategories().isEmpty()) {
                allowedCategories.retainAll(auditLogFilter.getCategories());
            }
            
            if (categoriesWithEmptyControllerIds(allowedCategories)) {
                if (allowedControllers.isEmpty()) {
                    allowedControllers = Collections.singleton(JocAuditLog.EMPTY_STRING);
                } else {
                    if (controllerId.isEmpty()) {
                        if (!allowedControllers.isEmpty()) {
                            allowedControllers.add(JocAuditLog.EMPTY_STRING);
                        }
                    } else {
                        if (!allowedControllers.isEmpty()) {
                            allowedControllers = Arrays.asList(controllerId, JocAuditLog.EMPTY_STRING).stream().collect(Collectors.toSet());
                        } else {
                            allowedControllers = Collections.singleton(JocAuditLog.EMPTY_STRING);
                        }
                    }
                }
            }
            
            if (EnumSet.allOf(CategoryType.class).size() == allowedCategories.size()) {
                allowedCategories = Collections.emptySet(); 
            }
            
            AuditLogDBFilter auditLogDBFilter = new AuditLogDBFilter(auditLogFilter, allowedControllers, allowedCategories);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            AuditLogDBLayer dbLayer = new AuditLogDBLayer(connection);
            ScrollableResults auditLogs = dbLayer.getAuditLogs(auditLogDBFilter, auditLogFilter.getLimit());
            AuditLog entity = new AuditLog();
            entity.setAuditLog(getAuditLogItems(auditLogs));
            entity.setDeliveryDate(new Date());

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private boolean categoriesWithEmptyControllerIds(Set<CategoryType> categories) {
        if (categories == null || categories.isEmpty()) {
            return true;
        }
        return categories.stream().anyMatch(c -> !CategoryType.CONTROLLER.equals(c));
    }

    private List<AuditLogItem> getAuditLogItems(ScrollableResults auditLogsFromDb) {
        List<AuditLogItem> auditLogItems = new ArrayList<>();
        if (auditLogsFromDb != null) {
            while (auditLogsFromDb.next()) {
                DBItemJocAuditLog dbItem = (DBItemJocAuditLog) auditLogsFromDb.get(0);
                AuditLogItem auditLogItem = new AuditLogItem();
                auditLogItem.setId(dbItem.getId());
                auditLogItem.setControllerId(dbItem.getControllerId());
                auditLogItem.setAccount(dbItem.getAccount());
                auditLogItem.setRequest(dbItem.getRequest());
                auditLogItem.setParameters(dbItem.getParameters());
                auditLogItem.setCategory(dbItem.getTypeAsEnum());
                auditLogItem.setComment(dbItem.getComment());
                auditLogItem.setCreated(dbItem.getCreated());
                auditLogItem.setTicketLink(dbItem.getTicketLink());
                auditLogItem.setTimeSpent(dbItem.getTimeSpent());
                auditLogItems.add(auditLogItem);
            }
        }
        return auditLogItems;
    }

}