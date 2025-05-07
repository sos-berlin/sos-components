package com.sos.joc.documentations.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.documentation.DocumentationHelper;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.documentations.resource.IDocumentationsResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.docu.Documentation;
import com.sos.joc.model.docu.Documentations;
import com.sos.joc.model.docu.DocumentationsFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("documentations")
public class DocumentationsResourceImpl extends JOCResourceImpl implements IDocumentationsResource {

    private static final String API_CALL = "./documentations";

    @Override
    public JOCDefaultResponse postDocumentations(String accessToken, byte[] filterBytes) {

        SOSHibernateSession sosHibernateSession = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DocumentationsFilter.class);
            DocumentationsFilter documentationsFilter = Globals.objectMapper.readValue(filterBytes, DocumentationsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getDocumentations().getView());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
            List<DBItemDocumentation> dbDocs = new ArrayList<DBItemDocumentation>();
            Set<String> types = null;
            if (documentationsFilter.getTypes() != null && !documentationsFilter.getTypes().isEmpty()) {
                if (documentationsFilter.getTypes().contains("assigntypes")) {
                    documentationsFilter.getTypes().remove("assigntypes");
                    types = Stream.concat(documentationsFilter.getTypes().stream(), DocumentationHelper.ASSIGN_TYPES.stream()).map(
                            String::toLowerCase).collect(Collectors.toSet());
                } else {
                    types = documentationsFilter.getTypes().stream().map(String::toLowerCase).collect(Collectors.toSet());
                }
            }
            boolean onlyWithAssignReference = documentationsFilter.getOnlyWithAssignReference() == Boolean.TRUE;
            if (documentationsFilter.getDocumentations() != null && !documentationsFilter.getDocumentations().isEmpty()) {
                dbDocs = dbLayer.getDocumentations(documentationsFilter.getDocumentations().stream().collect(Collectors.toList()));
            } else {
                if (documentationsFilter.getFolders() != null && !documentationsFilter.getFolders().isEmpty()) {
                    for (Folder folder : documentationsFilter.getFolders()) {
                        dbDocs.addAll(dbLayer.getDocumentations(types, folder.getFolder(), folder.getRecursive(), onlyWithAssignReference));
                    }
                } else if (documentationsFilter.getTypes() != null && !documentationsFilter.getTypes().isEmpty()) {
                    dbDocs = dbLayer.getDocumentations(types, null, false, onlyWithAssignReference);
                } else {
                    dbDocs = dbLayer.getDocumentations((List<String>) null, onlyWithAssignReference);
                }
            }
            Documentations documentations = new Documentations();
            documentations.setDocumentations(mapDbItemsToDocumentations(dbDocs, documentationsFilter.getOnlyWithAssignReference(), folderPermissions.getListOfFolders()));
            documentations.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(documentations);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private List<Documentation> mapDbItemsToDocumentations(List<DBItemDocumentation> dbDocs, Boolean onlyWithAssignReference, Set<Folder> permittedFolders) {
        Stream<Documentation> docs = dbDocs.stream().filter(dbDoc -> folderIsPermitted(dbDoc.getFolder(), permittedFolders)).map(dbDoc -> {
            Documentation doc = new Documentation();
            doc.setId(dbDoc.getId());
            doc.setName(dbDoc.getName());
            doc.setPath(dbDoc.getPath());
            doc.setType(dbDoc.getType().toLowerCase());
            doc.setModified(dbDoc.getModified());
            doc.setAssignReference(dbDoc.getDocRef());
            return doc;
        });
        if (onlyWithAssignReference == Boolean.TRUE) {
            return docs.sorted(Comparator.comparing(doc -> doc.getAssignReference().toLowerCase())).collect(Collectors.toList());
        } else {
            return docs.sorted(Comparator.comparing(doc -> doc.getName().toLowerCase())).collect(Collectors.toList());
        }
    }

}
