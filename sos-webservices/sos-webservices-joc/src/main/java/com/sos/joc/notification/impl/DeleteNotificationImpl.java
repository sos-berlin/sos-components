package com.sos.joc.notification.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.notification.DeleteNotificationFilter;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.delete.DeleteConfiguration;
import com.sos.joc.notification.resource.IDeleteNotification;
import com.sos.joc.xmleditor.impl.DeleteResourceImpl;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("notification")
public class DeleteNotificationImpl extends JOCResourceImpl implements IDeleteNotification {
    
    private static final String API_CALL = "./notification/delete";
    
    @Override
    public JOCDefaultResponse postDeleteNotification(String xAccessToken, byte[] deleteNotificationFilter) {
        try {
            deleteNotificationFilter = initLogging(API_CALL, deleteNotificationFilter, xAccessToken);
            JsonValidator.validateFailFast(deleteNotificationFilter, DeleteNotificationFilter.class);
            DeleteConfiguration in = Globals.objectMapper.readValue(deleteNotificationFilter, DeleteConfiguration.class);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getNotification().getManage()));
            if (response != null) {
                return response;
            }
            DBItemJocAuditLog auditLog = storeAuditLog(in.getAuditLog(), CategoryType.MONITORING);
            in.setObjectType(ObjectType.NOTIFICATION);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(DeleteResourceImpl.handleStandardConfiguration(in,
                    getAccount(), auditLog.getId())));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
