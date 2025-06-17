package com.sos.joc.audit.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.audit.resource.IAuditLogDetailResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.audit.AuditLogDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.model.audit.AuditLogDetailFilter;
import com.sos.joc.model.audit.AuditLogDetailItem;
import com.sos.joc.model.audit.AuditLogDetails;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("audit_log")
public class AuditLogDetailResourceImpl extends JOCResourceImpl implements IAuditLogDetailResource {

    private static final String API_CALL = "./audit_log/detail";
    
    @Override
    public JOCDefaultResponse postAuditLogDetail(String accessToken, byte[] bytes) {
        // only an alias
        return postAuditLogDetails(accessToken, bytes);
    }

    @Override
    public JOCDefaultResponse postAuditLogDetails(String accessToken, byte[] bytes) {
        SOSHibernateSession connection = null;
        try {
            bytes = initLogging(API_CALL, bytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(bytes, AuditLogDetailFilter.class);
            AuditLogDetailFilter auditLogFilter = Globals.objectMapper.readValue(bytes, AuditLogDetailFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAuditLog().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Long auditLogId = auditLogFilter.getAuditLogId();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            AuditLogDBLayer dbLayer = new AuditLogDBLayer(connection);
            DBItemJocAuditLog dbAuditLog = dbLayer.getAuditLog(auditLogId);
            boolean permitted = true;
            AuditLogDetails entity = new AuditLogDetails();

            if (dbAuditLog != null) {
                switch (dbAuditLog.getTypeAsEnum()) {
                case CERTIFICATES:
                    if (!getBasicJocPermissions(accessToken).getAdministration().getCertificates().getView()) {
                        permitted = false;
                    }
                case CONTROLLER:
                    if (!dbAuditLog.getControllerId().equals(JocAuditLog.EMPTY_STRING) && !getBasicControllerPermissions(dbAuditLog.getControllerId(),
                            accessToken).getView()) {
                        permitted = false;
                    }
                    break;
                case DOCUMENTATIONS:
                    if (!getBasicJocPermissions(accessToken).getDocumentations().getView()) {
                        permitted = false;
                    }
                case INVENTORY:
                    if (!getBasicJocPermissions(accessToken).getInventory().getView()) {
                        permitted = false;
                    }
                case DAILYPLAN:
                    if (!getBasicJocPermissions(accessToken).getDailyPlan().getView()) {
                        permitted = false;
                    }
                    break;
                case DEPLOYMENT:
                    // controller permissions will filter the result later
                    break;
                default:
                    break;

                }

                if (permitted) {
                    List<AuditLogDetailItem> details = Collections.emptyList();
                    switch (dbAuditLog.getTypeAsEnum()) {
                    case CERTIFICATES:
                        // Don't have details
                        break;
                    case CONTROLLER:
                    case DOCUMENTATIONS:
                    case INVENTORY:
                    case DAILYPLAN:
                        details = dbLayer.getDetails(auditLogId);
                        break;
                    case DEPLOYMENT:
                        Set<String> allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(
                                availableController -> getBasicControllerPermissions(availableController, accessToken).getDeployments().getView()).collect(
                                        Collectors.toSet());
                        if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                            allowedControllers = Collections.emptySet();
                        }
                        details = dbLayer.getDeploymentDetails(auditLogId, allowedControllers);
                        break;
                    default:
                        break;

                    }
                    entity.setAuditLogDetails(details);
                }
            }
            entity.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
}