package com.sos.auth.classes;

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
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSInitialPasswordSetting;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.security.SOSPermissionMapTable;
import com.sos.joc.classes.security.SOSSecurityConfigurationAccountEntry;
import com.sos.joc.classes.security.SOSSecurityConfigurationMainEntry;
import com.sos.joc.classes.security.SOSSecurityFolderItem;
import com.sos.joc.classes.security.SOSSecurityPermissionItem;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationAccount;
import com.sos.joc.model.security.configuration.SecurityConfigurationMainEntry;
import com.sos.joc.model.security.configuration.SecurityConfigurationRole;
import com.sos.joc.model.security.configuration.SecurityConfigurationRoles;
import com.sos.joc.model.security.configuration.permissions.ControllerFolders;
import com.sos.joc.model.security.configuration.permissions.IniControllers;
import com.sos.joc.model.security.configuration.permissions.IniPermission;
import com.sos.joc.model.security.configuration.permissions.IniPermissions;
import com.sos.joc.model.security.configuration.permissions.SecurityConfigurationFolders;

public class SOSSecurityConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSecurityConfiguration.class);

    private static final String SECTION_USERS = "users";
    private static final String SECTION_ROLES = "roles";
    private static final String SECTION_FOLDERS = "folders";
    private static final String SECTION_MAIN = "main";

    private Ini ini;
    private Wini writeIni;
    private String initialPassword;

    public SOSSecurityConfiguration(String iniFileName) {
        ini = Ini.fromResourcePath(iniFileName);
    }

    private void getInitialPassword(SOSHibernateSession sosHibernateSession) {
        if (initialPassword == null) {

            try {
                SOSInitialPasswordSetting sosInitialPasswordSetting = SOSAuthHelper.getInitialPasswordSettings(sosHibernateSession);
                initialPassword = sosInitialPasswordSetting.getInitialPassword();
            } catch (SOSHibernateException | IOException e) {
                initialPassword = SOSAuthHelper.INITIAL;
            }
        }
    }

    private List<SecurityConfigurationAccount> getAccounts(SOSHibernateSession sosHibernateSession) {

        final Section s = getSection(SECTION_USERS);

        if (s != null) {
            Long identityServiceId = 0L;
            getInitialPassword(sosHibernateSession);

            return s.entrySet().stream().map(entry -> {
                SecurityConfigurationAccount securityConfigurationAccount = new SecurityConfigurationAccount();
                SOSSecurityConfigurationAccountEntry sosSecurityConfigurationAccountEntry = new SOSSecurityConfigurationAccountEntry(entry
                        .getValue());

                securityConfigurationAccount.setAccountName(entry.getKey());

                securityConfigurationAccount.setPassword(initialPassword);
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

    private static List<String> mapFromJS1(List<String> perms) {
        SOSPermissionMapTable sosPermissionMapTable = new SOSPermissionMapTable();
        return sosPermissionMapTable.map(perms);
    }

    private static IniPermissions mapIniPermissionsToPermissionsObject(String iniPermissions) {

        List<String> perms;
        perms = mapFromJS1(Arrays.asList(iniPermissions.trim().split("\\s*,\\s*")));

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

    private Section getSection(String section) {
        Section s = ini.addSection(section);
        return s;
    }

    public SecurityConfiguration readConfigurationFromFilesystem(SOSHibernateSession sosHibernateSession, Path iniFilename)
            throws InvalidFileFormatException, IOException, JocException {
        writeIni = new Wini(iniFilename.toFile());

        SecurityConfiguration secConfig = new SecurityConfiguration();

        secConfig.setMain(getMain());
        secConfig.setAccounts(getAccounts(sosHibernateSession));
        secConfig.setRoles(getRoles());

        return secConfig;
    }

    public static boolean haveShiro() {
        return true;
    }
}
