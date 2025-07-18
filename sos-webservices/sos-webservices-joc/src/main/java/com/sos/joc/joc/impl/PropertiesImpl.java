package com.sos.joc.joc.impl;

import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsKiosk;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsUser;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.IPropertiesResource;
import com.sos.joc.model.Properties;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.joc.LicenseType;

@Path("joc")
public class PropertiesImpl extends JOCResourceImpl implements IPropertiesResource {

    private static final String API_CALL = "./joc/properties";

    @Override
    public JOCDefaultResponse postProperties(String accessToken) {

        try {
            initLogging(API_CALL, "{}".getBytes(), accessToken, CategoryType.OTHERS);
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
            
            ConfigurationGlobalsJoc jocSettings = Globals.getConfigurationGlobalsJoc();
            entity.setForceCommentsForAuditLog(ClusterSettings.getForceCommentsForAuditLog(jocSettings));
            entity.setComments(ClusterSettings.getCommentsForAuditLog(jocSettings));
            entity.setDefaultProfileAccount(ClusterSettings.getDefaultProfileAccount(jocSettings));
            entity.setCopy(ClusterSettings.getCopyPasteSuffixPrefix(jocSettings));
            entity.setRestore(ClusterSettings.getRestoreSuffixPrefix(jocSettings));
            entity.setImport(ClusterSettings.getImportSuffixPrefix(jocSettings));
            entity.setShowViews(ClusterSettings.getShowViews(jocSettings, true));
            entity.setDisplayFoldersInViews(jocSettings.getDisplayFoldersInViews());
            entity.setAllowEmptyArguments(ClusterSettings.getAllowEmptyArguments(jocSettings));
            entity.setAllowUndeclaredVariables(jocSettings.getAllowUndeclaredVariables());
            entity.setNumOfTagsDisplayedAsOrderId(jocSettings.getNumOfTagsDisplayedAsOrderId());
            entity.setNumOfWorkflowTagsDisplayed(jocSettings.getNumOfWorkflowTagsDisplayed());
            
            ConfigurationGlobalsKiosk kioskSettings = Globals.getConfigurationGlobalsKiosk();
            entity.setKioskRole(ClusterSettings.getKioskRole(kioskSettings));
            entity.setKioskViews(ClusterSettings.getKioskViews(kioskSettings));
            
            ConfigurationGlobalsUser userSettings = Globals.getConfigurationGlobalsUser();
            entity.setWelcomeDoNotRemindMe(ClusterSettings.getWelcomeDoNotRemindMe(userSettings));
            entity.setWelcomeGotIt(ClusterSettings.getWelcomeGotIt(userSettings));
            
            if(JocClusterService.getInstance().getCluster() != null) {
                JocClusterService.getInstance().getCluster().getConfig().rereadClusterMode();
                entity.setClusterLicense(JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult().getUse());
                entity.setLicenseValidFrom(JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult().getValidFrom());
                entity.setLicenseValidUntil(JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult().getValidUntil());
                if (entity.getLicenseValidFrom() == null && entity.getLicenseValidUntil() == null && !entity.getClusterLicense()) {
                    entity.setLicenseType(LicenseType.OPENSOURCE);
                } else {
                    if(entity.getClusterLicense()) {
                        entity.setLicenseType(LicenseType.COMMERCIAL_VALID);
                    } else {
                        entity.setLicenseType(LicenseType.COMMERCIAL_INVALID);
                    }
                }
            } else {
                entity.setClusterLicense(false);
                entity.setLicenseType(LicenseType.OPENSOURCE);
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
