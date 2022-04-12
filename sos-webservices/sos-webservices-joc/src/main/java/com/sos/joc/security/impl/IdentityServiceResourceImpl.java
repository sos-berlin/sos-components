package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.identityservice.IdentityService;
import com.sos.joc.model.security.identityservice.IdentityServiceAuthenticationScheme;
import com.sos.joc.model.security.identityservice.IdentityServiceFilter;
import com.sos.joc.model.security.identityservice.IdentityServiceRename;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.identityservice.IdentityServices;
import com.sos.joc.model.security.identityservice.IdentityServicesFilter;
import com.sos.joc.security.resource.IIdentityServiceResource;
import com.sos.schema.JsonValidator;

@Path("iam")
public class IdentityServiceResourceImpl extends JOCResourceImpl implements IIdentityServiceResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityServiceResourceImpl.class);

    private static final String API_CALL_SERVICES = "./iam/identityservices";
    private static final String API_CALL_SERVICES_READ = "./iam/identityservice";
    private static final String API_CALL_SERVICES_STORE = "./iam/identityservice/store";
    private static final String API_CALL_SERVICES_DELETE = "./iam/identityservice/delete";
    private static final String API_CALL_SERVICES_REORDER = "./iam/identityservices/reorder";

    @Override
    public JOCDefaultResponse postIdentityServiceRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            IdentityServiceFilter identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);
            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);

            initLogging(API_CALL_SERVICES_READ, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            IdentityService identityService = new IdentityService();

            this.checkRequiredParameter("identityServiceName", identityServiceFilter.getIdentityServiceName());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SERVICES_READ);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
            DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(filter);
            if (dbItemIamIdentityService != null) {
                identityService.setDisabled(dbItemIamIdentityService.getDisabled());
                identityService.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                identityService.setIdentityServiceType(IdentityServiceTypes.fromValue(dbItemIamIdentityService.getIdentityServiceType()));
                identityService.setServiceAuthenticationScheme(IdentityServiceAuthenticationScheme.fromValue(dbItemIamIdentityService
                        .getAuthenticationScheme()));
                identityService.setSingleFactorCert(dbItemIamIdentityService.getSingleFactorCert());
                identityService.setSingleFactorPwd(dbItemIamIdentityService.getSingleFactorPwd());
                identityService.setOrdering(dbItemIamIdentityService.getOrdering());
                identityService.setRequired(dbItemIamIdentityService.getRequired());
            } else {
                throw new JocObjectNotExistException("Object identity service <" + identityServiceFilter.getIdentityServiceName() + "> not found");
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityService));
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
    public JOCDefaultResponse postIdentityServiceStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            IdentityService identityService = Globals.objectMapper.readValue(body, IdentityService.class);
            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);

            initLogging(API_CALL_SERVICES_STORE, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("identityServiceName", identityService.getIdentityServiceName());
            this.checkRequiredParameter("identityServiceType", identityService.getIdentityServiceName());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SERVICES_READ);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityService.getIdentityServiceName());
            DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(filter);
            if (dbItemIamIdentityService == null) {
                dbItemIamIdentityService = new DBItemIamIdentityService();
                dbItemIamIdentityService.setIdentityServiceName(identityService.getIdentityServiceName());
                if (identityService.getOrdering() != null) {
                    dbItemIamIdentityService.setOrdering(9999);
                } else {
                    dbItemIamIdentityService.setOrdering(identityService.getOrdering());
                }
            }
            dbItemIamIdentityService.setDisabled(identityService.getDisabled());
            dbItemIamIdentityService.setIdentityServiceType(identityService.getIdentityServiceType().value());
            if (identityService.getOrdering() != null) {
                dbItemIamIdentityService.setOrdering(identityService.getOrdering());
            }
            dbItemIamIdentityService.setRequired(identityService.getRequired());
            if (identityService.getServiceAuthenticationScheme() != null) {
                dbItemIamIdentityService.setAuthenticationScheme(identityService.getServiceAuthenticationScheme().value());
            } else {
                dbItemIamIdentityService.setAuthenticationScheme(IdentityServiceAuthenticationScheme.SINGLE_FACTOR.value());
            }
            if (identityService.getSingleFactorPwd() != null) {
                dbItemIamIdentityService.setSingleFactorPwd(identityService.getSingleFactorPwd());
            } else {
                dbItemIamIdentityService.setSingleFactorPwd(true);
            }
            if (identityService.getSingleFactorCert() != null) {
                dbItemIamIdentityService.setSingleFactorCert(identityService.getSingleFactorCert());
            } else {
                dbItemIamIdentityService.setSingleFactorCert(false);
            }

            if (dbItemIamIdentityService.getId() == null) {
                sosHibernateSession.save(dbItemIamIdentityService);
            } else {
                sosHibernateSession.update(dbItemIamIdentityService);
            }

            DBItemJocAuditLog dbAuditLog = storeAuditLog(identityService.getAuditLog(), CategoryType.IDENTITY);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityService));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.commit(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }

    }

    @Override
    public JOCDefaultResponse postIdentityServiceRename(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            IdentityServiceRename identityServiceRename = Globals.objectMapper.readValue(body, IdentityServiceRename.class);
            JsonValidator.validateFailFast(body, IdentityServiceRename.class);

            initLogging(API_CALL_SERVICES_STORE, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("identityServiceOldName", identityServiceRename.getIdentityServiceOldName());
            this.checkRequiredParameter("identityServiceNewName", identityServiceRename.getIdentityServiceNewName());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SERVICES_READ);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityServiceRename.getIdentityServiceOldName());
            DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(filter);
            if (dbItemIamIdentityService == null) {
                throw new JocObjectNotExistException("Object Identity Service <" + identityServiceRename.getIdentityServiceOldName() + "> not found");
            }
            iamIdentityServiceDBLayer.rename(identityServiceRename.getIdentityServiceOldName(), identityServiceRename.getIdentityServiceNewName());

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);

            JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
            jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
            jocConfigurationFilter.setName(identityServiceRename.getIdentityServiceOldName());
            jocConfigurationFilter.setObjectType(dbItemIamIdentityService.getIdentityServiceType());
            jocConfigurationDBLayer.rename(jocConfigurationFilter, identityServiceRename.getIdentityServiceNewName());

            storeAuditLog(identityServiceRename.getAuditLog(), CategoryType.IDENTITY);

            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityServiceRename));
        } catch (JocException e) {
            Globals.rollback(sosHibernateSession);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    @Override
    public JOCDefaultResponse postIdentityServiceDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            IdentityServiceFilter identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);
            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);

            initLogging(API_CALL_SERVICES_DELETE, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("identityServiceName", identityServiceFilter.getIdentityServiceName());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SERVICES_DELETE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
            int count = iamIdentityServiceDBLayer.deleteCascading(filter);
            if (count == 0) {
                throw new JocObjectNotExistException("Object Identity Service<" + identityServiceFilter.getIdentityServiceName() + "> not found");
            }

            storeAuditLog(identityServiceFilter.getAuditLog(), CategoryType.IDENTITY);

            filter.setIdentityServiceName(null);
            if (iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0).size() == 0) {
                LOGGER.info("It is not possible to delete the last Identity Service");
                Globals.rollback(sosHibernateSession);
            } else {
                Globals.commit(sosHibernateSession);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(sosHibernateSession);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    @Override
    public JOCDefaultResponse postIdentityServices(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            IdentityServiceFilter identityServiceFilter = Globals.objectMapper.readValue(body, IdentityServiceFilter.class);
            JsonValidator.validateFailFast(body, IdentityServiceFilter.class);

            initLogging(API_CALL_SERVICES, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            IdentityServices identityServices = new IdentityServices();
            identityServices.setIdentityServiceItems(new ArrayList<IdentityService>());
            identityServices.setIdentityServiceTypes(new ArrayList<IdentityServiceTypes>());

            identityServices.getIdentityServiceTypes().add(IdentityServiceTypes.JOC);
            identityServices.getIdentityServiceTypes().add(IdentityServiceTypes.LDAP);
            identityServices.getIdentityServiceTypes().add(IdentityServiceTypes.LDAP_JOC);
            identityServices.getIdentityServiceTypes().add(IdentityServiceTypes.SHIRO);
            identityServices.getIdentityServiceTypes().add(IdentityServiceTypes.VAULT);
            identityServices.getIdentityServiceTypes().add(IdentityServiceTypes.VAULT_JOC);
            identityServices.getIdentityServiceTypes().add(IdentityServiceTypes.VAULT_JOC_ACTIVE);

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SERVICES_READ);
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
            List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
            for (DBItemIamIdentityService dbItemIamIdentityService : listOfIdentityServices) {
                IdentityService identityService = new IdentityService();
                identityService.setDisabled(dbItemIamIdentityService.getDisabled());
                identityService.setIdentityServiceName(dbItemIamIdentityService.getIdentityServiceName());
                identityService.setIdentityServiceType(IdentityServiceTypes.fromValue(dbItemIamIdentityService.getIdentityServiceType()));
                identityService.setServiceAuthenticationScheme(IdentityServiceAuthenticationScheme.fromValue(dbItemIamIdentityService
                        .getAuthenticationScheme()));
                identityService.setSingleFactorCert(dbItemIamIdentityService.getSingleFactorCert());
                identityService.setSingleFactorPwd(dbItemIamIdentityService.getSingleFactorPwd());
                identityService.setOrdering(dbItemIamIdentityService.getOrdering());
                identityService.setRequired(dbItemIamIdentityService.getRequired());
                identityServices.getIdentityServiceItems().add(identityService);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(identityServices));
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
    public JOCDefaultResponse postIdentityServicesReorder(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            IdentityServicesFilter identityServices = Globals.objectMapper.readValue(body, IdentityServicesFilter.class);

            JsonValidator.validateFailFast(body, IdentityServicesFilter.class);

            initLogging(API_CALL_SERVICES_REORDER, Globals.objectMapper.writeValueAsBytes(identityServices), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SERVICES_REORDER);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);

            IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();

            int order = 1;
            for (String identityServiceName : identityServices.getIdentityServiceNames()) {
                iamIdentityServiceFilter.setIdentityServiceName(identityServiceName);
                DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
                if (dbItemIamIdentityService != null) {
                    dbItemIamIdentityService.setOrdering(order);
                    sosHibernateSession.update(dbItemIamIdentityService);
                    order = order + 1;
                }
            }

            storeAuditLog(identityServices.getAuditLog(), CategoryType.IDENTITY);
            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}