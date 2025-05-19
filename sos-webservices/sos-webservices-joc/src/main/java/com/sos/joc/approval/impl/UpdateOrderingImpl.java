package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IUpdateOrderingResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.approval.UpdateOrderingFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class UpdateOrderingImpl extends JOCResourceImpl implements IUpdateOrderingResource {

    private static final String API_CALL = "./approval/approvers/ordering";

    @Override
    public JOCDefaultResponse postOrdering(String xAccessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, xAccessToken);
            JsonValidator.validateFailFast(filterBytes, UpdateOrderingFilter.class);
            UpdateOrderingFilter filter = Globals.objectMapper.readValue(filterBytes, UpdateOrderingFilter.class);
            JOCDefaultResponse response = initManageAccountPermissions(xAccessToken);
            if (response != null) {
                return response;
            }
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            
            updateOrdering(filter, dbLayer);
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private void updateOrdering(UpdateOrderingFilter filter, ApprovalDBLayer dbLayer) throws SOSHibernateException {
        List<DBItemJocApprover> dbApprovers =  dbLayer.getApprovers();
        Map<String, DBItemJocApprover> mappedByName = dbApprovers.stream()
                .collect(Collectors.toMap(DBItemJocApprover::getAccountName, Function.identity()));
        int ordering = 1;
        for (String name : filter.getAccountNames()) {
            DBItemJocApprover dbItem = mappedByName.remove(name);
            if (dbItem == null) {
                continue;
            }
            if (ordering != dbItem.getOrdering()) {
                dbItem.setOrdering(ordering);
                dbLayer.getSession().update(dbItem);
            }
            ordering++;
        }
        for (DBItemJocApprover dbItem : mappedByName.values().stream().sorted(Comparator.comparingInt(DBItemJocApprover::getOrdering))
                .collect(Collectors.toCollection(LinkedList::new))) {
            if (ordering != dbItem.getOrdering()) {
                dbItem.setOrdering(ordering);
                dbLayer.getSession().update(dbItem);
            }
            ordering++;
        }
    }
}
