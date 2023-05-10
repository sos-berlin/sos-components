package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamRoleDBLayer;
import com.sos.joc.db.security.IamRoleFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.roles.Role;
import com.sos.joc.model.security.roles.RoleFilter;
import com.sos.joc.model.security.roles.RoleListFilter;
import com.sos.joc.model.security.roles.RoleRename;
import com.sos.joc.model.security.roles.RoleStore;
import com.sos.joc.model.security.roles.Roles;
import com.sos.joc.model.security.roles.RolesFilter;
import com.sos.joc.security.resource.IRoleResource;
import com.sos.schema.JsonValidator;

@Path("iam")
public class RoleResourceImpl extends JOCResourceImpl implements IRoleResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleResourceImpl.class);

    private static final String API_CALL_ROLES = "./iam/roles";
    private static final String API_CALL_ROLE_READ = "./iam/role";
    private static final String API_CALL_ROLE_STORE = "./iam/role/store";
    private static final String API_CALL_ROLE_RENAME = "./iam/role/rename";
    private static final String API_CALL_ROLE_DELETE = "./iam/role/delete";

    @Override
    public JOCDefaultResponse postRoleRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_ROLE_READ, body, accessToken);
            RoleFilter roleFilter = Globals.objectMapper.readValue(body, RoleFilter.class);
            JsonValidator.validateFailFast(body, RoleFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Role role = new Role();
            role.setControllers(new ArrayList<String>());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ROLE_READ);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ROLE_READ);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, roleFilter
                    .getIdentityServiceName());

            IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);
            IamRoleFilter filter = new IamRoleFilter();
            filter.setRoleName(roleFilter.getRoleName());
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            DBItemIamRole dbItemIamRole = iamRoleDBLayer.getUniqueRole(filter);
            if (dbItemIamRole != null) {
                role.setRoleName(roleFilter.getRoleName());
                role.setIdentityServiceName(roleFilter.getIdentityServiceName());
                filter.setRoleId(dbItemIamRole.getId());
                role.getControllers().addAll(iamRoleDBLayer.getIamControllersForRole(filter));
            } else {
                throw new JocObjectNotExistException("Couldn't find the role <" + roleFilter.getRoleName() + ">");
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(role));

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
    public JOCDefaultResponse postRoleStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_ROLE_STORE, body, accessToken);
            RoleStore roleStore = Globals.objectMapper.readValue(body, RoleStore.class);
            JsonValidator.validateFailFast(body, Role.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ROLE_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, roleStore
                    .getIdentityServiceName());

            IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);

            IamRoleFilter iamRoleFilter = new IamRoleFilter();
            iamRoleFilter.setRoleName(roleStore.getRoleName());
            iamRoleFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            DBItemIamRole dbItemIamRole = iamRoleDBLayer.getUniqueRole(iamRoleFilter);

            if (dbItemIamRole == null) {
                dbItemIamRole = new DBItemIamRole();
                dbItemIamRole.setRoleName(roleStore.getRoleName());
                dbItemIamRole.setIdentityServiceId(dbItemIamIdentityService.getId());
                if (roleStore.getOrdering() != null) {
                    dbItemIamRole.setOrdering(roleStore.getOrdering());
                } else {
                    dbItemIamRole.setOrdering(1);
                }
                sosHibernateSession.save(dbItemIamRole);
            } else {
                if (roleStore.getOrdering() != null) {
                    dbItemIamRole.setOrdering(roleStore.getOrdering());
                    sosHibernateSession.update(dbItemIamRole);
                }
            }

            storeAuditLog(roleStore.getAuditLog(), CategoryType.IDENTITY);
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

    @Override
    public JOCDefaultResponse postRoleRename(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_ROLE_RENAME, body, accessToken);
            JsonValidator.validate(body, RoleRename.class);
            RoleRename roleRename = Globals.objectMapper.readValue(body, RoleRename.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ROLE_RENAME);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, roleRename
                    .getIdentityServiceName());

            IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);

            IamRoleFilter iamRoleFilter = new IamRoleFilter();
            iamRoleFilter.setRoleName(roleRename.getRoleNewName());
            iamRoleFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            DBItemIamRole dbItemIamRole = iamRoleDBLayer.getUniqueRole(iamRoleFilter);
            if (dbItemIamRole != null) {
                JocError error = new JocError();
                error.setMessage("Role " + roleRename.getRoleNewName() + " already exists");
                throw new JocException(error);
            }

            int count = iamRoleDBLayer.renameRole(dbItemIamIdentityService.getId(), roleRename.getRoleOldName(), roleRename.getRoleNewName());
            if (count == 0) {
                throw new JocObjectNotExistException("Couldn't find the role <" + roleRename.getRoleOldName() + ">");
            }

            storeAuditLog(roleRename.getAuditLog(), CategoryType.IDENTITY);

            Globals.commit(sosHibernateSession);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (

        JocException e) {
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

    @Override
    public JOCDefaultResponse postRolesDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_ROLE_DELETE, body, accessToken);
            JsonValidator.validate(body, RolesFilter.class);
            RolesFilter rolesFilter = Globals.objectMapper.readValue(body, RolesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, rolesFilter
                    .getIdentityServiceName());

            IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);
            IamRoleFilter iamRoleFilter = new IamRoleFilter();
            iamRoleFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            for (String roleName : rolesFilter.getRoleNames()) {
                iamRoleFilter.setRoleName(roleName);
                int count = iamRoleDBLayer.deleteCascading(iamRoleFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Couldn't find the role <" + roleName + ">");
                }
            }

            Globals.commit(sosHibernateSession);

            storeAuditLog(rolesFilter.getAuditLog(), CategoryType.IDENTITY);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
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
    public JOCDefaultResponse postRoles(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_ROLES, body, accessToken);
            RoleListFilter roleListFilter = Globals.objectMapper.readValue(body, RoleListFilter.class);
            JsonValidator.validateFailFast(body, RoleListFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ROLES);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, roleListFilter
                    .getIdentityServiceName());

            Roles roles = new Roles();
            roles.setRoles(new ArrayList<Role>());

            IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);
            IamRoleFilter filter = new IamRoleFilter();
            filter.setOrderCriteria("ordering");

            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            List<DBItemIamRole> listOfRoles = iamRoleDBLayer.getIamRoleList(filter, 0);
            for (DBItemIamRole dbItemIamRole : listOfRoles) {
                Role role = new Role();
                role.setControllers(new ArrayList<String>());
                role.setRoleName(dbItemIamRole.getRoleName());
                filter.setRoleId(dbItemIamRole.getId());
                role.getControllers().addAll(iamRoleDBLayer.getIamControllersForRole(filter));
                roles.getRoles().add(role);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(roles));
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
    public JOCDefaultResponse postRolesReorder(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_ROLE_STORE, body, accessToken);
            RolesFilter roles = Globals.objectMapper.readValue(body, RolesFilter.class);
            JsonValidator.validateFailFast(body, RolesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_ROLE_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, roles
                    .getIdentityServiceName());

            IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);

            IamRoleFilter iamRoleFilter = new IamRoleFilter();
            iamRoleFilter.setIdentityServiceId(dbItemIamIdentityService.getId());

            int order = 1;
            for (String roleName : roles.getRoleNames()) {
                iamRoleFilter.setRoleName(roleName);
                DBItemIamRole dbItemIamRole = iamRoleDBLayer.getUniqueRole(iamRoleFilter);
                if (dbItemIamRole != null) {
                    dbItemIamRole.setOrdering(order);
                    sosHibernateSession.update(dbItemIamRole);
                    order = order + 1;
                }
            }

            storeAuditLog(roles.getAuditLog(), CategoryType.IDENTITY);
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