package com.sos.joc.security.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.documentation.DocumentationHelper;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.documentation.impl.DocumentationResourceImpl;
import com.sos.joc.documentations.impl.DocumentationsImportResourceImpl;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.identityservice.IdentityProvider;
import com.sos.joc.model.security.identityservice.IdentityProviders;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.security.resource.IOidcResource;

import jakarta.ws.rs.Path;

@Path("iam")
public class OidcResourceImpl extends JOCResourceImpl implements IOidcResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcResourceImpl.class);

    private static final String API_CALL_IDENTITY_PROVIDERS = "./iam/identityproviders";
    private static final String API_CALL_IMPORT_ICON = "./iam/import";
    private static final String API_CALL_GET_ICON = "./iam/icon";

    private String getProperty(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @Override
    public JOCDefaultResponse postIdentityproviders() {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_IDENTITY_PROVIDERS, null);

            IdentityProviders identityProviders = new IdentityProviders();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_IDENTITY_PROVIDERS);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC);
            filter.setDisabled(false);
            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                IdentityProvider identityProvider = new IdentityProvider();
                identityProvider.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());

                JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
                JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
                jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
                jocConfigurationFilter.setName(dbItemIamIdentityService.getIdentityServiceName());
                jocConfigurationFilter.setObjectType(IdentityServiceTypes.OIDC.value());
                List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
                if (listOfJocConfigurations.size() == 1) {
                    DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                    com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                            com.sos.joc.model.security.properties.Properties.class);

                    if (properties.getOidc() != null) {

                        DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
                        String iconPath = "/sos/.images/" + dbItemIamIdentityService.getIdentityServiceName();
                        DBItemDocumentation dbItemDocumentation = dbLayer.getDocumentation(iconPath);
                        if (dbItemDocumentation != null) {
                            identityProvider.setIamIconUrl("/iam/icon" + JOCJsonCommand.urlEncodedPath(identityProvider.getIdentityServiceName()));
                        }

                        identityProvider.setIamOidcClientId(getProperty(properties.getOidc().getIamOidcClientId(), ""));
                        identityProvider.setIamOidcAuthenticationUrl(getProperty(properties.getOidc().getIamOidcAuthenticationUrl(), ""));
                        identityProvider.setIamOidcName(getProperty(properties.getOidc().getIamOidcName(), ""));
                        identityProvider.setIamOidcClientSecret(getProperty(properties.getOidc().getIamOidcClientSecret(), ""));
                    }
                }
                identityProviders.getIdentityServiceItems().add(identityProvider);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityProviders));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postImportDocumentations(String xAccessToken, String identityServiceName, FormDataBodyPart file, String timeSpent,
            String ticketLink, String comment) {
        InputStream stream = null;
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL_IMPORT_ICON, null, xAccessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getAccounts()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_IMPORT_ICON);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityServiceName);
            DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(filter);

            if (dbItemIamIdentityService == null) {
                throw new JocObjectNotExistException("Couldn't find the Identity Service <" + identityServiceName + ">");
            }

            if (file == null) {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }

            if (identityServiceName == null || identityServiceName.isEmpty()) {
                throw new JocMissingRequiredParameterException("undefined 'identityServiceName'");
            }

            AuditParams auditLog = new AuditParams();
            auditLog.setComment(comment);
            auditLog.setTicketLink(ticketLink);
            try {
                auditLog.setTimeSpent(Integer.valueOf(timeSpent));
            } catch (Exception e) {
            }

            String folder = "/sos/.images";
            final String mediaSubType = file.getMediaType().getSubtype().replaceFirst("^x-", "");
            Optional<String> supportedSubType = SOSAuthHelper.SUPPORTED_SUBTYPES.stream().filter(s -> mediaSubType.contains(s)).findFirst();

            if (supportedSubType.isPresent()) {
                DBItemJocAuditLog dbAudit = storeAuditLog(auditLog, null, CategoryType.IDENTITY, sosHibernateSession);
                DocumentationsImportResourceImpl.postImportDocumentations(folder, identityServiceName, file, new DocumentationDBLayer(
                        sosHibernateSession), dbAudit);
            } else {
                throw new JocUnsupportedFileTypeException("Unsupported image file type (" + mediaSubType + "), supported types are "
                        + DocumentationHelper.SUPPORTED_SUBTYPES.toString());
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    @Override
    public JOCDefaultResponse getIcon(String identityServiceName) {
        try {
            if (identityServiceName == null) {
                identityServiceName = "";
            }
            String request = String.format("%s/-identityServiceName-/%s", API_CALL_GET_ICON, identityServiceName);
            initLogging(request, null);

            checkRequiredParameter("identityServiceName", identityServiceName);
            return DocumentationResourceImpl.postDocumentation("sos/.images/" + identityServiceName);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseHTMLStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseHTMLStatusJSError(e, getJocError());
        }
    }

}