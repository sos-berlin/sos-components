package com.sos.joc.inventory.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IRevalidateResource;
import com.sos.joc.model.inventory.common.RequestFilter;
import com.sos.joc.model.inventory.validate.Report;
import com.sos.joc.model.inventory.validate.ReportItem;

import io.vavr.collection.Stream;
import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class RevalidateResourceImpl extends JOCResourceImpl implements IRevalidateResource {

    @Override
    public JOCDefaultResponse revalidate(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            RequestFilter in = Globals.objectMapper.readValue(inBytes, RequestFilter.class);

            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(revalidate(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private Report revalidate(RequestFilter in) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            
            DBItemJocAuditLog dbAuditLog = JocInventory.storeAuditLog(getJocAuditLog(), in.getAuditLog());
            Report report = JocInventory.revalidate(null, dbLayer, getJocError(), dbAuditLog);
            
            report.setErroneousObjs(sort(report.getErroneousObjs()));
            report.setInvalidObjs(sort(report.getInvalidObjs()));
            report.setValidObjs(sort(report.getValidObjs()));
            
            Stream.concat(report.getInvalidObjs(), report.getValidObjs()).map(ReportItem::getPath).map(JOCResourceImpl::getParent).distinct().forEach(
                    JocInventory::postEvent);

            return report;
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private Set<ReportItem> sort(Set<ReportItem> collection) {
        if (collection == null) {
            return null;
        }
        return collection.stream().sorted(Comparator.comparing(i -> i.getPath().toLowerCase())).collect(Collectors.toSet());
    }
}
