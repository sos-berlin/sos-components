package com.sos.joc.documentations.impl;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
import com.sos.joc.db.documentation.DBItemDocumentationUsage;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.documentations.resource.IDocumentationsDeleteResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.docu.DocumentationsFilter;
import com.sos.schema.JsonValidator;

@Path("documentations")
public class DocumentationsDeleteResourceImpl extends JOCResourceImpl implements IDocumentationsDeleteResource {

    private static final String API_CALL = "./documentations/delete";

    @Override
    public JOCDefaultResponse deleteDocumentations(String accessToken, byte[] filterBytes) throws Exception {

        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DocumentationsFilter.class);
            DocumentationsFilter documentationsFilter = Globals.objectMapper.readValue(filterBytes, DocumentationsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(documentationsFilter.getControllerId(), getJocPermissions(accessToken)
                    .getDocumentations().getManage());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("documentations", documentationsFilter.getDocumentations());

            storeAuditLog(documentationsFilter.getAuditLog(), documentationsFilter.getControllerId(), CategoryType.DOCUMENTATIONS);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
            List<DBItemDocumentation> docs = dbLayer.getDocumentations(documentationsFilter.getDocumentations());
            for (DBItemDocumentation dbDoc : docs) {
                List<DBItemDocumentationUsage> dbUsages = dbLayer.getDocumentationUsages(dbDoc.getId());
                if (dbUsages != null && !dbUsages.isEmpty()) {
                    for (DBItemDocumentationUsage dbUsage : dbUsages) {
                        sosHibernateSession.delete(dbUsage);
                    }
                }
                if (dbDoc.getImageId() != null) {
                    DBItemDocumentationImage dbImage = sosHibernateSession.get(DBItemDocumentationImage.class, dbDoc.getImageId());
                    if (dbImage != null) {
                        sosHibernateSession.delete(dbImage);
                    }
                }
                sosHibernateSession.delete(dbDoc);
                // storeAuditLogEntry(deleteAudit);
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
