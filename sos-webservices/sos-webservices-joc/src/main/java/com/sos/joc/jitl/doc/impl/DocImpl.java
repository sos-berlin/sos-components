package com.sos.joc.jitl.doc.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.help.impl.HelpImpl;
import com.sos.joc.jitl.doc.resource.IDocResource;
import com.sos.joc.model.audit.CategoryType;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;

@Path("jitl-jobdoc")
public class DocImpl extends JOCResourceImpl implements IDocResource {

    private static final String API_CALL = "jitl-jobdoc";

    @Override
    public JOCDefaultResponse postJitlDocFile(String accessToken, String referer, String path) {
        try {
            if (path == null) {
                path = "";
            }
            String resource = String.format("/%s/%s", API_CALL, path);
            if (referer != null && referer.contains("/joc/api/" + API_CALL + "/")) {
                initLogging("." + resource, null, CategoryType.DOCUMENTATIONS);
            } else {
                initLogging("." + resource, null, accessToken, CategoryType.DOCUMENTATIONS);
                JOCDefaultResponse jocDefaultResponse = initPermissions(true);
                if (jocDefaultResponse != null) {
                    return jocDefaultResponse;
                }
            }

            checkRequiredParameter("path", path);
            final InputStream stream = HelpImpl.getInputStream(resource, API_CALL);
            
            StreamingOutput jobDocFileStream = new StreamingOutput() {

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

            return JOCDefaultResponse.responseStatus200(jobDocFileStream, HelpImpl.getMediaType(path), null, getJocAuditTrail());
        } catch (JocObjectNotExistException e) {
            return responseStatus434JSError(e, false);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
