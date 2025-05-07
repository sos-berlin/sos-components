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
import com.sos.joc.db.authentication.DBItemIamPermission;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamPermissionDBLayer;
import com.sos.joc.db.security.IamPermissionFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.permissions.Permission;
import com.sos.joc.model.security.permissions.PermissionFilter;
import com.sos.joc.model.security.permissions.PermissionItem;
import com.sos.joc.model.security.permissions.PermissionListFilter;
import com.sos.joc.model.security.permissions.PermissionRename;
import com.sos.joc.model.security.permissions.Permissions;
import com.sos.joc.model.security.permissions.PermissionsFilter;
import com.sos.joc.security.resource.IPermissionResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class PermissionResourceImpl extends JOCResourceImpl implements IPermissionResource {

    private static final String API_CALL_PERMISSIONS = "./iam/permissions";
    private static final String API_CALL_PERMISSION_READ = "./iam/permission";
    private static final String API_CALL_PERMISSIONS_STORE = "./iam/permissions/store";
    private static final String API_CALL_PERMISSION_RENAME = "./iam/permission/rename";
    private static final String API_CALL_PERMISSIONS_DELETE = "./iam/permissions/delete";

    @Override
    public JOCDefaultResponse postPermissionRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_PERMISSION_READ, body, accessToken);
            JsonValidator.validateFailFast(body, PermissionFilter.class);
            PermissionFilter permissionFilter = Globals.objectMapper.readValue(body, PermissionFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            PermissionItem permissionItem = new PermissionItem();

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_PERMISSION_READ);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, permissionFilter
                    .getIdentityServiceName());
            DBItemIamRole dbItemIamRole = SOSAuthHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), permissionFilter
                    .getRoleName());

            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            IamPermissionFilter filter = new IamPermissionFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setControllerId(permissionFilter.getControllerId());
            filter.setRoleId(dbItemIamRole.getId());
            filter.setPermission(permissionFilter.getPermissionPath());

            DBItemIamPermission dbItemIamPermission = iamPermissionDBLayer.getUniquePermission(filter);
            if (dbItemIamPermission != null) {
                Permission permission = new Permission();
                permission.setExcluded(dbItemIamPermission.getExcluded());
                permission.setPermissionPath(dbItemIamPermission.getAccountPermission());
                permissionItem.setControllerId(dbItemIamPermission.getControllerId());
                permissionItem.setIdentityServiceName(permissionFilter.getIdentityServiceName());
                permissionItem.setRoleName(permissionFilter.getRoleName());

                permissionItem.setPermission(permission);
            } else {
                throw new JocObjectNotExistException("Couldn't find the permission <" + permissionFilter.getPermissionPath() + ">");
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(permissionItem));

        } catch (

        JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postPermissionsStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        Permissions permissions = null;
        try {

            body = initLogging(API_CALL_PERMISSIONS_STORE, body, accessToken);
            JsonValidator.validateFailFast(body, Permissions.class);
            permissions = Globals.objectMapper.readValue(body, Permissions.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getAccounts()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_PERMISSIONS_STORE);
            sosHibernateSession.setAutoCommit(false);

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, permissions
                    .getIdentityServiceName());
            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            DBItemIamRole dbItemIamRole = SOSAuthHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), permissions.getRoleName());

            IamPermissionFilter iamPermissionFilter = new IamPermissionFilter();
            iamPermissionFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamPermissionFilter.setControllerId(permissions.getControllerId());
            iamPermissionFilter.setRoleId(dbItemIamRole.getId());

            for (Permission permission : permissions.getPermissions()) {
                sosHibernateSession.beginTransaction();
                iamPermissionFilter.setPermission(permission.getPermissionPath());
                DBItemIamPermission dbItemIamPermission = iamPermissionDBLayer.getUniquePermission(iamPermissionFilter);
                boolean newPermission = false;
                if (dbItemIamPermission == null) {
                    dbItemIamPermission = new DBItemIamPermission();
                    dbItemIamPermission.setControllerId(permissions.getControllerId());
                    dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
                    dbItemIamPermission.setRoleId(dbItemIamRole.getId());
                    newPermission = true;
                }

                dbItemIamPermission.setRecursive(false);
                dbItemIamPermission.setAccountPermission(permission.getPermissionPath());
                dbItemIamPermission.setExcluded(permission.getExcluded());

                if (newPermission) {
                    sosHibernateSession.save(dbItemIamPermission);
                } else {
                    sosHibernateSession.update(dbItemIamPermission);
                }
                Globals.commit(sosHibernateSession);
            }

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
            if (permissions != null) {
                storeAuditLog(permissions.getAuditLog(), CategoryType.IDENTITY);
            }
        }
    }

    @Override
    public JOCDefaultResponse postPermissionRename(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            body = initLogging(API_CALL_PERMISSION_RENAME, body, accessToken);
            JsonValidator.validate(body, PermissionRename.class);
            PermissionRename permissionRename = Globals.objectMapper.readValue(body, PermissionRename.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getAccounts()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_PERMISSION_RENAME);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, permissionRename
                    .getIdentityServiceName());
            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            DBItemIamRole dbItemIamRole = SOSAuthHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), permissionRename
                    .getRoleName());

            int count = iamPermissionDBLayer.renamePermission(dbItemIamIdentityService.getId(), dbItemIamRole.getId(), permissionRename
                    .getControllerId(), permissionRename.getOldPermissionPath(), permissionRename.getNewPermission().getPermissionPath(),
                    permissionRename.getNewPermission().getExcluded());

            if (count == 0) {
                throw new JocObjectNotExistException("Couldn't find the permission <" + permissionRename.getOldPermissionPath() + ">");
            }

            storeAuditLog(permissionRename.getAuditLog(), CategoryType.IDENTITY);

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
    public JOCDefaultResponse postPermissionsDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            body = initLogging(API_CALL_PERMISSIONS_DELETE, body, accessToken);
            JsonValidator.validate(body, PermissionsFilter.class);
            PermissionsFilter permissionsFilter = Globals.objectMapper.readValue(body, PermissionsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getAccounts()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, permissionsFilter
                    .getIdentityServiceName());
            DBItemIamRole dbItemIamRole = SOSAuthHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), permissionsFilter
                    .getRoleName());

            IamPermissionFilter iamPermissionFilter = new IamPermissionFilter();
            iamPermissionFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamPermissionFilter.setControllerId(permissionsFilter.getControllerId());
            iamPermissionFilter.setRoleId(dbItemIamRole.getId());

            for (String permission : permissionsFilter.getPermissionPaths()) {
                iamPermissionFilter.setPermission(permission);
                int count = iamPermissionDBLayer.delete(iamPermissionFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Couldn't find the permission <" + permission + ">");
                }
            }
            Globals.commit(sosHibernateSession);

            storeAuditLog(permissionsFilter.getAuditLog(), CategoryType.IDENTITY);

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
    public JOCDefaultResponse postPermissions(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_PERMISSIONS, body, accessToken);
            JsonValidator.validateFailFast(body, PermissionListFilter.class);
            PermissionListFilter permissionFilter = Globals.objectMapper.readValue(body, PermissionListFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_PERMISSIONS);
            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, permissionFilter
                    .getIdentityServiceName());
            DBItemIamRole dbItemIamRole = SOSAuthHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), permissionFilter
                    .getRoleName());

            IamPermissionFilter iamPermissionFilter = new IamPermissionFilter();
            iamPermissionFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamPermissionFilter.setControllerId(permissionFilter.getControllerId());
            iamPermissionFilter.setRoleId(dbItemIamRole.getId());

            Permissions permissions = new Permissions();
            permissions.setPermissions(new ArrayList<Permission>());
            permissions.setControllerId(permissionFilter.getControllerId());
            permissions.setIdentityServiceName(permissionFilter.getIdentityServiceName());
            permissions.setRoleName(permissionFilter.getRoleName());

            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);

            List<DBItemIamPermission> listOfPermissions = iamPermissionDBLayer.getIamPermissionList(iamPermissionFilter, 0);
            for (DBItemIamPermission dbItemIamPermission : listOfPermissions) {
                Permission permission = new Permission();
                permission.setExcluded(dbItemIamPermission.getExcluded());
                String p = dbItemIamPermission.getAccountPermission().replaceFirst("sos:products:joc:adminstration:",
                        "sos:products:joc:administration:");
                permission.setPermissionPath(p);
                permissions.getPermissions().add(permission);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(permissions));
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