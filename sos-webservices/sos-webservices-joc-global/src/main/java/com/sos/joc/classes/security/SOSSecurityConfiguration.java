package com.sos.joc.classes.security;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.mgt.SecurityManager;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile;
import org.ini4j.Wini;

import com.sos.auth.interfaces.ISOSSecurityConfiguration;
import com.sos.auth.shiro.classes.SOSShiroIniShare;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
 import com.sos.joc.model.security.configuration.permissions.IniPermissions;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationAccount;
import com.sos.joc.model.security.configuration.SecurityConfigurationMainEntry;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.SecurityConfigurationRoles;
import com.sos.joc.model.security.configuration.permissions.ControllerFolders;
import com.sos.joc.model.security.configuration.permissions.IniControllers;
import com.sos.joc.model.security.configuration.permissions.IniPermission;
import com.sos.joc.model.security.configuration.permissions.SecurityConfigurationFolders;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
 

 
public class SOSSecurityConfiguration implements ISOSSecurityConfiguration {

    private static final String SECTION_USERS = "users";
    private static final String SECTION_ROLES = "roles";
    private static final String SECTION_FOLDERS = "folders";
    private static final String SECTION_MAIN = "main";

    private Ini ini;
    private Wini writeIni;

    public SOSSecurityConfiguration() {
        ini = Ini.fromResourcePath(Globals.getIniFileForShiro(Globals.getShiroIniInClassPath()));
    }

    public SOSSecurityConfiguration(String iniFileName) {
        ini = Ini.fromResourcePath(iniFileName);
    }

    private Long getIdentiyServiceId() {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SHIRO");

            Long idendityService = 0L;
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
            iamIdentityServiceFilter.setIamIdentityServiceType(IdentityServiceTypes.SHIRO);
            try {
                List<DBItemIamIdentityService> listOfIamIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(
                        iamIdentityServiceFilter, 0);
                if (listOfIamIdentityServices.size() == 1) {
                    idendityService = listOfIamIdentityServices.get(0).getId();
                }
            } catch (SOSHibernateException | JocConfigurationException e) {
                idendityService = 0L;
            }
            return idendityService;
        }catch(Exception e) {
            return 0L;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private List<SecurityConfigurationAccount> getAccounts() {

        final Section s = getSection(SECTION_USERS);

        if (s != null) {
            Long identityServiceId = getIdentiyServiceId();
            return s.entrySet().stream().map(entry -> {
                SecurityConfigurationAccount securityConfigurationAccount = new SecurityConfigurationAccount();
                SOSSecurityConfigurationAccountEntry sosSecurityConfigurationAccountEntry = new SOSSecurityConfigurationAccountEntry(entry.getValue(), null,
                        null);

                securityConfigurationAccount.setAccountName(entry.getKey());
                securityConfigurationAccount.setPassword(sosSecurityConfigurationAccountEntry.getPassword());
                securityConfigurationAccount.setIdentityServiceId(identityServiceId);
                securityConfigurationAccount.setRoles(sosSecurityConfigurationAccountEntry.getRoles());
                return securityConfigurationAccount;
            }).collect(Collectors.toList());

        }
        return Collections.emptyList();

    }

    private List<SecurityConfigurationMainEntry> getMain() throws JocException {

        final Section mainSection = ini.getSection(SECTION_MAIN);
        if (mainSection == null) {
            throw new JocConfigurationException("Missing [main] section in shiro.ini");
        }

        final Profile.Section mainProfileSection = writeIni.get(SECTION_MAIN);
        Map<String, String> comments = mainSection.entrySet().stream().filter(entry -> mainProfileSection.getComment(entry.getKey()) != null
                && !mainProfileSection.getComment(entry.getKey()).trim().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> mainProfileSection.getComment(entry.getKey()).trim()));

        return mainSection.entrySet().stream().map(entry -> {
            SecurityConfigurationMainEntry securityConfigurationMainEntry = new SecurityConfigurationMainEntry();
            securityConfigurationMainEntry.setEntryName(entry.getKey());
            securityConfigurationMainEntry.setEntryValue(SOSSecurityConfigurationMainEntry.getMultiLineValue(entry.getKey(), entry.getValue()));
            securityConfigurationMainEntry.setEntryComment(SOSSecurityConfigurationMainEntry.getMultiLineComment(entry.getKey(), comments));
            return securityConfigurationMainEntry;
        }).collect(Collectors.toList());
    }

