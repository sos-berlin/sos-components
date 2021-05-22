package com.sos.joc.documentation.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.documentation.resource.IDocumentationUsedResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.InventoryObject;
import com.sos.joc.model.docu.DocumentationFilter;
import com.sos.joc.model.docu.UsedBy;
import com.sos.schema.JsonValidator;

@Path("documentation")
public class DocumentationUsedResourceImpl extends JOCResourceImpl implements IDocumentationUsedResource {

    private static final String API_CALL = "./documentation/used";

    @Override
    public JOCDefaultResponse postDocumentationUsed(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DocumentationFilter.class);
            DocumentationFilter documentationFilter = Globals.objectMapper.readValue(filterBytes, DocumentationFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getDocumentations().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            UsedBy usedBy = new UsedBy();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            String docRef = dbLayer.getDocRef(documentationFilter.getDocumentation());
            if (docRef == null) {
                usedBy.setObjects(null);
            } else {
                InventoryDBLayer invDbLayer = new InventoryDBLayer(connection);
                List<DBItemInventoryConfiguration> usedObjects = invDbLayer.getUsedObjectsByDocName(docRef);
                List<DBItemInventoryConfiguration> usedJobs = invDbLayer.getUsedJobsByDocName(docRef);
                Stream<DBItemInventoryConfiguration> usedByStream = Stream.empty();
                if (usedObjects != null) {
                    usedByStream = usedObjects.stream();
                }
                if (usedJobs != null) {
                    usedByStream = Stream.concat(usedByStream, usedJobs.stream());
                }
                usedBy.setObjects(usedByStream.map(item -> {
                    InventoryObject js = new InventoryObject();
                    js.setPath(item.getPath());
                    js.setType(item.getTypeAsEnum());
                    return js;
                }).distinct().collect(Collectors.toList()));
                
            }
            usedBy.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(usedBy);
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
