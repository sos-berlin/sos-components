package com.sos.joc.security.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.identityservice.FidoIdentityProvider;
import com.sos.joc.model.security.identityservice.IdentityProviders;
import com.sos.joc.model.security.identityservice.IdentityServiceFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.identityservice.OidcIdentityProvider;
import com.sos.joc.security.resource.IOidcResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class OidcResourceImpl extends JOCResourceImpl implements IOidcResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OidcResourceImpl.class);

    private static final String API_CALL_TOKEN = "./iam/oidc/token";
    private static final String API_CALL_IDENTITY_PROVIDERS = "./iam/identityproviders";
    private static final String API_CALL_IDENTITY_CLIENTS = "./iam/identitycliens";
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
            filter.setDisabled(false);
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC);
            List<DBItemIamIdentityService> listOfIdentityServicesOIdc = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC_JOC);
            List<DBItemIamIdentityService> listOfIdentityServicesOIdcJoc = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
            jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);

            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServicesOIdc) {
                OidcIdentityProvider oidcIdentityProvider = new OidcIdentityProvider();
                oidcIdentityProvider.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());

                jocConfigurationFilter.setName(dbItemIamIdentityService.getIdentityServiceName());
                jocConfigurationFilter.setObjectType(IdentityServiceTypes.OIDC.value());
                List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
                if (listOfJocConfigurations.size() == 1) {
                    DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                    com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                            com.sos.joc.model.security.properties.Properties.class);

                    if (properties.getOidc() != null) {

                        DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
                        String iconPath = DocumentationDBLayer.SOS_IMAGES_FOLDER + "/" + dbItemIamIdentityService.getIdentityServiceName();
                        DBItemDocumentation dbItemDocumentation = dbLayer.getDocumentation(iconPath);
                        if (dbItemDocumentation != null) {
                            oidcIdentityProvider.setIamIconUrl("/iam/icon/" + JOCJsonCommand.urlEncodedPath(oidcIdentityProvider
                                    .getIdentityServiceName()));
                        }

                        oidcIdentityProvider.setIamOidcAuthenticationUrl(getProperty(properties.getOidc().getIamOidcAuthenticationUrl(), ""));
                        oidcIdentityProvider.setIamOidcName(getProperty(properties.getOidc().getIamOidcName(), ""));
                    }
                }
                identityProviders.getOidcServiceItems().add(oidcIdentityProvider);
            }

            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServicesOIdcJoc) {
                OidcIdentityProvider oidcIdentityProvider = new OidcIdentityProvider();
                oidcIdentityProvider.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());

                jocConfigurationFilter.setName(dbItemIamIdentityService.getIdentityServiceName());
                jocConfigurationFilter.setObjectType(IdentityServiceTypes.OIDC_JOC.value());
                List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
                if (listOfJocConfigurations.size() == 1) {
                    DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                    com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                            com.sos.joc.model.security.properties.Properties.class);

                    if (properties.getOidc() != null) {

                        DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
                        String iconPath = DocumentationDBLayer.SOS_IMAGES_FOLDER + "/" + dbItemIamIdentityService.getIdentityServiceName();
                        DBItemDocumentation dbItemDocumentation = dbLayer.getDocumentation(iconPath);
                        if (dbItemDocumentation != null) {
                            oidcIdentityProvider.setIamIconUrl("/iam/icon/" + JOCJsonCommand.urlEncodedPath(oidcIdentityProvider
                                    .getIdentityServiceName()));
                        }

                        oidcIdentityProvider.setIamOidcAuthenticationUrl(getProperty(properties.getOidc().getIamOidcAuthenticationUrl(), ""));
                        oidcIdentityProvider.setIamOidcName(getProperty(properties.getOidc().getIamOidcName(), ""));
                    }
                }
                identityProviders.getOidcServiceItems().add(oidcIdentityProvider);
            }

            filter.setIamIdentityServiceType(IdentityServiceTypes.FIDO);
            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                FidoIdentityProvider fidoIdentityProvider = new FidoIdentityProvider();
                fidoIdentityProvider.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());

                com.sos.joc.model.security.properties.Properties properties = SOSAuthHelper.getIamProperties(dbItemIamIdentityService
                        .getIdentityServiceName());

                if (properties != null) {
                    if (properties.getFido() != null) {

                        DocumentationDBLayer dbLayer = new DocumentationDBLayer(sosHibernateSession);
                        String iconPath = DocumentationDBLayer.SOS_IMAGES_FOLDER + "/" + dbItemIamIdentityService.getIdentityServiceName();
                        DBItemDocumentation dbItemDocumentation = dbLayer.getDocumentation(iconPath);
                        if (dbItemDocumentation != null) {
                            fidoIdentityProvider.setIamIconUrl("/iam/icon/" + JOCJsonCommand.urlEncodedPath(fidoIdentityProvider
                                    .getIdentityServiceName()));
                        }
                    }
                }
                if (dbItemIamIdentityService.getSecondFactor()) {
                    identityProviders.getFido2ndFactorServiceItems().add(fidoIdentityProvider);
                } else {
                    identityProviders.getFidoServiceItems().add(fidoIdentityProvider);
                }
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
    public JOCDefaultResponse postIdentityclient(byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_IDENTITY_CLIENTS, body);
            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);
            IdentityServiceFilter identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);

            checkRequiredParameter("identityServiceName", identityServiceFilter.getIdentityServiceName());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_IDENTITY_CLIENTS);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC);
            filter.setDisabled(false);
            filter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
            List<DBItemIamIdentityService> listOfIdentityServicesOidc = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC);
            List<DBItemIamIdentityService> listOfIdentityServicesOidcJoc = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);

            OidcIdentityProvider identityProvider = new OidcIdentityProvider();

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
            jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);

            if (listOfIdentityServicesOidcJoc.size() > 0) {

                identityProvider.setIdentityServiceName(listOfIdentityServicesOidcJoc.get(0).getIdentityServiceName());

                jocConfigurationFilter.setName(listOfIdentityServicesOidcJoc.get(0).getIdentityServiceName());
                jocConfigurationFilter.setObjectType(IdentityServiceTypes.OIDC_JOC.value());
                List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
                if (listOfJocConfigurations.size() == 1) {
                    DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                    com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                            com.sos.joc.model.security.properties.Properties.class);

                    if (properties.getOidc() != null) {
                        identityProvider.setIamOidcClientId(getProperty(properties.getOidc().getIamOidcClientId(), ""));
                        identityProvider.setIamOidcClientSecret(getProperty(properties.getOidc().getIamOidcClientSecret(), ""));
                    }
                }
            }

            if (listOfIdentityServicesOidc.size() > 0) {
                identityProvider.setIdentityServiceName(listOfIdentityServicesOidc.get(0).getIdentityServiceName());
                jocConfigurationFilter.setName(listOfIdentityServicesOidc.get(0).getIdentityServiceName());
                jocConfigurationFilter.setObjectType(IdentityServiceTypes.OIDC.value());
                List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
                if (listOfJocConfigurations.size() == 1) {
                    DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                    com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                            com.sos.joc.model.security.properties.Properties.class);

                    if (properties.getOidc() != null) {
                        identityProvider.setIamOidcClientId(getProperty(properties.getOidc().getIamOidcClientId(), ""));
                        identityProvider.setIamOidcClientSecret(getProperty(properties.getOidc().getIamOidcClientSecret(), ""));
                    }
                }
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityProvider));
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

            final String mediaSubType = file.getMediaType().getSubtype().replaceFirst("^x-", "");
            Optional<String> supportedSubType = SOSAuthHelper.SUPPORTED_SUBTYPES.stream().filter(s -> mediaSubType.contains(s)).findFirst();

            if (supportedSubType.isPresent()) {
                DBItemJocAuditLog dbAudit = storeAuditLog(auditLog, null, CategoryType.IDENTITY, sosHibernateSession);
                DocumentationsImportResourceImpl.postImportDocumentations(DocumentationDBLayer.SOS_IMAGES_FOLDER, identityServiceName, file,
                        new DocumentationDBLayer(sosHibernateSession), dbAudit);
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
            String request = String.format("%s/%s", API_CALL_GET_ICON, identityServiceName);
            initLogging(request, null);

            checkRequiredParameter("identityServiceName", identityServiceName);
            return DocumentationResourceImpl.postDocumentation(DocumentationDBLayer.SOS_IMAGES_FOLDER + "/" + identityServiceName);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseHTMLStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseHTMLStatusJSError(e, getJocError());
        }
    }

}