package com.sos.joc.audit.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.audit.resource.IAuditLogDetailResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.audit.AuditLogDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.AuditLogDetailFilter;
import com.sos.joc.model.audit.AuditLogDetailItem;
import com.sos.joc.model.audit.AuditLogDetails;
import com.sos.schema.JsonValidator;

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
            initLogging(API_CALL, bytes, accessToken);
            JsonValidator.validateFailFast(bytes, AuditLogDetailFilter.class);
            AuditLogDetailFilter auditLogFilter = Globals.objectMapper.readValue(bytes, AuditLogDetailFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAuditLog().getView());
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
                    if (!getJocPermissions(accessToken).getAdministration().getCertificates().getView()) {
                        permitted = false;
                    }
                case CONTROLLER:
                    if (!dbAuditLog.getControllerId().equals(JocAuditLog.EMPTY_STRING) && !getControllerPermissions(dbAuditLog.getControllerId(),
                            accessToken).getView()) {
                        permitted = false;
                    }
                    break;
                case DOCUMENTATIONS:
                    if (!getJocPermissions(accessToken).getDocumentations().getView()) {
                        permitted = false;
                    }
                case INVENTORY:
                    if (!getJocPermissions(accessToken).getInventory().getView()) {
                        permitted = false;
                    }
                case DAILYPLAN:
                    if (!getJocPermissions(accessToken).getDailyPlan().getView()) {
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
                                availableController -> getControllerPermissions(availableController, accessToken).getDeployments().getView()).collect(
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
}