    private SecurityConfigurationRoles getRoles() {

        final Section s = getSection(SECTION_ROLES);
        SecurityConfigurationRoles roles = new SecurityConfigurationRoles();
        if (s != null) {
            Map<String, SecurityConfigurationFolders> folders = getFolders();
            s.forEach((role, perms) -> {
                SecurityConfigurationRole r = new SecurityConfigurationRole();
                r.setPermissions(mapIniPermissionsToPermissionsObject(perms));
                r.setFolders(folders.get(role));
                roles.setAdditionalProperty(role, r);
            });
        }
        return roles;
    }

    private static List<String> mapFromJS1(List<String> perms){
        SOSPermissionMapTable sosPermissionMapTable = new SOSPermissionMapTable();       
        return sosPermissionMapTable.map(perms);
    }
    
    private static IniPermissions mapIniPermissionsToPermissionsObject(String iniPermissions) {
        List<String> perms = mapFromJS1(Arrays.asList(iniPermissions.trim().split("\\s*,\\s*")));
               
        IniPermissions iniPerms = new IniPermissions();
        List<IniPermission> jocPerms = new ArrayList<>();
        List<IniPermission> defaultPerms = new ArrayList<>();
        List<SOSSecurityPermissionItem> controllerPerms = new ArrayList<>();
        IniControllers controllers = new IniControllers();
        for (String p : perms) {
            if (p.isEmpty()) {
                continue;
            }
            SOSSecurityPermissionItem i = new SOSSecurityPermissionItem(p);
            if (i.isJocPermission()) {
                jocPerms.add(new IniPermission(i.getNormalizedPermission(), i.isExcluded()));
            } else if (i.getController().isEmpty()) {
                defaultPerms.add(new IniPermission(i.getNormalizedPermission(), i.isExcluded()));
            } else {
                controllerPerms.add(i);
            }
        }
        iniPerms.setJoc(jocPerms);
        iniPerms.setControllerDefaults(defaultPerms);
        controllerPerms.stream().collect(Collectors.groupingBy(SOSSecurityPermissionItem::getController, Collectors.mapping(i -> new IniPermission(i
                .getNormalizedPermission(), i.isExcluded()), Collectors.toList()))).forEach((k, v) -> controllers.setAdditionalProperty(k, v));
        iniPerms.setControllers(controllers);
        return iniPerms;
    }

    private Map<String, SecurityConfigurationFolders> getFolders() {

        final Section s = getSection(SECTION_FOLDERS);
        if (s != null) {
            Map<String, ControllerFolders> controllerFolders = s.entrySet().stream().filter(e -> e.getKey().matches("[^|]+\\|[^|]+")).collect(
                    Collectors.toMap(e -> e.getKey().split("\\|", 2)[1], e -> {
                        ControllerFolders cf = new ControllerFolders();
                        cf.setAdditionalProperty(e.getKey().split("\\|", 2)[0], Arrays.asList(e.getValue().trim().split("\\s*,\\s*")).stream().map(
                                f -> {
                                    SOSSecurityFolderItem i = new SOSSecurityFolderItem(f);
                                    Folder folder = new Folder();
                                    folder.setFolder(i.getNormalizedFolder());
                                    folder.setRecursive(i.isRecursive());
                                    return folder;
                                }).collect(Collectors.toList()));
                        return cf;
                    }));

            Map<String, List<Folder>> jocFolders = s.entrySet().stream().filter(e -> !e.getKey().contains("|") && !e.getKey().isEmpty()).collect(
                    Collectors.toMap(Map.Entry::getKey, e -> {
                        return Arrays.asList(e.getValue().trim().split("\\s*,\\s*")).stream().map(f -> {
                            SOSSecurityFolderItem i = new SOSSecurityFolderItem(f);
                            Folder folder = new Folder();
                            folder.setFolder(i.getNormalizedFolder());
                            folder.setRecursive(i.isRecursive());
                            return folder;
                        }).collect(Collectors.toList());
                    }));

            Stream<String> roles = controllerFolders.keySet().stream();
            roles = Stream.concat(roles, jocFolders.keySet().stream()).distinct();
            return roles.collect(Collectors.toMap(role -> role, role -> {
                SecurityConfigurationFolders folders = new SecurityConfigurationFolders();
                if (jocFolders.get(role) != null) {
                    folders.setJoc(jocFolders.get(role));
                }
                if (controllerFolders.get(role) != null) {
                    folders.setControllers(controllerFolders.get(role));
                }
                return folders;
            }));
        }
        return Collections.emptyMap();
    }

