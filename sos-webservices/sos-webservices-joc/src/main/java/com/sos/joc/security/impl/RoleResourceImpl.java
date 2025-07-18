package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

import jakarta.ws.rs.Path;

@Path("iam")
public class RoleResourceImpl extends JOCResourceImpl implements IRoleResource {

    private static final String API_CALL_ROLES = "./iam/roles";
    private static final String API_CALL_ROLE_READ = "./iam/role";
    private static final String API_CALL_ROLE_STORE = "./iam/role/store";
    private static final String API_CALL_ROLE_RENAME = "./iam/role/rename";
    private static final String API_CALL_ROLE_DELETE = "./iam/role/delete";

    @Override
    public JOCDefaultResponse postRoleRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_ROLE_READ, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, RoleFilter.class);
            RoleFilter roleFilter = Globals.objectMapper.readValue(body, RoleFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Role role = new Role();
            role.setControllers(new ArrayList<String>());

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
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(role));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRoleStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_ROLE_STORE, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, Role.class);
            RoleStore roleStore = Globals.objectMapper.readValue(body, RoleStore.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
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

            storeAuditLog(roleStore.getAuditLog());
            Globals.commit(sosHibernateSession);

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRoleRename(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            body = initLogging(API_CALL_ROLE_RENAME, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, RoleRename.class);
            RoleRename roleRename = Globals.objectMapper.readValue(body, RoleRename.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
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

            storeAuditLog(roleRename.getAuditLog());

            Globals.commit(sosHibernateSession);

            return responseStatusJSOk(Date.from(Instant.now()));

        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRolesDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            body = initLogging(API_CALL_ROLE_DELETE, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, RolesFilter.class);
            RolesFilter rolesFilter = Globals.objectMapper.readValue(body, RolesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
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

            storeAuditLog(rolesFilter.getAuditLog());

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRoles(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_ROLES, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, RoleListFilter.class);
            RoleListFilter roleListFilter = Globals.objectMapper.readValue(body, RoleListFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
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

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(roles));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postRolesReorder(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_ROLE_STORE, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, RolesFilter.class);
            RolesFilter roles = Globals.objectMapper.readValue(body, RolesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
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

            storeAuditLog(roles.getAuditLog());
            Globals.commit(sosHibernateSession);

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}