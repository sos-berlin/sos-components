package com.sos.joc.documentation.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.JocAuditTrail;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentationImage;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.documentation.resource.IDocumentationResource;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.documentation.DocumentationEvent;
import com.sos.joc.event.bean.documentation.DocumentationFolderEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.audit.CategoryType;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

@Path("documentation")
public class DocumentationResourceImpl extends JOCResourceImpl implements IDocumentationResource {

    private static final String API_CALL = "./documentation";
    private static final java.nio.file.Path CSS = Paths.get("/sos/css/default-markdown.css");

    @Override
    public JOCDefaultResponse postDocumentation(String accessToken, String referer, String path) {
        try {
            if (path == null) {
                path = "";
            }
            String request = String.format("%s/%s", API_CALL, path);
            if (referer != null && referer.contains("/joc/api/documentation/")) {
                initLogging(request, null, CategoryType.DOCUMENTATIONS);
            } else {
                initLogging(request, null, accessToken, CategoryType.DOCUMENTATIONS);
                JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getDocumentations().getView());
                if (jocDefaultResponse != null) {
                    return jocDefaultResponse;
                }
            }
            
            checkRequiredParameter("path", path);
            return postDocumentation(path, getJocAuditTrail());
        } catch (Exception e) {
            return responseStatusJSError(e, MediaType.TEXT_HTML + "; charset=UTF-8");
        }
    }
    
    public static JOCDefaultResponse postDocumentation(String path, JocAuditTrail auditTrail) {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            DBItemDocumentation dbItem = dbLayer.getDocumentation(path);
            String errMessage = "Couldn't find a database entry (" + path + ") as documentation resource";

            if (dbItem == null) {
                throw new DBMissingDataException(errMessage);
            }
            String type = dbItem.getType();
            if (dbItem.getImageId() != null && dbItem.getImageId() != 0L) {
                DBItemDocumentationImage dbItemImage = dbLayer.getDocumentationImage(dbItem.getImageId());
                if (dbItemImage == null) {
                    throw new DBMissingDataException(errMessage);
                }
                if (type.isEmpty()) {
                    type = MediaType.APPLICATION_OCTET_STREAM;
                }
                return JOCDefaultResponse.responseStatus200(dbItemImage.getImage(), getType(type), auditTrail);

            } else if (dbItem.getContent() != null && !dbItem.getContent().isEmpty()) {
                if ("markdown".equals(type)) {
                    return JOCDefaultResponse.responsePlainStatus200(createHTMLfromMarkdown(dbItem).getBytes(StandardCharsets.UTF_8),
                            MediaType.TEXT_PLAIN + "; charset=UTF-8", auditTrail);
                } else {
                    return JOCDefaultResponse.responsePlainStatus200(dbItem.getContent().getBytes(StandardCharsets.UTF_8), getType(type), auditTrail);
                }

            } else {
                throw new DBMissingDataException(errMessage);
            }
        } finally {
            Globals.disconnect(connection);
        }
    }

    private static String getType(String type) {
        switch (type) {
        case "xml":
        case "xsl":
        case "xsd":
            type = MediaType.APPLICATION_XML;
            break;
        case "svg":
            type = MediaType.APPLICATION_SVG_XML;
            break;
        case "pdf":
            type = "application/" + type;
            break;
        case "javascript":
        case "css":
            type = "text/" + type;
            break;
        case "json":
            type = MediaType.APPLICATION_JSON;
            break;
        case "markdown":
        case "":
            type = MediaType.TEXT_PLAIN;
            break;
        case "html":
            type = MediaType.TEXT_HTML;
            break;
        case "gif":
        case "jpeg":
        case "png":
            type = "image/" + type;
            break;
        case "icon":
            type = "image/x-icon";
            break;
        }
        if (type.startsWith("text/")) {
            type += "; charset=UTF-8";
        }
        return type;
    }

    private static String createHTMLfromMarkdown(DBItemDocumentation dbItem) {
        java.nio.file.Path path = Paths.get(dbItem.getFolder());
        String title = dbItem.getName().replaceFirst("\\.[^\\.]*$", ""); //filename without extension
        String cssFile = path.relativize(CSS).toString().replace('\\', '/');
        
        /*
         * Markdown can start with reference-style links which are invisible
         * these links can be used as header information for title and css
         * [css] : my.css
         * [title] : myTitle
         */
        Matcher markdownHeader = Pattern.compile("^((?:\\[[^\\]]+\\]\\p{Blank}*:.*\\r?\\n)+)").matcher(dbItem.getContent());
        while (markdownHeader.find()) {
            String[] lines = markdownHeader.group(1).split("\\r?\\n");
            Pattern p = Pattern.compile("\\[([^\\]]+)\\]\\p{Blank}*:(.*)");
            Matcher m;
            for (int i=0; i<lines.length; i++) {
                m = p.matcher(lines[i]);
                if (m.find()) {
                    if ("title".equals(m.group(1).toLowerCase())) {
                        title = m.group(2).trim();
                    }
                    if ("css".equals(m.group(1).toLowerCase())) {
                        cssFile = m.group(2).trim();
                    }
                }
            }
        }
        final Configuration conf = Configuration.builder().forceExtentedProfile().build();
        final String html = Processor.process(dbItem.getContent(), conf);

        boolean isCompleteHTML = false;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            String media = URLConnection.guessContentTypeFromStream(is);
            if (media != null && media.contains("html")) {
                isCompleteHTML = true; 
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
              try {
                is.close();
            } catch (IOException e) {
            }  
            }
        }

        if (isCompleteHTML) {
            return html;
        } else {
            StringBuilder s = new StringBuilder();
            s.append("<!DOCTYPE html>");
            s.append("<html>\n<head>\n");
            s.append("  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge, chrome=1\"/>\n");
            s.append("  <meta charset=\"utf-8\"/>\n");
            s.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1, minimal-ui\"/>\n");
            s.append("  <link rel=\"stylesheet\" href=\"").append(cssFile).append("\"/>\n");
            s.append("  <title>").append(title).append("</title>\n");
            s.append("</head>\n<body>\n  <article class=\"markdown-body\">\n");
            s.append(html);
            s.append("  </article>\n</body>\n</html>");
            return s.toString();
        }
    }
    
    public static void postEvent(String folder) {
        if (folder != null) {
            EventBus.getInstance().post(new DocumentationEvent(folder));
        }
    }
    
    public static void postFolderEvent(String folder) {
        if (folder != null) {
            EventBus.getInstance().post(new DocumentationFolderEvent(folder));
        }
    }

}
