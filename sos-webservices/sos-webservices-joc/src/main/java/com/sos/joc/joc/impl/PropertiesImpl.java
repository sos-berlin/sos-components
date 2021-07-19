package com.sos.joc.joc.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.IPropertiesResource;
import com.sos.joc.model.Properties;

@Path("joc")
public class PropertiesImpl extends JOCResourceImpl implements IPropertiesResource {

    private static final String API_CALL = "./joc/properties";

    @Override
    public JOCDefaultResponse postProperties(String accessToken) {

        try {
            initLogging(API_CALL, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            }
            Properties entity = new Properties();
            entity.setTitle(Globals.sosCockpitProperties.getProperty("title", ""));
            entity.setSecurityLevel(Globals.getJocSecurityLevel());
            entity.setApiVersion(Globals.apiVersion);
            entity.setInventoryVersion(Globals.inventoryVersion);
            
            ConfigurationGlobalsJoc custerSettings = Globals.getConfigurationGlobalsJoc();
            entity.setForceCommentsForAuditLog(ClusterSettings.getForceCommentsForAuditLog(custerSettings));
            entity.setComments(ClusterSettings.getCommentsForAuditLog(custerSettings));
            entity.setDefaultProfileAccount(ClusterSettings.getDefaultProfileAccount(custerSettings));
            entity.setCopy(ClusterSettings.getCopyPasteSuffixPrefix(custerSettings));
            entity.setRestore(ClusterSettings.getRestoreSuffixPrefix(custerSettings));
            entity.setImport(ClusterSettings.getImportSuffixPrefix(custerSettings));
            entity.setShowViews(ClusterSettings.getShowViews(custerSettings, true));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
