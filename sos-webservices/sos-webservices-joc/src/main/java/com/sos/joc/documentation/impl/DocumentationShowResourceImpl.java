package com.sos.joc.documentation.impl;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.documentation.resource.IDocumentationShowResource;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;

@Path("documentation")
public class DocumentationShowResourceImpl extends JOCResourceImpl implements IDocumentationShowResource {

    private static final String API_CALL_SHOW = "./documentation/show";
    
    @Override
    public JOCDefaultResponse show(String xAccessToken, String accessToken, String path) {
        return show(getAccessToken(xAccessToken, accessToken), path);
    }

    // ./documentation/preview as alias for show
    @Override
    public JOCDefaultResponse preview(String xAccessToken, String accessToken, String path) {
        return show(getAccessToken(xAccessToken, accessToken), path);
    }

    private JOCDefaultResponse show(String accessToken, String path) {
        try {
            String request = String.format("%s/%s/%s", API_CALL_SHOW, accessToken, path.replaceFirst("^/", ""));
            initLogging(request, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getDocumentations().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            checkRequiredParameter("documentation", path);
            if (!path.contains("/")) {
                path = getPathFromRef(path);
            }
            checkFolderPermissions(path);
            String entity = String.format(
                    "<!DOCTYPE html>%n<html>\n<head>%n  <meta http-equiv=\"refresh\" content=\"0;URL='%s%s'\" />%n</head>%n<body>%n</body>%n</html>",
                    accessToken, JOCJsonCommand.urlEncodedPath(path));

            return JOCDefaultResponse.responseHtmlStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseHTMLStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseHTMLStatusJSError(e, getJocError());
        }
    }
    
    private String getPathFromRef(String docRef) {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_SHOW);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            String path = dbLayer.getPath(docRef);
            if (path == null) {
                throw new DBMissingDataException("Couldn't find a documentation with the name (" + docRef + ")");
            }
            return path;
        } finally {
            Globals.disconnect(connection);
        }
    }

//    @Override
//    public JOCDefaultResponse postUrl(String accessToken, byte[] inBytes) {
//        try {
//            initLogging(API_CALL_URL, inBytes, accessToken);
//            JsonValidator.validate(inBytes, DocumentationShowFilter.class);
//            DocumentationShowFilter documentationFilter = Globals.objectMapper.readValue(inBytes, DocumentationShowFilter.class);
//            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getDocumentations().getView());
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//
//            DocumentationUrl entity = new DocumentationUrl();
//            entity.setUrl(getUrl(accessToken, documentationFilter));
//            entity.setDeliveryDate(Date.from(Instant.now()));
//
//            return JOCDefaultResponse.responseStatus200(entity);
//        } catch (JocException e) {
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatusJSError(e);
//        } catch (Exception e) {
//            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        }
//    }
//
//    private String getUrl(String accessToken, DocumentationShowFilter documentationFilter)
//            throws JocMissingRequiredParameterException, JocConfigurationException, DBConnectionRefusedException, DBInvalidDataException,
//            DBMissingDataException, UnsupportedEncodingException, DBOpenSessionException {
//        SOSHibernateSession connection = null;
//        try {
//            documentationFilter.setPath(normalizePath(documentationFilter.getPath()));
//            connection = Globals.createSosHibernateStatelessConnection(API_CALL_URL);
//            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
//            
//            // TODO look into object's JSON to get the documentation's path
//            String path = null; //dbLayer.getDocumentationName(documentationFilter);
//            if (path == null) {
//                throw new DBMissingDataException("Couldn't find the documentation");
//            }
//            //checkFolderPermissions(path); or better checkFolderPermissions(documentationFilter.getPath()); ???
//            return accessToken + JOCJsonCommand.urlEncodedPath(path);
//        } finally {
//            Globals.disconnect(connection);
//        }
//    }

}
