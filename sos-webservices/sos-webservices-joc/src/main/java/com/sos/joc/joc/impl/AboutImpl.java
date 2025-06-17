package com.sos.joc.joc.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.core.MediaType;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.IAboutResource;
import com.sos.joc.model.Version;
import com.sos.joc.model.audit.CategoryType;

@jakarta.ws.rs.Path("")
public class AboutImpl extends JOCResourceImpl implements IAboutResource {
    
    @Override
    public JOCDefaultResponse postVersion(String accept) {
        return getAbout(accept, "./version");
    }

    @Override
    public JOCDefaultResponse getVersion(String accept) {
        return getAbout(accept, "./version");
    }

    @Override
    public JOCDefaultResponse postAbout(String accept) {
        return getAbout(accept, "./about");
    }

    @Override
    public JOCDefaultResponse getAbout(String accept) {
        return getAbout(accept, "./about");
    }

    public JOCDefaultResponse getAbout(String accept, String apiCall) {
        String mediaType = MediaType.TEXT_HTML + "; charset=UTF-8";
        if (MediaType.APPLICATION_JSON.equalsIgnoreCase(accept)) {
            mediaType = MediaType.APPLICATION_JSON;
        }
        try {
            initLogging(apiCall, null, CategoryType.OTHERS);
            return responseStatus200(readVersion(mediaType), mediaType);
        } catch (Exception e) {
            return responseStatusJSError(e, mediaType);
        }
    }

    private byte[] readVersion(String mediaType) throws JocException {
        InputStream stream = null;
        String versionFile = "/version.json";
        try {
            stream = this.getClass().getClassLoader().getResourceAsStream(versionFile);
            if (stream != null) {
                Version v = Globals.objectMapper.readValue(stream, Version.class);
                return MediaType.TEXT_PLAIN.equals(mediaType) ? versionClassToString(v) : Globals.objectMapper.writeValueAsBytes(v);
            } else {
                throw new JocException(new JocError("JOC-002", String.format("Couldn't find version file %1$s in classpath", versionFile)));
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocException(new JocError("JOC-002", String.format("Error while reading %1$s from classpath: ", versionFile)), e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }
    
    private byte[] versionClassToString(Version v) {
        return String.format("version: %s%ngitHash: %s%ndate: %s%n", v.getVersion(), v.getGitHash(), v.getDate()).getBytes(StandardCharsets.UTF_8);
    }

}
