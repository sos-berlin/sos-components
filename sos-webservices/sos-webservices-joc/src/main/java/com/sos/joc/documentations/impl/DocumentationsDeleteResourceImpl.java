package com.sos.joc.documentations.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.documentation.DocumentationHelper;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.documentation.impl.DocumentationResourceImpl;
import com.sos.joc.documentations.resource.IDocumentationsDeleteResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.audit.ObjectType;
import com.sos.joc.model.docu.DocumentationsDeleteFilter;
import com.sos.schema.JsonValidator;

@Path("documentations")
public class DocumentationsDeleteResourceImpl extends JOCResourceImpl implements IDocumentationsDeleteResource {

    private static final String API_CALL = "./documentations/delete";

    @Override
    public JOCDefaultResponse deleteDocumentations(String accessToken, byte[] filterBytes) {

        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.DOCUMENTATIONS);
            JsonValidator.validate(filterBytes, DocumentationsDeleteFilter.class);
            DocumentationsDeleteFilter documentationsFilter = Globals.objectMapper.readValue(filterBytes, DocumentationsDeleteFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getDocumentations().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            DBItemJocAuditLog dbAuditItem = storeAuditLog(documentationsFilter.getAuditLog());

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            List<DBItemDocumentation> docs = null;
            if (documentationsFilter.getDocumentations() != null && !documentationsFilter.getDocumentations().isEmpty()) {
                docs = dbLayer.getDocumentations(documentationsFilter.getDocumentations().stream().collect(Collectors.toList()));
            } else {
                docs = dbLayer.getDocumentations(null, documentationsFilter.getFolder(), true, false);
            }
            if (docs != null) {
                JocAuditLog.storeAuditLogDetails(docs.stream().map(dbDoc -> new AuditLogDetail(dbDoc.getPath(), ObjectType.DOCUMENTATION.intValue()))
                        .collect(Collectors.toList()), connection, dbAuditItem);

                Set<String> folders = new HashSet<>();
                for (DBItemDocumentation dbDoc : docs) {
                    DocumentationHelper.delete(dbDoc, connection);
                    folders.add(dbDoc.getFolder());
                }

                if (documentationsFilter.getDocumentations() != null && !documentationsFilter.getDocumentations().isEmpty()) {
                    folders.forEach(f -> DocumentationResourceImpl.postEvent(f)); 
                } else {
                    String parentFolder = "/".equals(documentationsFilter.getFolder()) ? "/" : getParent(documentationsFilter.getFolder());
                    DocumentationResourceImpl.postFolderEvent(parentFolder); 
                }
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
