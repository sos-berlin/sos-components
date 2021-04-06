package com.sos.joc.documentations.impl;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.DeleteDocumentationAudit;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentation;
import com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationImage;
import com.sos.joc.db.inventory.deprecated.documentation.DBItemDocumentationUsage;
import com.sos.joc.documentations.resource.IDocumentationsDeleteResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.docu.DocumentationsFilter;

@Path("documentations")
public class DocumentationsDeleteResourceImpl extends JOCResourceImpl implements IDocumentationsDeleteResource {

    private static final String API_CALL = "./documentations/delete";

    @Override
    public JOCDefaultResponse deleteDocumentations(String xAccessToken, DocumentationsFilter filter) throws Exception {
        
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, filter.getControllerId(), getJocPermissions(xAccessToken)
                    .getDocumentations().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            checkRequiredParameter("controllerId", filter.getControllerId());
            checkRequiredParameter("documentations", filter.getDocumentations());
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            List<DBItemDocumentation> docs = dbLayer.getDocumentations(filter.getControllerId(), filter.getDocumentations());
            logAuditMessage(filter.getAuditLog());
            for (DBItemDocumentation dbDoc : docs) {
                DeleteDocumentationAudit deleteAudit = new DeleteDocumentationAudit(filter, dbDoc.getPath(), dbDoc.getDirectory());
                List<DBItemDocumentationUsage> dbUsages = dbLayer.getDocumentationUsages(filter.getControllerId(), dbDoc.getId());
                if (dbUsages != null && !dbUsages.isEmpty()) {
                    for (DBItemDocumentationUsage dbUsage : dbUsages) {
                        connection.delete(dbUsage);
                    }
                }
                if (dbDoc.getImageId() != null) {
                    DBItemDocumentationImage dbImage = connection.get(DBItemDocumentationImage.class, dbDoc.getImageId());
                    if (dbImage != null) {
                        connection.delete(dbImage);
                    }
                }
                connection.delete(dbDoc);
                storeAuditLogEntry(deleteAudit);
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
