package com.sos.joc.documentations.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.documentations.resource.IDocumentationsResource;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.docu.Documentation;
import com.sos.joc.model.docu.Documentations;
import com.sos.joc.model.docu.DocumentationsFilter;
import com.sos.schema.JsonValidator;

@Path("documentations")
public class DocumentationsResourceImpl extends JOCResourceImpl implements IDocumentationsResource {

    private static final String API_CALL = "./documentations";
    private static final Set<String> ASSIGN_TYPES = new HashSet<String>(Arrays.asList("html", "xml", "pdf", "markdown"));

    @Override
    public JOCDefaultResponse postDocumentations(String accessToken, byte[] filterBytes) throws Exception {

        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DocumentationsFilter.class);
            DocumentationsFilter documentationsFilter = Globals.objectMapper.readValue(filterBytes, DocumentationsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(documentationsFilter.getControllerId(), getJocPermissions(accessToken)
                    .getDocumentations().getView());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("controllerId", documentationsFilter.getControllerId());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
            List<DBItemDocumentation> dbDocs = new ArrayList<DBItemDocumentation>();
            Set<String> types = null;
            if (documentationsFilter.getTypes() != null && !documentationsFilter.getTypes().isEmpty()) {
                types = documentationsFilter.getTypes().stream().map(String::toLowerCase).collect(Collectors.toSet());
                if (types.contains("assigntypes")) {
                    types.remove("assigntypes");
                    types.addAll(ASSIGN_TYPES);
                }
            }
            if (documentationsFilter.getDocumentations() != null && !documentationsFilter.getDocumentations().isEmpty()) {
                dbDocs = dbLayer.getDocumentations(documentationsFilter.getDocumentations());
            } else {
                if (documentationsFilter.getFolders() != null && !documentationsFilter.getFolders().isEmpty()) {
                    for (Folder folder : documentationsFilter.getFolders()) {
                        dbDocs.addAll(dbLayer.getDocumentations(types, folder.getFolder(), folder.getRecursive()));
                    }
                } else if (documentationsFilter.getTypes() != null && !documentationsFilter.getTypes().isEmpty()) {
                    dbDocs = dbLayer.getDocumentations(types, null, false);
                } else {
                    dbDocs = dbLayer.getDocumentations((List<String>) null);
                }
                if (documentationsFilter.getRegex() != null && !documentationsFilter.getRegex().isEmpty()) {
                    dbDocs = filterByRegex(dbDocs, documentationsFilter.getRegex());
                }
            }
            Documentations documentations = new Documentations();
            documentations.setDocumentations(mapDbItemsToDocumentations(dbDocs));
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

    private List<DBItemDocumentation> filterByRegex(List<DBItemDocumentation> unfilteredDocs, String regex) throws Exception {
        List<DBItemDocumentation> filteredDocs = new ArrayList<DBItemDocumentation>();
        Pattern p = Pattern.compile(regex);
        for (DBItemDocumentation unfilteredDoc : unfilteredDocs) {
            Matcher regExMatcher = p.matcher(unfilteredDoc.getPath());
            if (regExMatcher.find()) {
                filteredDocs.add(unfilteredDoc);
            }
        }
        return filteredDocs;
    }

    private List<Documentation> mapDbItemsToDocumentations(List<DBItemDocumentation> dbDocs) {
        List<Documentation> docs = new ArrayList<Documentation>();
        for (DBItemDocumentation dbDoc : dbDocs) {
            Documentation doc = new Documentation();
            doc.setId(dbDoc.getId());
            doc.setName(dbDoc.getName());
            doc.setPath(dbDoc.getPath());
            doc.setType(dbDoc.getType());
            doc.setModified(dbDoc.getModified());
            docs.add(doc);
        }
        return docs;
    }

}
