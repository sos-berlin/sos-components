package com.sos.joc.reporting.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.reporting.ReportingDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.reporting.ReportsUpdated;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.reporting.DeleteReports;
import com.sos.joc.reporting.resource.IDeleteReportsResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;


@Path(WebservicePaths.REPORTING)
public class DeleteReportsImpl extends JOCResourceImpl implements IDeleteReportsResource {
    
    @Override
    public JOCDefaultResponse delete(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, DeleteReports.class);
            DeleteReports in = Globals.objectMapper.readValue(filterBytes, DeleteReports.class);
            
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getReports().getManage()));
            if (response != null) {
                return response;
            }
            
            storeAuditLog(in.getAuditLog());
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            
            ReportingDBLayer dbLayer = new ReportingDBLayer(session);
            in.getReportIds().forEach(id -> dbLayer.delete(id));
            Globals.commit(session);
            
            EventBus.getInstance().post(new ReportsUpdated());
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

}
