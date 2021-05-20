package com.sos.joc.documentation.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.documentation.resource.IDocumentationUsedResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.docu.DocumentationFilter;
import com.sos.joc.model.docu.UsedBy;
import com.sos.schema.JsonValidator;

@Path("documentation")
public class DocumentationUsedResourceImpl extends JOCResourceImpl implements IDocumentationUsedResource {

    private static final String API_CALL = "./documentation/used";

    @Override
    public JOCDefaultResponse postDocumentationsUsed(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DocumentationFilter.class);
//            DocumentationFilter documentationFilter = Globals.objectMapper.readValue(filterBytes, DocumentationFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getDocumentations().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            // TODO folder permissions?
            UsedBy usedBy = new UsedBy();
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            // TODO Look into INV_CONFIGURATION with JSON-SQL
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
//            usedBy.setObjects(dbLayer.getDocumentationUsages(normalizePath(documentationFilter.getDocumentation())));
            usedBy.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(usedBy);
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
