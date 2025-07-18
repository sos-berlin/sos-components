package com.sos.joc.configuration.impl;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.common.FilenameSanitizer;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.configuration.resource.ILoginConfigurationResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.configuration.Login;
import com.sos.joc.model.configuration.LoginLogo;
import com.sos.joc.model.configuration.LoginLogoPosition;

import jakarta.ws.rs.Path;

@Path("configuration")
public class LoginConfigurationResourceImpl extends JOCResourceImpl implements ILoginConfigurationResource {

    private static final String API_CALL = "./configuration/login";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginConfigurationResourceImpl.class);
    private static final String LOGO_LOCATION = "webapps/root/ext/images/";

    @Override
    public JOCDefaultResponse postLoginConfiguration() {
        return getLoginConfiguration();
    }

    @Override
    public JOCDefaultResponse getLoginConfiguration() {
        try {
            initLogging(API_CALL, "{}".getBytes(), CategoryType.SETTINGS);
            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            }
            Login login = new Login();
            login.setTitle(Globals.sosCockpitProperties.getProperty("title", ""));
            login.setEnableRememberMe(ClusterSettings.getEnableRememberMe(Globals.getConfigurationGlobalsJoc()));
            String logoName = Globals.sosCockpitProperties.getProperty("custom_logo_name", "").trim();
            if (logoName != null && !logoName.isEmpty()) {
                try {
                    FilenameSanitizer.test("custom_logo_name", logoName);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("joc.properties: " + e.getMessage());
                    logoName = "";
                }
                java.nio.file.Path p = Paths.get(LOGO_LOCATION + logoName);
                if (!Files.exists(p)) {
                    LOGGER.warn("logo image '" + p.toString() + "' doesn't exist but configured.");
                    logoName = "";
                }
            }
            if (logoName != null && !logoName.isEmpty()) {
                LoginLogo loginLogo = new LoginLogo();
                loginLogo.setName(logoName);
                
                String regEx = "(\\d+(cm|mm|in|px|pt|pc|em|ex|ch|rem|vw|vh|vmin|vmax|%)|auto)";
                String logoHeight = Globals.sosCockpitProperties.getProperty("custom_logo_height", "").trim();
                if (logoHeight.matches("\\d+")) {
                    loginLogo.setHeight(logoHeight + "px");
                } else if (logoHeight.matches(regEx)) {
                    loginLogo.setHeight(logoHeight);
                } else {
                    LOGGER.warn("logo height '" + logoHeight + "' doesn't match " + regEx);
                }
                
                String logoPosition = Globals.sosCockpitProperties.getProperty("custom_logo_position", "").trim();
                try {
                    loginLogo.setPosition(LoginLogoPosition.fromValue(logoPosition.toUpperCase()));
                } catch (Exception e) {
                    loginLogo.setPosition(LoginLogoPosition.BOTTOM);
                }
                login.setCustomLogo(loginLogo);
            }
            login.setDefaultProfileAccount(ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc()));
            
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(login));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}