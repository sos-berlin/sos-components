package com.sos.auth.ldap.classes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSLdapGroupRolesMapping {

    private static final String DISTINGUISHED_NAME = "distinguishedName";
    private static final String MEMBER_OF = "memberOf";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapLogin.class);
    private SOSLdapWebserviceCredentials sosLdapWebserviceCredentials;
    private InitialDirContext ldapContext;

    public SOSLdapGroupRolesMapping(InitialDirContext ldapContext, SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) {
        super();
        this.sosLdapWebserviceCredentials = sosLdapWebserviceCredentials;
        this.ldapContext = ldapContext;
    }

    private Collection<String> getGroupNamesByGroup() throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String searchFilter = substituteUserName(sosLdapWebserviceCredentials.getGroupSearchFilter());
        LOGGER.debug(String.format("getting groups from ldap using group search filter %s with group search base %s", searchFilter,
                sosLdapWebserviceCredentials.getGroupSearchBase()));

        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapWebserviceCredentials.getGroupSearchBase(), searchFilter, searchCtls);
        ArrayList<String> rolesForGroups = new ArrayList<String>();

        while (answer.hasMoreElements()) {
            SearchResult result = answer.next();
            String groupNameAttribute = sosLdapWebserviceCredentials.getGroupNameAttribute();
            LOGGER.debug(String.format("Groupname in attribute %s", groupNameAttribute));

            Attribute g = result.getAttributes().get(groupNameAttribute);
            if (g != null) {
                String group = g.get().toString();
                rolesForGroups.add(group);
                LOGGER.debug(String.format("Account is member of group: %s", group));
            } else {
                LOGGER.warn("Could not find attribute " + groupNameAttribute);
            }

        }
        return rolesForGroups;
    }

    private String substituteUserName(String source) {
        String s = String.format(source, this.sosLdapWebserviceCredentials.getSosLdapLoginUserName().getUserName());
        s = s.replaceAll("\\^s", "%s");
        s = String.format(s, this.sosLdapWebserviceCredentials.getSosLdapLoginUserName().getLoginAccount());
        return s;
    }

    private Attributes getUserAttributes() throws NamingException {
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Attributes user = null;

        String searchFilter = "(" + sosLdapWebserviceCredentials.getUserNameAttribute() + "=%s)";
        if (sosLdapWebserviceCredentials.getUserSearchFilter() == null) {
            LOGGER.warn(String.format(
                    "You have specified a value for userNameAttribute but you have not defined the userSearchFilter. The default filter %s will be used",
                    searchFilter));
        } else {
            searchFilter = sosLdapWebserviceCredentials.getUserSearchFilter();
        }

        searchFilter = substituteUserName(searchFilter);

        LOGGER.debug(String.format("getting user from ldap using search filter %s with search base %s", sosLdapWebserviceCredentials.getSearchBase(),
                searchFilter));

        String searchBase = "";
        if (sosLdapWebserviceCredentials.getSearchBase() == null) {
            LOGGER.warn(String.format(
                    "You have specified a value for userNameAttribute but you have not defined the searchBase. The default empty search base will be used"));
        } else {
            searchBase = sosLdapWebserviceCredentials.getSearchBase();
        }

        NamingEnumeration<SearchResult> answer = ldapContext.search(searchBase, searchFilter, searchCtls);

        if (!answer.hasMore()) {
            LOGGER.warn(String.format("Cannot find user: %s with search filter %s and search base: %s ", sosLdapWebserviceCredentials
                    .getSosLdapLoginUserName().getLoginAccount(), searchFilter, searchBase));
        } else {
            SearchResult result = answer.nextElement();
            user = result.getAttributes();
            LOGGER.debug("user found: " + user.toString());
        }
        return user;

    }

    private void closeEnumeration(NamingEnumeration<?> namingEnumeration) {
        try {
            if (namingEnumeration != null) {
                namingEnumeration.close();
            }
        } catch (NamingException e) {
            LOGGER.error("Exception while closing NamingEnumeration: ", e);
        }
    }

    private Collection<String> getAllAttributeValues(Attribute attribute) throws NamingException {
        Set<String> values = new HashSet<String>();
        NamingEnumeration<?> namingEnumeration = null;
        try {
            namingEnumeration = attribute.getAll();
            while (namingEnumeration.hasMore()) {
                String value = (String) namingEnumeration.next();
                values.add(value);
            }
        } finally {
            closeEnumeration(namingEnumeration);
        }
        return values;
    }

    private Collection<String> getGroupkeysFromNestedGroup(String userDistinguishedName) throws NamingException {
        LOGGER.debug("Get mapped groupkeys from nested group for child member " + userDistinguishedName);
        Collection<String> groupNames = new HashSet<String>();

        for (String groupKey : sosLdapWebserviceCredentials.getGroupRolesMap().keySet()) {
            LOGGER.debug("Get nested members for groupKey: " + groupKey);
            Collection<String> members = getNestedMembers(groupKey);
            for (String member : members) {
                if (member.equals(userDistinguishedName)) {
                    LOGGER.debug(userDistinguishedName + " is child of " + groupKey);
                    groupNames.add(groupKey);
                }
            }
        }
        return groupNames;
    }

    private Collection<String> getNestedMembers(String group) throws NamingException {

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Collection<String> members = new HashSet<String>();

        String searchFilter = "memberOf:1.2.840.113556.1.4.1941:=" + group;

        LOGGER.debug(String.format("getting members from ldap using user search filter %s with search base %s", searchFilter,
                sosLdapWebserviceCredentials.getSearchBase()));

        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapWebserviceCredentials.getSearchBase(), searchFilter, searchCtls);

        try {
            while (answer.hasMore()) {
                SearchResult result = answer.nextElement();

                Attribute distinguishName = result.getAttributes().get(DISTINGUISHED_NAME);
                if (distinguishName != null) {
                    members.add(distinguishName.get().toString());
                    LOGGER.debug(String.format("Member %s found in group", distinguishName.get().toString(), DISTINGUISHED_NAME));
                }

            }
        } catch (Exception e) {
        }

        return members;

    }

    private Collection<String> getGroupNamesByUser() throws NamingException {

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Collection<String> groupNames = null;

        String userSearchFilter = "";
        if (sosLdapWebserviceCredentials.getUserSearchFilter().isEmpty() && MEMBER_OF.equals(sosLdapWebserviceCredentials.getGroupNameAttribute())) {
            userSearchFilter = "(sAMAccountName=%s)";
        } else {
            userSearchFilter = sosLdapWebserviceCredentials.getUserSearchFilter();
        }

        String searchFilter = substituteUserName(userSearchFilter);

        LOGGER.debug(String.format("getting groups from ldap using user search filter %s with search base %s", searchFilter,
                sosLdapWebserviceCredentials.getSearchBase()));

        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapWebserviceCredentials.getSearchBase(), searchFilter, searchCtls);

        if (!answer.hasMore()) {
            LOGGER.debug(String.format("Cannot find account with search filter %s and search base: %s ", searchFilter, sosLdapWebserviceCredentials
                    .getSearchBase()));
            LOGGER.warn(String.format("Cannot find account for user: search filter %s and search base: %s ", searchFilter,
                    sosLdapWebserviceCredentials.getSearchBase()));
        } else {
            SearchResult result = answer.nextElement();
            String groupNameAttribute = sosLdapWebserviceCredentials.getGroupNameAttribute();
            Attribute memberOf = result.getAttributes().get(groupNameAttribute);

            if (memberOf != null) {
                LOGGER.debug("getting all attribute values using attribute " + memberOf);
                groupNames = getAllAttributeValues(memberOf);
                for (String group : groupNames) {
                    LOGGER.debug(String.format("Account is member of group: %s", group));
                }
                if (!sosLdapWebserviceCredentials.getDisableNestedGroupSearch()) {
                    Set<String> groupsToAdd = new HashSet<String>();
                    if (MEMBER_OF.equals(sosLdapWebserviceCredentials.getGroupNameAttribute())) {
                        Attribute distinguishName = result.getAttributes().get(DISTINGUISHED_NAME);
                        String userDistinguishedName = "";
                        if (distinguishName != null) {
                            userDistinguishedName = distinguishName.get().toString();
                            groupsToAdd.addAll(getGroupkeysFromNestedGroup(userDistinguishedName));
                        }
                    }
                    groupNames.addAll(groupsToAdd);
                }
            } else {
                LOGGER.info("User is not member of any group");
            }
        }
        return groupNames;
    }

    public List<String> getGroupRolesMapping() throws NamingException {
        LOGGER.debug(String.format("Getting roles for user %s", sosLdapWebserviceCredentials.getSosLdapLoginUserName().getLoginAccount()));
        List<String> listOfRoles = new ArrayList<String>();
        if (sosLdapWebserviceCredentials.getUserNameAttribute() != null && !sosLdapWebserviceCredentials.getUserNameAttribute().isEmpty()) {
            LOGGER.debug("get userPrincipalName for substitution from user record.");
            Attributes user = getUserAttributes();
            if (user != null) {
                if (user.get(sosLdapWebserviceCredentials.getUserNameAttribute()) != null) {
                    sosLdapWebserviceCredentials.getSosLdapLoginUserName().setUserName(user.get(sosLdapWebserviceCredentials.getUserNameAttribute())
                            .get().toString());
                }
            } else {
                LOGGER.debug("using the username from login: " + sosLdapWebserviceCredentials.getSosLdapLoginUserName().getLoginAccount());
            }
        }
        LOGGER.debug("userPrincipalName: " + sosLdapWebserviceCredentials.getSosLdapLoginUserName().getUserName());

        if ((sosLdapWebserviceCredentials.getSearchBase() != null || sosLdapWebserviceCredentials.getGroupSearchBase() != null)
                && (sosLdapWebserviceCredentials.getGroupSearchFilter() != null || sosLdapWebserviceCredentials.getUserSearchFilter() != null)) {

            Collection<String> groupNames;

            if (sosLdapWebserviceCredentials.getGroupSearchFilter() != null && !sosLdapWebserviceCredentials.getGroupSearchFilter().isEmpty()) {
                groupNames = getGroupNamesByGroup();
            } else {
                groupNames = getGroupNamesByUser();
            }

            if (groupNames != null) {
                listOfRoles = getRoleNamesForGroups(groupNames);
            }
        }

        return listOfRoles;
    }

    private List<String> getRoleNamesForGroups(Collection<String> groupNames) {
        List<String> listOfRoles = new ArrayList<String>();
        if (sosLdapWebserviceCredentials.getGroupRolesMap() != null) {
            LOGGER.debug("Analysing groupRolesMapping");
            for (String groupName : groupNames) {
                LOGGER.debug(String.format("Checking group: %s", groupName));
                List<String> listOfGroupRoles = sosLdapWebserviceCredentials.getGroupRolesMap().get(groupName);
                if (listOfRoles != null && listOfGroupRoles != null) {
                    for (String role : listOfGroupRoles) {
                        listOfRoles.add(role);
                    }
                } else {
                    LOGGER.debug(String.format("Group %s not found in groupRolesMapping", groupName));
                }
            }
        }
        return listOfRoles;
    }
}
