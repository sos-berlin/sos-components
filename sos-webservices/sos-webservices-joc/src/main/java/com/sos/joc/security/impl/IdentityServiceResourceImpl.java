package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.IdentityService;
import com.sos.joc.model.security.IdentityServiceFilter;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.IdentityServices;
import com.sos.joc.security.resource.IIdentityServiceResource;
import com.sos.schema.JsonValidator;

@Path("iam")
public class IdentityServiceResourceImpl extends JOCResourceImpl implements IIdentityServiceResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityServiceResourceImpl.class);

    private static final String API_CALL_SERVICES = "./iam/identityservices";
    private static final String API_CALL_SERVICES_READ = "./iam/identityservice";
    private static final String API_CALL_SERVICES_STORE = "./iam/identityservice/store";
    private static final String API_CALL_SERVICES_DELETE = "./iam/identityservice/delete";

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
                identityService.setOrdering(dbItemIamIdentityService.getOrdering());
                identityService.setRequired(dbItemIamIdentityService.getRequierd());
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
            }
            dbItemIamIdentityService.setDisabled(identityService.getDisabled());
            dbItemIamIdentityService.setIdentityServiceType(identityService.getIdentityServiceType().value());
            dbItemIamIdentityService.setOrdering(identityService.getOrdering());
            dbItemIamIdentityService.setRequierd(identityService.getRequired());

            if (dbItemIamIdentityService.getId() == null) {
                sosHibernateSession.save(dbItemIamIdentityService);
            } else {
                sosHibernateSession.update(dbItemIamIdentityService);
            }
 
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

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SERVICES_READ);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityServiceFilter.getIdentityServiceName());
            iamIdentityServiceDBLayer.delete(filter);
            filter.setIdentityServiceName(null);
            if (iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0).size() == 0) {
                LOGGER.info("It is not possible to delete the last Identity Service");
                Globals.rollback(sosHibernateSession);
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
                identityService.setOrdering(dbItemIamIdentityService.getOrdering());
                identityService.setRequired(dbItemIamIdentityService.getRequierd());
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

}