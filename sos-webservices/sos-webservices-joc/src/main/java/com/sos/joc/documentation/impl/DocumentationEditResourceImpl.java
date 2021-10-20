package com.sos.joc.documentation.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.documentation.resource.IDocumentationEditResource;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectAlreadyExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.audit.ObjectType;
import com.sos.joc.model.docu.DocumentationFilter;
import com.sos.schema.JsonValidator;

@Path("documentation")
public class DocumentationEditResourceImpl extends JOCResourceImpl implements IDocumentationEditResource {

    private static final String API_CALL = "./documentation/edit";

    @Override
    public JOCDefaultResponse postDocumentationEdit(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DocumentationFilter.class);
            DocumentationFilter documentationFilter = Globals.objectMapper.readValue(filterBytes, DocumentationFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getDocumentations().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DBItemJocAuditLog dbAudit = storeAuditLog(documentationFilter.getAuditLog(), CategoryType.DOCUMENTATIONS);
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            String path = documentationFilter.getDocumentation();
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            
            DBItemDocumentation dbItem = dbLayer.getDocumentation(path);
            if (dbItem == null) {
                throw new DBMissingDataException("A documentation with the name (" + path + ") could not be found");
            }
            JocAuditLog.storeAuditLogDetail(new AuditLogDetail(path, ObjectType.DOCUMENTATION.intValue()), connection, dbAudit);
            
            if (documentationFilter.getAssignReference() != null && !documentationFilter.getAssignReference().isEmpty()) {
                if (documentationFilter.getAssignReference().equals(dbItem.getDocRef())) {
                    // Nothing to do
                } else {
                    dbItem.setDocRef(checkUniqueReference(documentationFilter.getAssignReference(), path, dbLayer));
                    dbItem.setIsRef(true);
                    connection.update(dbItem);
                    DocumentationResourceImpl.postEvent(dbItem.getFolder());
                }
            } else {
                if (documentationFilter.getAssignReference() == null) {
                    // Nothing to do 
                } else {
                    dbItem.setDocRef(null);
                    dbItem.setIsRef(false);
                    connection.update(dbItem);
                    DocumentationResourceImpl.postEvent(dbItem.getFolder());
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
    
    private static String checkUniqueReference(String reference, String path, DocumentationDBLayer dbLayer) {
        String otherPath = dbLayer.getDocumentationByRef(reference, path);
        if (otherPath != null) {
            throw new JocObjectAlreadyExistException(String.format("The reference %s is already used in %s", reference, otherPath));
        }
        return reference;
    }

}