    private void writeUsers(List<SecurityConfigurationAccount> accounts) throws InvalidFileFormatException, IOException {
        Wini oldWriteIni = new Wini(Globals.getShiroIniFile().toFile());
        Profile.Section oldSection = oldWriteIni.get(SECTION_USERS);
        clearSection(SECTION_USERS);
        if (accounts != null) {
            Profile.Section s = writeIni.get(SECTION_USERS);
            SOSSecurityHashSettings sosSecurityHashSettings = new SOSSecurityHashSettings();
            sosSecurityHashSettings.setMain(getSection(SECTION_MAIN));

            accounts.stream().distinct().forEachOrdered(securityConfigurationAccount -> {
                SOSSecurityConfigurationAccountEntry sosSecurityConfigurationAccountEntry = new SOSSecurityConfigurationAccountEntry(securityConfigurationAccount,
                        oldSection, sosSecurityHashSettings);
                if ((securityConfigurationAccount.getPassword() != null && !securityConfigurationAccount.getPassword().isEmpty())
                        || !securityConfigurationAccount.getRoles().isEmpty()) {
                    s.put(securityConfigurationAccount.getAccountName(), sosSecurityConfigurationAccountEntry.getIniWriteString());
                }
            });
        }
    }

    private void clearSection(String section) {
        if (writeIni.get(section) != null) {
            writeIni.get(section).clear();
        } else {
            writeIni.add(section);
        }
    }

    private Section getSection(String section) {
        Section s = ini.addSection(section);
        return s;
    }

    private void writeRolesAndFolders(SecurityConfigurationRoles confRoles) {
        clearSection(SECTION_ROLES);
        clearSection(SECTION_FOLDERS);
        List<SOSSecurityConfigurationFolderEntry> folders = new ArrayList<>();

        for (Map.Entry<String, SecurityConfigurationRole> roleEntry : confRoles.getAdditionalProperties().entrySet()) {
            String role = roleEntry.getKey();

            if (roleEntry.getValue() == null) {
                // TODO role without perms?
            } else {
                if (roleEntry.getValue().getPermissions() == null) {
                    // TODO role without perms?
                } else {
                    String iniEntry = writeRole(role, roleEntry.getValue().getPermissions());
                    if (!iniEntry.trim().isEmpty()) {
                        writeIni.get(SECTION_ROLES).put(role, iniEntry);
                    }
                }
                if (roleEntry.getValue().getFolders() != null) {
                    folders.addAll(writeFolders(role, roleEntry.getValue().getFolders()));
                }
            }
        }

        folders.stream().distinct().forEachOrdered(folder -> {
            String iniEntry = folder.getIniWriteString();
            if (!iniEntry.trim().isEmpty()) {
                writeIni.get(SECTION_FOLDERS).put(folder.getFolderKey(), iniEntry);
            }
        });
    }

    private String writeRole(String role, IniPermissions permissions) {
        SOSSecurityConfigurationRoleEntry sosSecurityConfigurationRoleEntry = new SOSSecurityConfigurationRoleEntry(role);
        if (permissions.getJoc() != null && !permissions.getJoc().isEmpty()) {
            for (IniPermission permission : permissions.getJoc()) {
                if (permission.getPermissionPath() != null) {
                    SOSSecurityPermissionItem sosSecurityPermissionItem = new SOSSecurityPermissionItem("", permission);
                    sosSecurityConfigurationRoleEntry.addPermission(sosSecurityPermissionItem.getIniValue());
                }
            }
        }
        if (permissions.getControllerDefaults() != null && !permissions.getControllerDefaults().isEmpty()) {
            for (IniPermission permission : permissions.getControllerDefaults()) {
                if (permission.getPermissionPath() != null) {
                    SOSSecurityPermissionItem sosSecurityPermissionItem = new SOSSecurityPermissionItem("", permission);
                    sosSecurityConfigurationRoleEntry.addPermission(sosSecurityPermissionItem.getIniValue());
                }
            }
        }
        if (permissions.getControllers() != null && permissions.getControllers().getAdditionalProperties() != null && !permissions.getControllers()
                .getAdditionalProperties().isEmpty()) {
            for (Map.Entry<String, List<IniPermission>> controllerPermissions : permissions.getControllers().getAdditionalProperties().entrySet()) {
                if (controllerPermissions.getValue() != null) {
                    for (IniPermission permission : controllerPermissions.getValue()) {
                        if (permission.getPermissionPath() != null) {
                            SOSSecurityPermissionItem sosSecurityPermissionItem = new SOSSecurityPermissionItem(controllerPermissions.getKey(),
                                    permission);
                            sosSecurityConfigurationRoleEntry.addPermission(sosSecurityPermissionItem.getIniValue());
                        }
                    }
                }
            }
        }
        return sosSecurityConfigurationRoleEntry.getIniWriteString();
    }

