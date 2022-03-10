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
import com.sos.joc.db.authentication.DBItemIamPermission;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.security.IamPermissionDBLayer;
import com.sos.joc.db.security.IamPermissionFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.folders.FolderFilter;
import com.sos.joc.model.security.folders.FolderItem;
import com.sos.joc.model.security.folders.FolderListFilter;
import com.sos.joc.model.security.folders.FolderRename;
import com.sos.joc.model.security.folders.Folders;
import com.sos.joc.model.security.permissions.FoldersFilter;
import com.sos.joc.security.classes.SecurityHelper;
import com.sos.joc.security.resource.IFolderResource;
import com.sos.schema.JsonValidator;

@Path("iam")
public class FolderResourceImpl extends JOCResourceImpl implements IFolderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderResourceImpl.class);

    private static final String API_CALL_FOLDERS = "./iam/folders";
    private static final String API_CALL_FOLDER_READ = "./iam/folder";
    private static final String API_CALL_FOLDERS_STORE = "./iam/folders/store";
    private static final String API_CALL_FOLDER_RENAME = "./iam/folder/rename";
    private static final String API_CALL_FOLDERS_DELETE = "./iam/folders/delete";

    @Override
    public JOCDefaultResponse postFolderRead(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            FolderFilter folderFilter = Globals.objectMapper.readValue(body, FolderFilter.class);
            JsonValidator.validateFailFast(body, FolderFilter.class);

            initLogging(API_CALL_FOLDER_READ, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            FolderItem folderItem = new FolderItem();
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FOLDER_READ);

            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, folderFilter
                    .getIdentityServiceName());
            DBItemIamRole dbItemIamRole = SecurityHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), folderFilter.getRoleName());

            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            IamPermissionFilter filter = new IamPermissionFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setControllerId(folderFilter.getControllerId());
            filter.setRoleId(dbItemIamRole.getId());
            filter.setFolder(folderFilter.getFolderName());

            DBItemIamPermission dbItemIamPermission = iamPermissionDBLayer.getUniquePermission(filter);
            if (dbItemIamPermission != null) {
                Folder folder = new Folder();
                folder.setFolder(dbItemIamPermission.getFolderPermission());
                folder.setRecursive(dbItemIamPermission.getRecursive());
                folderItem.setControllerId(dbItemIamPermission.getControllerId());
                folderItem.setFolder(folder);
                folderItem.setIdentityServiceName(folderFilter.getIdentityServiceName());
                folderItem.setRoleName(folderFilter.getRoleName());
            } else {
                throw new JocObjectNotExistException("Object permission <" + folderFilter.getFolderName() + "> not found");
            }
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(folderItem));

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
    public JOCDefaultResponse postFoldersStore(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            Folders folders = Globals.objectMapper.readValue(body, Folders.class);
            JsonValidator.validateFailFast(body, Folders.class);

            initLogging(API_CALL_FOLDERS_STORE, Globals.objectMapper.writeValueAsBytes(folders), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FOLDERS_STORE);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, folders
                    .getIdentityServiceName());
            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            DBItemIamRole dbItemIamRole = SecurityHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), folders.getRoleName());

            IamPermissionFilter iamPermissionFilter = new IamPermissionFilter();
            iamPermissionFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamPermissionFilter.setControllerId(folders.getControllerId());
            iamPermissionFilter.setRoleId(dbItemIamRole.getId());

            for (Folder folder : folders.getFolders()) {
                iamPermissionFilter.setFolder(folder.getFolder());
                DBItemIamPermission dbItemIamPermission = iamPermissionDBLayer.getUniquePermission(iamPermissionFilter);
                boolean newFolder = false;
                if (dbItemIamPermission == null) {
                    dbItemIamPermission = new DBItemIamPermission();
                    dbItemIamPermission.setControllerId(folders.getControllerId());
                    dbItemIamPermission.setIdentityServiceId(dbItemIamIdentityService.getId());
                    dbItemIamPermission.setRoleId(dbItemIamRole.getId());
                    newFolder = true;
                }
                dbItemIamPermission.setExcluded(false);

                dbItemIamPermission.setFolderPermission(folder.getFolder());
                dbItemIamPermission.setRecursive(folder.getRecursive());

                if (newFolder) {
                    sosHibernateSession.save(dbItemIamPermission);
                } else {
                    sosHibernateSession.update(dbItemIamPermission);
                }
            }

            storeAuditLog(folders.getAuditLog(), CategoryType.IDENTITY);
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
    public JOCDefaultResponse postFolderRename(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            initLogging(API_CALL_FOLDER_RENAME, body, accessToken);
            JsonValidator.validate(body, FolderRename.class);
            FolderRename folderRename = Globals.objectMapper.readValue(body, FolderRename.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FOLDER_RENAME);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, folderRename
                    .getIdentityServiceName());
            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            DBItemIamRole dbItemIamRole = SecurityHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), folderRename.getRoleName());

            int count = iamPermissionDBLayer.renameFolder(dbItemIamIdentityService.getId(), dbItemIamRole.getId(), folderRename.getControllerId(),
                    folderRename.getOldFolderName(), folderRename.getNewFolder().getFolder(), folderRename.getNewFolder().getRecursive());

            if (count == 0) {
                throw new JocObjectNotExistException("Object folder <" + folderRename.getOldFolderName() + "> not found");
            }

            storeAuditLog(folderRename.getAuditLog(), CategoryType.IDENTITY);

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
    public JOCDefaultResponse postFoldersDelete(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;

        try {
            JsonValidator.validate(body, FoldersFilter.class);
            FoldersFilter foldersFilter = Globals.objectMapper.readValue(body, FoldersFilter.class);
            initLogging(API_CALL_FOLDERS_DELETE, Globals.objectMapper.writeValueAsBytes(foldersFilter), accessToken);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSSecurityDBConfiguration");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);
            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, foldersFilter
                    .getIdentityServiceName());
            DBItemIamRole dbItemIamRole = SecurityHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), foldersFilter.getRoleName());

            IamPermissionFilter iamPermissionFilter = new IamPermissionFilter();
            iamPermissionFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamPermissionFilter.setControllerId(foldersFilter.getControllerId());
            iamPermissionFilter.setRoleId(dbItemIamRole.getId());

            for (String folder : foldersFilter.getFolderNames()) {
                iamPermissionFilter.setFolder(folder);
                int count = iamPermissionDBLayer.delete(iamPermissionFilter);
                if (count == 0) {
                    throw new JocObjectNotExistException("Object <" + folder + "> not found");
                }
            }
            Globals.commit(sosHibernateSession);

            storeAuditLog(foldersFilter.getAuditLog(), CategoryType.IDENTITY);

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
    public JOCDefaultResponse postFolders(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            FolderListFilter permissionFilter = Globals.objectMapper.readValue(body, FolderListFilter.class);
            JsonValidator.validateFailFast(body, FolderListFilter.class);

            initLogging(API_CALL_FOLDERS, body, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getAccounts().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_FOLDERS);
            DBItemIamIdentityService dbItemIamIdentityService = SecurityHelper.getIdentityService(sosHibernateSession, permissionFilter
                    .getIdentityServiceName());
            DBItemIamRole dbItemIamRole = SecurityHelper.getRole(sosHibernateSession, dbItemIamIdentityService.getId(), permissionFilter
                    .getRoleName());

            IamPermissionFilter iamPermissionFilter = new IamPermissionFilter();
            iamPermissionFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamPermissionFilter.setControllerId(permissionFilter.getControllerId());
            iamPermissionFilter.setRoleId(dbItemIamRole.getId());

            Folders folders = new Folders();
            folders.setFolders(new ArrayList<Folder>());
            folders.setControllerId(permissionFilter.getControllerId());
            folders.setRoleName(permissionFilter.getRoleName());
            folders.setIdentityServiceName(permissionFilter.getIdentityServiceName());

            IamPermissionDBLayer iamPermissionDBLayer = new IamPermissionDBLayer(sosHibernateSession);

            List<DBItemIamPermission> listOfPermissions = iamPermissionDBLayer.getIamFolderList(iamPermissionFilter, 0);
            for (DBItemIamPermission dbItemIamPermission : listOfPermissions) {
                Folder folder = new Folder();
                folder.setRecursive(dbItemIamPermission.getRecursive());
                folder.setFolder(dbItemIamPermission.getFolderPermission());
                folders.getFolders().add(folder);
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(folders));
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