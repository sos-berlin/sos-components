package com.sos.joc.help.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.help.resource.IHelpResource;
import com.sos.joc.model.audit.CategoryType;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Path("help-files")
public class HelpImpl extends JOCResourceImpl implements IHelpResource {

    private static final String API_CALL = "help-files";

    @Override
    public JOCDefaultResponse postHelpFile(String accessToken, String referer, String path) {
        try {
            if (path == null) {
                path = "";
            }
            String resource = String.format("/%s/%s", API_CALL, path);
            if (referer != null && referer.contains("/joc/api/" + API_CALL + "/")) {
                initLogging("." + resource, null, CategoryType.DOCUMENTATIONS);
            } else {
                initLogging("." + resource, null, accessToken, CategoryType.DOCUMENTATIONS);
                JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
                if (jocDefaultResponse != null) {
                    return jocDefaultResponse;
                }
            }

            checkRequiredParameter("path", path);
            final InputStream stream = getInputStream(resource);
            
            StreamingOutput helpFileStream = new StreamingOutput() {

                @Override
                public void write(OutputStream output) throws IOException {
                    try {
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = stream.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }
                        output.flush();
                    } finally {
                        try {
                            output.close();
                        } catch (Exception e) {
                        }
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            };

            return JOCDefaultResponse.responseStatus200(helpFileStream, getMediaType(path), null, getJocAuditTrail());
        } catch (Exception e) {
            return responseStatusJSError(e, getMediaType("html"));
        }
    }
    
    private InputStream getInputStream(String resource) throws FileNotFoundException {
        InputStream stream = JOCResourceImpl.class.getResourceAsStream(resource);
        if (stream == null) {
//            stream = JOCResourceImpl.class.getResourceAsStream(resource.substring(1));
//            if (stream == null) {
                throw new FileNotFoundException(resource);
//            }
        }
        return stream;
    }
    
    private static String getExtension(String path) {
        String filename = Paths.get(path).getFileName().toString();
        String extension = "";
        if (filename.contains(".")) {
            String[] pathParts = path.split("\\.");
            extension = pathParts[pathParts.length - 1];
        }
        return extension;
    }
    
    private static String getMediaType(String path) {
        String type = getExtension(path);
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
        case "md":
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
        case "jpg":
            type = "image/jpeg";
            break;
        case "icon":
            type = "image/x-icon";
            break;
        default:
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        if (type.startsWith("text/")) {
            type += "; charset=UTF-8";
        }
        return type;
    }

}
