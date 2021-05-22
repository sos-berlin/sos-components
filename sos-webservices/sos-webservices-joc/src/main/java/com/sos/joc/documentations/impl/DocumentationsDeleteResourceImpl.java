package com.sos.joc.documentations.impl;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.documentations.resource.IDocumentationsDeleteResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.audit.ObjectType;
import com.sos.joc.model.docu.DocumentationsFilter;
import com.sos.schema.JsonValidator;

@Path("documentations")
public class DocumentationsDeleteResourceImpl extends JOCResourceImpl implements IDocumentationsDeleteResource {

    private static final String API_CALL = "./documentations/delete";

    @Override
    public JOCDefaultResponse deleteDocumentations(String accessToken, byte[] filterBytes) {

        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DocumentationsFilter.class);
            DocumentationsFilter documentationsFilter = Globals.objectMapper.readValue(filterBytes, DocumentationsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken)
                    .getDocumentations().getManage());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("documentations", documentationsFilter.getDocumentations());
            DBItemJocAuditLog dbAuditItem = storeAuditLog(documentationsFilter.getAuditLog(), CategoryType.DOCUMENTATIONS);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            List<DBItemDocumentation> docs = dbLayer.getDocumentations(documentationsFilter.getDocumentations());
            JocAuditLog.storeAuditLogDetails(docs.stream().map(dbDoc -> new AuditLogDetail(dbDoc.getPath(), ObjectType.DOCUMENTATION.intValue()))
                    .collect(Collectors.toList()), connection, dbAuditItem);
            for (DBItemDocumentation dbDoc : docs) {
                if (dbDoc.getImageId() != null) {
                    DBItemDocumentationImage dbImage = connection.get(DBItemDocumentationImage.class, dbDoc.getImageId());
                    if (dbImage != null) {
                        connection.delete(dbImage);
                    }
                }
                connection.delete(dbDoc);
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