    private List<SOSSecurityConfigurationFolderEntry> writeFolders(String role, SecurityConfigurationFolders folders) {
        List<SOSSecurityConfigurationFolderEntry> folderList = new ArrayList<>();
        if (folders.getJoc() != null && !folders.getJoc().isEmpty()) {
            SOSSecurityConfigurationFolderEntry sosSecurityConfigurationFolderEntry = new SOSSecurityConfigurationFolderEntry("", role);
            for (Folder folder : folders.getJoc()) {
                sosSecurityConfigurationFolderEntry.addFolder(folder);
            }
            folderList.add(sosSecurityConfigurationFolderEntry);
        }
        if (folders.getControllers() != null && folders.getControllers().getAdditionalProperties() != null && !folders.getControllers()
                .getAdditionalProperties().isEmpty()) {
            for (Map.Entry<String, List<Folder>> folderPermissions : folders.getControllers().getAdditionalProperties().entrySet()) {
                if (folderPermissions.getValue() != null) {
                    SOSSecurityConfigurationFolderEntry sosSecurityConfigurationFolderEntry = new SOSSecurityConfigurationFolderEntry(
                            folderPermissions.getKey(), role);
                    for (Folder folder : folderPermissions.getValue()) {
                        sosSecurityConfigurationFolderEntry.addFolder(folder);
                    }
                    folderList.add(sosSecurityConfigurationFolderEntry);
                }
            }
        }
        return folderList;
    }

    private void writeMain(List<SecurityConfigurationMainEntry> main) throws InvalidFileFormatException, IOException {
        clearSection(SECTION_MAIN);
        if (main != null) {
            Profile.Section s = writeIni.get(SECTION_MAIN);
            main.stream().distinct().forEachOrdered(securityConfigurationMainEntry -> {
                String comment = String.join(System.lineSeparator(), securityConfigurationMainEntry.getEntryComment());
                s.putComment(securityConfigurationMainEntry.getEntryName(), comment);
                s.put(securityConfigurationMainEntry.getEntryName(), SOSSecurityConfigurationMainEntry.getIniWriteString(
                        securityConfigurationMainEntry));
            });
        }
    }

    @Override
    public SecurityConfiguration readConfiguration(Long identityServiceId,String identityServiceName) throws InvalidFileFormatException, IOException, JocException, SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Export shiro.ini");
            SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
            sosShiroIniShare.provideIniFile();

            return readConfigurationFromFilesystem();
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public SecurityConfiguration readConfigurationFromFilesystem() throws InvalidFileFormatException, IOException, JocException {
        writeIni = new Wini(Globals.getShiroIniFile().toFile());

        SecurityConfiguration secConfig = new SecurityConfiguration();

        secConfig.setMain(getMain());
        secConfig.setAccounts(getAccounts());
        secConfig.setRoles(getRoles());

        return secConfig;
    }
    

    public SecurityConfiguration readConfigurationFromFilesystem(Path iniFilename) throws InvalidFileFormatException, IOException, JocException {
        writeIni = new Wini(iniFilename.toFile());

        SecurityConfiguration secConfig = new SecurityConfiguration();

        secConfig.setMain(getMain());
        secConfig.setAccounts(getAccounts());
        secConfig.setRoles(getRoles());

        return secConfig;
    }

    @Override
    public SecurityConfiguration writeConfiguration(SecurityConfiguration securityConfiguration,DBItemIamIdentityService dbItemIamIdentityService) throws IOException, SOSHibernateException,
            JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            writeIni = new Wini(Globals.getShiroIniFile().toFile());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Import shiro.ini");
            writeUsers(securityConfiguration.getAccounts());
            writeRolesAndFolders(securityConfiguration.getRoles());
            writeMain(securityConfiguration.getMain());
            writeIni.store();

            @SuppressWarnings("deprecation")
            org.apache.shiro.config.IniSecurityManagerFactory factory = Globals.getShiroIniSecurityManagerFactory();
            @SuppressWarnings("unused")
            SecurityManager securityManager = factory.getInstance();

            SOSShiroIniShare sosShiroIniShare = new SOSShiroIniShare(sosHibernateSession);
            sosShiroIniShare.copyFileToDb(Globals.getShiroIniFile().toFile());

            return securityConfiguration;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public SecurityConfiguration readConfiguration() throws InvalidFileFormatException, JocException, SOSHibernateException, IOException {
        return readConfiguration(null,"");
    }
}
