package com.sos.joc.audit.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.sos.joc.db.audit.AuditLogDBItem;
import com.sos.joc.db.audit.AuditLogDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.AuditLog;
import com.sos.joc.model.audit.AuditLogFilter;
import com.sos.joc.model.audit.AuditLogItem;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.audit.ObjectType;
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
            boolean allControllerAllowed = false;
            boolean controllerCategoryIsPermitted = false;
            boolean deployCategoryIsPermitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getView()).collect(Collectors.toSet());
                controllerCategoryIsPermitted = !allowedControllers.isEmpty();
                deployCategoryIsPermitted = Proxies.getControllerDbInstances().keySet().stream().filter(
                        availableController -> getControllerPermissions(availableController, accessToken).getDeployments().getView()).count() > 0L;
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allControllerAllowed = true;
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
                    return true; // depends on ControllerId
                case CERTIFICATES:
                    return getJocPermissions(accessToken).getAdministration().getCertificates().getView();
                case DAILYPLAN:
                    return getJocPermissions(accessToken).getDailyPlan().getView();
                case DEPLOYMENT:
                    return true; // depends on ControllerId
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

            boolean withDeployment = allowedCategories.contains(CategoryType.DEPLOYMENT);

            // advanced search with objects or folders
            boolean withFolders = auditLogFilter.getFolders() != null && !auditLogFilter.getFolders().isEmpty();
            boolean withObjectName = auditLogFilter.getObjectName() != null && !auditLogFilter.getObjectName().isEmpty();
            boolean withObjectTypes = auditLogFilter.getObjectTypes() != null && !auditLogFilter.getObjectTypes().isEmpty();
            boolean withAdvancedSearch = withFolders || withObjectName || withObjectTypes;

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            AuditLogDBLayer dbLayer = new AuditLogDBLayer(connection);

            Stream<Long> auditLogIds = Stream.empty();
            if (withAdvancedSearch) {
                boolean searchInDepHistory = withDeployment;
                if (withDeployment && withObjectTypes && !auditLogFilter.getObjectTypes().stream().anyMatch(t -> !ObjectType.ORDER.equals(t))) {
                    searchInDepHistory = false;
                }

                auditLogIds = dbLayer.getAuditlogIds(auditLogFilter.getFolders(), auditLogFilter.getObjectTypes(), auditLogFilter.getObjectName());
                if (searchInDepHistory) {
                    auditLogIds = Stream.concat(auditLogIds, dbLayer.getAuditlogIdsFromDepHistory(auditLogFilter.getFolders(), auditLogFilter
                            .getObjectTypes(), auditLogFilter.getObjectName()));
                }
            }

            if (categoriesWithEmptyControllerIds(allowedCategories)) {
                if (allControllerAllowed) {
                    allowedControllers = Collections.emptySet();
                } else {
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
            } else {
                if (allControllerAllowed) {
                    allowedControllers = Collections.emptySet();
                }
            }

            if (EnumSet.allOf(CategoryType.class).size() == allowedCategories.size()) {
                allowedCategories = Collections.emptySet();
            }

            AuditLogDBFilter auditLogDBFilter = new AuditLogDBFilter(auditLogFilter, allowedControllers, allowedCategories, auditLogIds.collect(
                    Collectors.toSet()));
            AuditLog entity = new AuditLog();
            setAuditLogItems(entity.getAuditLog(), dbLayer.getAuditLogs(auditLogDBFilter, withDeployment, auditLogFilter.getLimit()));
            entity.setDeliveryDate(Date.from(Instant.now()));

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

    private void setAuditLogItems(List<AuditLogItem> auditLogItems, ScrollableResults sr) throws Exception {
        try {
            if (sr != null) {
                while (sr.next()) {
                    auditLogItems.add(((AuditLogDBItem) sr.get(0)).toAuditLogItem());
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (sr != null) {
                sr.close();
            }
        }
    }

}