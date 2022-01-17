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

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapLogin.class);
    SOSLdapWebserviceCredentials sosLdapWebserviceCredentials;
    InitialDirContext ldapContext;

    public SOSLdapGroupRolesMapping(InitialDirContext ldapContext, SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) {
        super();
        this.sosLdapWebserviceCredentials = sosLdapWebserviceCredentials;
        this.ldapContext = ldapContext;
    }

    private Collection<String> getGroupNamesByGroup(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) throws NamingException {
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
            Attribute g = result.getAttributes().get(groupNameAttribute);
            String group = g.get().toString();
            rolesForGroups.add(group);
            LOGGER.debug(String.format("Groupname %s found in attribute", group, groupNameAttribute));
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

    private Collection<String> getGroupNamesByUser(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) throws NamingException {

        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        Collection<String> groupNames = null;

        String userSearchFilter = "";
        if (sosLdapWebserviceCredentials.getUserSearchFilter().isEmpty() && "memberOf".equals(sosLdapWebserviceCredentials.getGroupNameAttribute())) {
            userSearchFilter = "(sAMAccountName=%s)";
        } else {
            userSearchFilter = sosLdapWebserviceCredentials.getUserSearchFilter();
        }

        String searchFilter = substituteUserName(userSearchFilter);

        LOGGER.debug(String.format("getting groups from ldap using user search filter %s with search base %s", searchFilter,
                sosLdapWebserviceCredentials.getSearchBase()));

        NamingEnumeration<SearchResult> answer = ldapContext.search(sosLdapWebserviceCredentials.getSearchBase(), searchFilter, searchCtls);

        if (!answer.hasMore()) {
            LOGGER.warn(String.format("Cannot find roles for user: %s with search filter %s and search base: %s ", sosLdapWebserviceCredentials
                    .getSosLdapLoginUserName().getUserName(), searchFilter, sosLdapWebserviceCredentials.getSearchBase()));
        } else {
            SearchResult result = answer.nextElement();
            String groupNameAttribute = sosLdapWebserviceCredentials.getGroupNameAttribute();
            Attribute memberOf = result.getAttributes().get(groupNameAttribute);

            if (memberOf != null) {
                LOGGER.debug("getting all attribute values using attribute " + memberOf);
                groupNames = getAllAttributeValues(memberOf);
            } else {
                LOGGER.info(String.format("User: %s is not member of any group", sosLdapWebserviceCredentials.getSosLdapLoginUserName()
                        .getUserName()));
            }
        }
        return groupNames;
    }

    public List<String> getGroupRolesMapping(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials) throws NamingException {
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
                LOGGER.info("using the username from login: " + sosLdapWebserviceCredentials.getSosLdapLoginUserName().getLoginAccount());
            }
        }
        LOGGER.debug("userPrincipalName: " + sosLdapWebserviceCredentials.getSosLdapLoginUserName().getUserName());

        if ((sosLdapWebserviceCredentials.getSearchBase() != null || sosLdapWebserviceCredentials.getGroupSearchBase() != null)
                && (sosLdapWebserviceCredentials.getGroupSearchFilter() != null || sosLdapWebserviceCredentials.getUserSearchFilter() != null)) {

            Collection<String> groupNames;

            if (sosLdapWebserviceCredentials.getGroupSearchFilter() != null && !sosLdapWebserviceCredentials.getGroupSearchFilter().isEmpty()) {
                groupNames = getGroupNamesByGroup(sosLdapWebserviceCredentials);
            } else {
                groupNames = getGroupNamesByUser(sosLdapWebserviceCredentials);
            }

            if (groupNames != null) {
                listOfRoles = getRoleNamesForGroups(sosLdapWebserviceCredentials, groupNames);
            }
        }

        return listOfRoles;
    }

   
    private List<String> getRoleNamesForGroups(SOSLdapWebserviceCredentials sosLdapWebserviceCredentials, Collection<String> groupNames) {
        List<String> listOfRoles = new ArrayList<String>();
        if (sosLdapWebserviceCredentials.getGroupRolesMap() != null) {
            LOGGER.debug("Analysing groupRolesMapping");
            for (String groupName : groupNames) {
                LOGGER.debug(String.format("Looking for group: %s", groupName));
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
