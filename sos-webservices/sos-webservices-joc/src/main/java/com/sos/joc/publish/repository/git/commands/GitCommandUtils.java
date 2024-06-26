package com.sos.joc.publish.repository.git.commands;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.git.GitCommand;
import com.sos.commons.git.enums.GitConfigType;
import com.sos.commons.git.results.GitAddCommandResult;
import com.sos.commons.git.results.GitCheckoutCommandResult;
import com.sos.commons.git.results.GitCloneCommandResult;
import com.sos.commons.git.results.GitCommitCommandResult;
import com.sos.commons.git.results.GitConfigCommandResult;
import com.sos.commons.git.results.GitLsRemoteCommandResult;
import com.sos.commons.git.results.GitPullCommandResult;
import com.sos.commons.git.results.GitPushCommandResult;
import com.sos.commons.git.results.GitRemoteCommandResult;
import com.sos.commons.git.util.GitCommandConstants;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocGitException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.publish.git.GitCredentials;
import com.sos.joc.model.publish.git.GitCredentialsList;
import com.sos.joc.model.publish.git.commands.CheckoutFilter;
import com.sos.joc.model.publish.git.commands.CloneFilter;
import com.sos.joc.model.publish.git.commands.CommitFilter;
import com.sos.joc.model.publish.git.commands.CommonFilter;

public class GitCommandUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommandUtils.class);
    private static final String REGEX_GIT_URI_SSH = "^\\S*@(\\S*):(\\S*)$";
    private static final String REGEX_GIT_URI_PROTOCOL = "^([a-z]{2,5})://.*$";
    private static final String REGEX_GIT_URI_HOST = "^[a-z]{2,5}://([^/]*)/.*$";
    private static final String REGEX_HTTP_URI = "^(http.?)://([^/]*)(.*)$";
    private static final String DEFAULT_PROTOCOL = "ssh";
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    public static final String CONCURRENT_ACCESS_MESSAGE = "The Git API is currently used by another user. Please try later.";
    
    public static final GitCloneCommandResult cloneGitRepository (CloneFilter filter, String account, SOSHibernateSession session,
            Charset charset) throws JsonMappingException, JsonProcessingException, SOSException {
        JocConfigurationDbLayer dbLayer = new JocConfigurationDbLayer(session);
        String hostPort = "";
        String path = "";
        String protocol = "";
        if (isSshUri(filter.getRemoteUrl())) {
            Pattern sshGitUriPattern = Pattern.compile(REGEX_GIT_URI_SSH);
            Matcher sshGitUriMatcher = sshGitUriPattern.matcher(filter.getRemoteUrl());
            if(sshGitUriMatcher.matches()) {
                protocol = DEFAULT_PROTOCOL;
                hostPort = sshGitUriMatcher.group(1);
                path = sshGitUriMatcher.group(2);
            }
        } else {
            URI remoteUri = null;
            try {
                remoteUri = new URI(filter.getRemoteUrl());
                hostPort = remoteUri.getHost();
            } catch (URISyntaxException e) {
                throw new JocBadRequestException(String.format("%1$s is not a Git URL.", filter.getRemoteUrl()), e);
            }
            
            Pattern protocolUriPattern = Pattern.compile(REGEX_GIT_URI_PROTOCOL);
            Matcher protocolUriMatcher = protocolUriPattern.matcher(filter.getRemoteUrl());
            if(protocolUriMatcher.matches()) {
                protocol = protocolUriMatcher.group(1);
                if (!HTTP_PROTOCOL.equals(protocol) && !HTTPS_PROTOCOL.equals(protocol)) {
                    throw new JocBadRequestException(String.format("%1$s is not a Git URL.", filter.getRemoteUrl()));
                }
                String hostPortWithCreds = "";
                if(hostPort == null || hostPort.isEmpty()) {
                    Pattern hostUriPattern = Pattern.compile(REGEX_GIT_URI_HOST);
                    Matcher hostUriMatcher = hostUriPattern.matcher(filter.getRemoteUrl());
                    if(hostUriMatcher.matches()) {
                        hostPortWithCreds = hostUriMatcher.group(1);
                        if (hostPortWithCreds.contains("@")) {
                            hostPort = hostPortWithCreds.split("@")[1];
                        } else {
                            hostPort = hostPortWithCreds;
                        }
                    }
                }
                if(!hostPort.contains(":") && remoteUri.getPort() != -1) {
                    hostPort += ":" + remoteUri.getPort();
                }
                if(remoteUri.getPath() != null && !remoteUri.getPath().isEmpty()) {
                    path = remoteUri.getPath();
                } else if (!hostPortWithCreds.isEmpty()) {
                    path = filter.getRemoteUrl().replace(protocol + "://" + hostPortWithCreds, "");
                }
            }
        }
        if (hostPort.isEmpty() || path.isEmpty() || protocol.isEmpty()) {
            throw new JocBadRequestException(String.format("%1$s is not a Git URL.", filter.getRemoteUrl()));
        }

        try {
            GitCredentialsList credList = null;
            DBItemJocConfiguration dbItem = getJocConfigurationDbItem(account, dbLayer);
            if(dbItem != null) {
                credList =  Globals.objectMapper.readValue(dbItem.getConfigurationItem(), GitCredentialsList.class);
            }
            if(credList == null) {
                if(dbItem != null) {
                    throw new JocGitException("No Git Credentials found for Joc account:  " + dbItem.getAccount());
                } else {
                    throw new JocGitException("No Git Credentials found for current account.");
                }
            }
            if(!credList.getRemoteUrls().contains(filter.getRemoteUrl())) {
                credList.getRemoteUrls().add(filter.getRemoteUrl());
                dbItem.setConfigurationItem(Globals.objectMapper.writeValueAsString(credList));
                dbLayer.saveOrUpdateConfiguration(dbItem);
            }
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            if (workingDir != null) {
                LOGGER.info("working dir: " + workingDir.toString());
            }
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            if(repositoryBase != null) {
                LOGGER.debug("repositoryBase: " + repositoryBase.toString());
            }
            if(protocol.equals(DEFAULT_PROTOCOL)) {
                Path gitKeyfilePath = null;
                String username = null;
                String email = null;
                for(GitCredentials credentials : credList.getCredentials()) {
                    if(credentials.getGitServer().equals(hostPort)) {
                        username = credentials.getUsername();
                        email = credentials.getEmail();
                        if (credentials.getKeyfilePath() == null) {
                            throw new JocGitException(String.format(
                                    "remote URL '%1$s' needs ssh authentication. Missing keyfilePath in credentials for server '%2$s' and account '%3$s'.",
                                    filter.getRemoteUrl(), hostPort, credentials.getGitAccount()));
                        } else if(credentials.getKeyfilePath().isEmpty()) {
                            // use ~/.ssh/id_rsa from home directory
                            // use Path homeDir = Paths.get(System.getProperty("user.home")); for windows and linux
                            gitKeyfilePath = Paths.get(System.getProperty("user.home")).resolve(".ssh/id_rsa");
                        } else if(!credentials.getKeyfilePath().contains("/") && !credentials.getKeyfilePath().contains("\\")) {
                            // filename only, use key from JETTY_BASE/resources/joc/repositories/private
                            gitKeyfilePath = Globals.sosCockpitProperties.resolvePath("repositories").resolve("private")
                                    .resolve(credentials.getKeyfilePath());
                        } else  {
                            gitKeyfilePath = Paths.get(credentials.getKeyfilePath());
                        }
                        break;
                    }
                }
                // check remote connectivity
                GitLsRemoteCommandResult lsRemoteResult = (GitLsRemoteCommandResult)GitCommand.executeGitCheckRemoteConnection(
                        filter.getRemoteUrl(), charset);
                // TODO: rest of check when timeout available from SOSShell
                
                // prepare git config environment
                // remember old values
                
                String oldSshCommandValue = getOldSshCommandValue(charset);
                String oldUsernameValue = getOldUsernameValue(charset);
                String oldEmailValue = getOldEmailValue(charset);
                // unset old values
                prepareConfigUnsetSshCommand(charset);
                prepareConfigUnsetUsername(charset);
                prepareConfigUnsetUserEmail(charset);
                // add new values
                
                GitConfigCommandResult configResult = prepareConfigSetSshCommand(charset, gitKeyfilePath);
                configResult = prepareConfigSetUsername(charset, username);
                configResult = prepareConfigSetUserEmail(charset, email);
                // clone
                String folder = filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder();
                GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(
                        filter.getRemoteUrl(), folder, repositoryBase, charset);
                if(result.getExitCode() != 0) {
                    throw new JocGitException(String.format("clone command exit code <%1$d> with message: %2$s", 
                            result.getExitCode(), result.getStdErr()), result.getException());
                }
                // cleanup git config environment
                prepareConfigUnsetSshCommand(charset);
                prepareConfigUnsetUsername(charset);
                prepareConfigUnsetUserEmail(charset);
                if(!oldSshCommandValue.isEmpty()) {
                    prepareConfigRestoreSshCommand(charset, oldSshCommandValue);
                }
                if(!oldUsernameValue.isEmpty()) {
                    prepareConfigRestoreUsername(charset, oldUsernameValue);
                }
                if(!oldEmailValue.isEmpty()) {
                    prepareConfigRestoreUserEmail(charset, oldEmailValue);
                }
                createOrUpdateFolder(filter.getFolder(), session);
                return result;
            } else {
                String username = null;
                String pwopat = null;
                String gitAccount = null;
                for(GitCredentials credentials : credList.getCredentials()) {
                    if(credentials.getGitServer().equals(hostPort)) {
                        gitAccount = credentials.getGitAccount();
                        username = credentials.getUsername();
                        if(credentials.getPersonalAccessToken() != null && !credentials.getPersonalAccessToken().isEmpty()) {
                            pwopat = credentials.getPersonalAccessToken();
                        } else if (credentials.getPassword() != null && !credentials.getPassword().isEmpty()) {
                            pwopat = credentials.getPassword();
                        }
                        break;
                    }
                }
                if(gitAccount == null || pwopat == null) {
                    throw new JocGitException(String.format("No credentials found for Git Server '%1$s'.", hostPort));
                }
                // prepare Uri
                String updatedUri = String.format("%1$s://%2$s:%3$s@%4$s%5$s", protocol, gitAccount, pwopat, hostPort, path);
                // clone
                String folder = filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder();
                GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(updatedUri, folder, repositoryBase, charset);
                if(result.getExitCode() != 0) {
                    throw new JocGitException(String.format("clone command exit code <%1$d> with message: %2$s", 
                            result.getExitCode(), result.getStdErr()), result.getException());
                }
                createOrUpdateFolder(filter.getFolder(), session);
                return result;
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public static final GitCloneCommandResult cloneGitRepositoryWithConfigFile (CloneFilter filter, String account, SOSHibernateSession session,
            Charset charset) throws JsonMappingException, JsonProcessingException, SOSException {
        JocConfigurationDbLayer dbLayer = new JocConfigurationDbLayer(session);
        String hostPort = "";
        String path = "";
        String protocol = "";
        if (isSshUri(filter.getRemoteUrl())) {
            Pattern sshGitUriPattern = Pattern.compile(REGEX_GIT_URI_SSH);
            Matcher sshGitUriMatcher = sshGitUriPattern.matcher(filter.getRemoteUrl());
            if(sshGitUriMatcher.matches()) {
                protocol = DEFAULT_PROTOCOL;
                hostPort = sshGitUriMatcher.group(1);
                path = sshGitUriMatcher.group(2);
            }
        } else {
            URI remoteUri = null;
            try {
                remoteUri = new URI(filter.getRemoteUrl());
                hostPort = remoteUri.getHost();
            } catch (URISyntaxException e) {
                throw new JocBadRequestException(String.format("%1$s is not a Git URL.", filter.getRemoteUrl()), e);
            }
            
            Pattern protocolUriPattern = Pattern.compile(REGEX_GIT_URI_PROTOCOL);
            Matcher protocolUriMatcher = protocolUriPattern.matcher(filter.getRemoteUrl());
            if(protocolUriMatcher.matches()) {
                protocol = protocolUriMatcher.group(1);
                if (!HTTP_PROTOCOL.equals(protocol) && !HTTPS_PROTOCOL.equals(protocol)) {
                    throw new JocBadRequestException(String.format("%1$s is not a Git URL.", filter.getRemoteUrl()));
                }
                String hostPortWithCreds = "";
                if(hostPort == null || hostPort.isEmpty()) {
                    Pattern hostUriPattern = Pattern.compile(REGEX_GIT_URI_HOST);
                    Matcher hostUriMatcher = hostUriPattern.matcher(filter.getRemoteUrl());
                    if(hostUriMatcher.matches()) {
                        hostPortWithCreds = hostUriMatcher.group(1);
                        if (hostPortWithCreds.contains("@")) {
                            hostPort = hostPortWithCreds.split("@")[1];
                        } else {
                            hostPort = hostPortWithCreds;
                        }
                    }
                }
                if(!hostPort.contains(":") && remoteUri.getPort() != -1) {
                    hostPort += ":" + remoteUri.getPort();
                }
                if(remoteUri.getPath() != null && !remoteUri.getPath().isEmpty()) {
                    path = remoteUri.getPath();
                } else if (!hostPortWithCreds.isEmpty()) {
                    path = filter.getRemoteUrl().replace(protocol + "://" + hostPortWithCreds, "");
                }
            }
        }
        if (hostPort.isEmpty() || path.isEmpty() || protocol.isEmpty()) {
            throw new JocBadRequestException(String.format("%1$s is not a Git URL.", filter.getRemoteUrl()));
        }

        try {
            GitCredentialsList credList = null;
            DBItemJocConfiguration dbItem = getJocConfigurationDbItem(account, dbLayer);
            if(dbItem != null) {
                credList =  Globals.objectMapper.readValue(dbItem.getConfigurationItem(), GitCredentialsList.class);
            }
            if(credList == null) {
                if(dbItem != null) {
                    throw new JocGitException("No Git Credentials found for Joc account:  " + dbItem.getAccount());
                } else {
                    throw new JocGitException("No Git Credentials found for current account.");
                }
            }
            if(!credList.getRemoteUrls().contains(filter.getRemoteUrl())) {
                credList.getRemoteUrls().add(filter.getRemoteUrl());
                dbItem.setConfigurationItem(Globals.objectMapper.writeValueAsString(credList));
                dbLayer.saveOrUpdateConfiguration(dbItem);
            }
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            if (workingDir != null) {
                LOGGER.info("working dir: " + workingDir.toString());
            }
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            if(repositoryBase != null) {
                LOGGER.debug("repositoryBase: " + repositoryBase.toString());
            }
            if(protocol.equals(DEFAULT_PROTOCOL)) {
                Path gitKeyfilePath = null;
                String username = null;
                String email = null;
                for(GitCredentials credentials : credList.getCredentials()) {
                    if(credentials.getGitServer().equals(hostPort)) {
                        username = credentials.getUsername();
                        email = credentials.getEmail();
                        if (credentials.getKeyfilePath() == null) {
                            throw new JocGitException(String.format(
                                    "remote URL '%1$s' needs ssh authentication. Missing keyfilePath in credentials for server '%2$s' and account '%3$s'.",
                                    filter.getRemoteUrl(), hostPort, credentials.getGitAccount()));
                        } else if(credentials.getKeyfilePath().isEmpty()) {
                            // use ~/.ssh/id_rsa from home directory
                            // use Path homeDir = Paths.get(System.getProperty("user.home")); for windows and linux
                            gitKeyfilePath = Paths.get(System.getProperty("user.home")).resolve(".ssh/id_rsa");
                        } else if(!credentials.getKeyfilePath().contains("/") && !credentials.getKeyfilePath().contains("\\")) {
                            // filename only, use key from JETTY_BASE/resources/joc/repositories/private
                            gitKeyfilePath = Globals.sosCockpitProperties.resolvePath("repositories").resolve("private")
                                    .resolve(credentials.getKeyfilePath());
                        } else  {
                            gitKeyfilePath = Paths.get(credentials.getKeyfilePath());
                        }
                        break;
                    }
                }
                // check remote connectivity
                GitLsRemoteCommandResult lsRemoteResult = (GitLsRemoteCommandResult)GitCommand.executeGitCheckRemoteConnection(
                        filter.getRemoteUrl(), charset);
                // TODO: rest of check when timeout available from SOSShell
                
                // clone
                String folder = filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder();
                GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(
                        filter.getRemoteUrl(), folder, repositoryBase, charset);
                if(result.getExitCode() != 0) {
                    throw new JocGitException(String.format("clone command exit code <%1$d> with message: %2$s", 
                            result.getExitCode(), result.getStdErr()), result.getException());
                }
                createOrUpdateFolder(filter.getFolder(), session);
                return result;
            } else {
                String username = null;
                String pwopat = null;
                String gitAccount = null;
                for(GitCredentials credentials : credList.getCredentials()) {
                    if(credentials.getGitServer().equals(hostPort)) {
                        gitAccount = credentials.getGitAccount();
                        username = credentials.getUsername();
                        if(credentials.getPersonalAccessToken() != null && !credentials.getPersonalAccessToken().isEmpty()) {
                            pwopat = credentials.getPersonalAccessToken();
                        } else if (credentials.getPassword() != null && !credentials.getPassword().isEmpty()) {
                            pwopat = credentials.getPassword();
                        }
                        break;
                    }
                }
                if(gitAccount == null || pwopat == null) {
                    throw new JocGitException(String.format("No credentials found for Git Server '%1$s'.", hostPort));
                }
                // prepare Uri
                String updatedUri = String.format("%1$s://%2$s:%3$s@%4$s%5$s", protocol, gitAccount, pwopat, hostPort, path);
                // clone
                String folder = filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder();
                GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(updatedUri, folder, repositoryBase, charset);
                if(result.getExitCode() != 0) {
                    throw new JocGitException(String.format("clone command exit code <%1$d> with message: %2$s", 
                            result.getExitCode(), result.getStdErr()), result.getException());
                }
                createOrUpdateFolder(filter.getFolder(), session);
                return result;
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public static final GitAddCommandResult addAllChanges (String account, Path localRepo, Path workingDir, Charset charset)
            throws JsonMappingException, JsonProcessingException {
        GitAddCommandResult result = (GitAddCommandResult)GitCommand.executeGitAddAll(localRepo, workingDir, charset);
        if(result.getExitCode() != 0) {
            throw new JocGitException(String.format("add all command exit code <%1$d> with message: %2$s", 
                    result.getExitCode(), result.getStdErr()), result.getException());
        }
        return result;
    }
    
    public static final GitAddCommandResult addAllChanges (CommonFilter filter, String account, JocConfigurationDbLayer dbLayer,
            Charset charset) throws JsonMappingException, JsonProcessingException {
        try {
            GitCredentialsList credList = getCredentialsList(account, dbLayer);
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            Path localRepo = repositoryBase.resolve(
                    filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder());
            String remoteUri = getActiveRemoteUri(localRepo, workingDir, credList, charset);
            if(remoteUri.isEmpty()) {
                throw new JocGitException("remote Repository not configured for the account: " + account);
            }
            String username = "";
            String userEmail = "";
            String oldUsernameValue = "";
            String oldEmailValue = "";
            for (GitCredentials creds : credList.getCredentials()) {
                if(remoteUri.contains(creds.getGitServer())) {
                    username = creds.getUsername();
                    userEmail = creds.getEmail();
                }
            }
            // set author
            if (!username.isEmpty() && !userEmail.isEmpty()) {
                oldUsernameValue = getOldUsernameValue(charset);
                oldEmailValue = getOldEmailValue(charset);
                prepareConfigUnsetUsername(charset);
                prepareConfigUnsetUserEmail(charset);
                prepareConfigSetUsername(charset, username);
                prepareConfigSetUserEmail(charset, userEmail);
            }
            
            GitAddCommandResult result = (GitAddCommandResult)GitCommand.executeGitAddAll(localRepo, workingDir, charset);
            if(result.getExitCode() != 0) {
                throw new JocGitException(String.format("add all command exit code <%1$d> with message: %2$s", 
                        result.getExitCode(), result.getStdErr()), result.getException());
            }
            // cleanup
            if (!username.isEmpty() && !userEmail.isEmpty()) {
                prepareConfigUnsetUsername(charset);
                prepareConfigUnsetUserEmail(charset);
                if (!oldUsernameValue.isEmpty() && !oldEmailValue.isEmpty()) {
                    prepareConfigRestoreUsername(charset, oldUsernameValue);
                    prepareConfigRestoreUserEmail(charset, oldEmailValue);
                }
            }
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public static final GitCommitCommandResult commitAllStagedChanges(CommitFilter filter, String account, Path localRepo, Path workingDir,
            Charset charset) throws JsonMappingException, JsonProcessingException {
        GitCommitCommandResult result = (GitCommitCommandResult)GitCommand.executeGitCommitFormatted(filter.getMessage() , localRepo,
                workingDir, charset);
        if(result.getExitCode() != 0 && result.getExitCode() != 1) {
            throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                    result.getExitCode(), result.getStdErr()), result.getException());
        }
        return result;
    }
    public static final GitCommitCommandResult commitAllStagedChanges(CommitFilter filter, String account, JocConfigurationDbLayer dbLayer,
            Charset charset) throws JsonMappingException, JsonProcessingException {
        try {
            GitCredentialsList credList = getCredentialsList(account, dbLayer);
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            Path localRepo = repositoryBase.resolve(
                    filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder());
            String remoteUri = getActiveRemoteUri(localRepo, workingDir, credList, charset);
            if(remoteUri.isEmpty()) {
                throw new JocGitException("remote Repository not configured for the account: " + account);
            }
            String username = "";
            String userEmail = "";
            String oldUsernameValue = "";
            String oldEmailValue = "";
            for (GitCredentials creds : credList.getCredentials()) {
                if(remoteUri.contains(creds.getGitServer())) {
                    username = creds.getUsername();
                    userEmail = creds.getEmail();
                }
            }
            // set author
            if (!username.isEmpty() && !userEmail.isEmpty()) {
                oldUsernameValue = getOldUsernameValue(charset);
                oldEmailValue = getOldEmailValue(charset);
                prepareConfigUnsetUsername(charset);
                prepareConfigUnsetUserEmail(charset);
                prepareConfigSetUsername(charset, username);
                prepareConfigSetUserEmail(charset, userEmail);
            }
            GitCommitCommandResult result = (GitCommitCommandResult)GitCommand.executeGitCommitFormatted(filter.getMessage() ,localRepo,
                    workingDir, charset);
            if(result.getExitCode() != 0 && result.getExitCode() != 1) {
                throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                        result.getExitCode(), result.getStdErr()), result.getException());
            }
            // cleanup
            if (!username.isEmpty() && !userEmail.isEmpty()) {
                prepareConfigUnsetUsername(charset);
                prepareConfigUnsetUserEmail(charset);
                if (!oldUsernameValue.isEmpty() && !oldEmailValue.isEmpty()) {
                    prepareConfigRestoreUsername(charset, oldUsernameValue);
                    prepareConfigRestoreUserEmail(charset, oldEmailValue);
                }
            }
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public static final GitPushCommandResult pushCommitedChanges (CommonFilter filter, String account, Path localRepo, Path workingDir,
            Charset charset) throws JsonMappingException, JsonProcessingException {
        GitPushCommandResult result = (GitPushCommandResult)GitCommand.executeGitPush(localRepo, workingDir, charset);
        if(result.getExitCode() != 0) {
            throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                    result.getExitCode(), result.getStdErr()), result.getException());
        }
        return result;
    }

    public static final GitPushCommandResult pushCommitedChanges (CommonFilter filter, String account, JocConfigurationDbLayer dbLayer,
            Charset charset) throws JsonMappingException, JsonProcessingException {
        try {
            GitCredentialsList credList = getCredentialsList(account, dbLayer);
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            Path localRepo = repositoryBase.resolve(
                    filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder());
            String remoteUri = getActiveRemoteUri(localRepo, workingDir, credList, charset);
            if(remoteUri.isEmpty()) {
                throw new JocGitException("remote Repository not configured for the account: " + account);
            }
            Path keyfilePath = null;
            String oldSshCommandValue = "";
            if (isSshUri(remoteUri)) {
                for(GitCredentials creds : credList.getCredentials()) {
                    if (remoteUri.contains(creds.getGitServer())) {
                        keyfilePath = Paths.get(creds.getKeyfilePath());
                    }
                }
                if (keyfilePath != null) {
                    String keyfile = keyfilePath.toString().replace('\\', '/');
                    Path gitKeyfilePath = null;
                    if(keyfile.isEmpty()) {
                        // use ~/.ssh/id_rsa from home directory
                        // use Path homeDir = Paths.get(System.getProperty("user.home")); for windows and linux
                        gitKeyfilePath = Paths.get(System.getProperty("user.home")).resolve(".ssh/id_rsa");
                    } else if(!keyfile.contains("/")) {
                        // filename only, use key from JETTY_BASE/resources/joc/repositories/private
                        gitKeyfilePath = Globals.sosCockpitProperties.resolvePath("repositories").resolve("private")
                                .resolve(keyfile);
                    } else  {
                        gitKeyfilePath = Paths.get(keyfile);
                    }
                    oldSshCommandValue = getOldSshCommandValue(charset);
                    if (!oldSshCommandValue.isEmpty()) {
                        prepareConfigUnsetSshCommand(charset);
                    }
                    prepareConfigSetSshCommand(charset, gitKeyfilePath);
                }
            }
            GitPushCommandResult result = (GitPushCommandResult)GitCommand.executeGitPush(localRepo, workingDir, charset);
            if(result.getExitCode() != 0) {
                throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                        result.getExitCode(), result.getStdErr()), result.getException());
            }
            // cleanup
            if (isSshUri(remoteUri)) {
                if (keyfilePath != null) {
                    prepareConfigUnsetSshCommand(charset);
                }
                if (!oldSshCommandValue.isEmpty()) {
                    prepareConfigRestoreSshCommand(charset, oldSshCommandValue);
                }
            }
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public static final GitPullCommandResult pullChanges (CommonFilter filter, String account, Path localRepo, Path workingDir, 
            Charset charset) throws JsonMappingException, JsonProcessingException {
        GitPullCommandResult result = (GitPullCommandResult)GitCommand.executeGitPull(localRepo, workingDir, charset);
        if(result.getExitCode() != 0) {
            throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                    result.getExitCode(), result.getStdErr()), result.getException());
        }
        return result;
    }
    
    public static final GitPullCommandResult pullChanges (CommonFilter filter, String account, JocConfigurationDbLayer dbLayer, 
            Charset charset) throws JsonMappingException, JsonProcessingException {
        try {
            GitCredentialsList credList = getCredentialsList(account, dbLayer);
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            Path localRepo = repositoryBase.resolve(
                    filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder());
            String remoteUri = getActiveRemoteUri(localRepo, workingDir, credList, charset);
            if(remoteUri.isEmpty()) {
                throw new JocGitException("remote Repository not configured for the account: " + account);
            }
            
            Path keyfilePath = null;
            String oldSshCommandValue = "";
            if (isSshUri(remoteUri)) {
                for(GitCredentials creds : credList.getCredentials()) {
                    if (remoteUri.contains(creds.getGitServer())) {
                        keyfilePath = Paths.get(creds.getKeyfilePath());
                    }
                }
                if (keyfilePath != null) {
                    String keyfile = keyfilePath.toString().replace('\\', '/');
                    Path gitKeyfilePath = null;
                    if(keyfile.isEmpty()) {
                        // use ~/.ssh/id_rsa from home directory
                        // use Path homeDir = Paths.get(System.getProperty("user.home")); for windows and linux
                        gitKeyfilePath = Paths.get(System.getProperty("user.home")).resolve(".ssh/id_rsa");
                    } else if(!keyfile.contains("/")) {
                        // filename only, use key from JETTY_BASE/resources/joc/repositories/private
                        gitKeyfilePath = Globals.sosCockpitProperties.resolvePath("repositories").resolve("private")
                                .resolve(keyfile);
                    } else  {
                        gitKeyfilePath = Paths.get(keyfile);
                    }
                    oldSshCommandValue = getOldSshCommandValue(charset);
                    if (!oldSshCommandValue.isEmpty()) {
                        prepareConfigUnsetSshCommand(charset);
                    }
                    prepareConfigSetSshCommand(charset, gitKeyfilePath);
                }
            }
            
            GitPullCommandResult result = (GitPullCommandResult)GitCommand.executeGitPull(localRepo, workingDir, charset);
            if(result.getExitCode() != 0) {
                throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                        result.getExitCode(), result.getStdErr()), result.getException());
            }
            // cleanup
            if (isSshUri(remoteUri)) {
                if (keyfilePath != null) {
                    prepareConfigUnsetSshCommand(charset);
                }
                if (!oldSshCommandValue.isEmpty()) {
                    prepareConfigRestoreSshCommand(charset, oldSshCommandValue);
                }
            }
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public static final GitCheckoutCommandResult checkout (CheckoutFilter filter, String account, Path localRepo, Path workingDir, 
            Charset charset) throws JsonMappingException, JsonProcessingException {
        GitCheckoutCommandResult result = null;
        if (filter.getBranch() != null) {
            result = (GitCheckoutCommandResult)GitCommand.executeGitCheckout(filter.getBranch(), null, localRepo, workingDir, charset);
        } else { // filter.getTag() != null
            result = (GitCheckoutCommandResult)GitCommand.executeGitCheckout(null, filter.getTag(), localRepo, workingDir, charset);
        }
        
        if(result.getExitCode() != 0) {
            throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                    result.getExitCode(), result.getStdErr()), result.getException());
        }
        return result;
    }

    // TODO: GitCheckoutCommandResult in sos-commons-git
    public static final GitCheckoutCommandResult checkout (CheckoutFilter filter, String account, JocConfigurationDbLayer dbLayer, 
            Charset charset) throws JsonMappingException, JsonProcessingException {
        try {
            GitCredentialsList credList = getCredentialsList(account, dbLayer);
            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            Path localRepo = repositoryBase.resolve(
                    filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder());
            if(getActiveRemoteUri(localRepo, workingDir, credList, charset).isEmpty()) {
                throw new JocGitException("remote Repository not configured for the account: " + account);
            }
            GitCheckoutCommandResult result = null;
            if (filter.getBranch() != null) {
                result = (GitCheckoutCommandResult)GitCommand.executeGitCheckout(filter.getBranch(), null, localRepo, workingDir, charset);
            } else { // filter.getTag() != null
                result = (GitCheckoutCommandResult)GitCommand.executeGitCheckout(null, filter.getTag(), localRepo, workingDir, charset);
            }
            
            if(result.getExitCode() != 0) {
                throw new JocGitException(String.format("commit command exit code <%1$d> with message: %2$s", 
                        result.getExitCode(), result.getStdErr()), result.getException());
            }
            return result;
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }
    
    public static final String getActiveRemoteUri (Path localRepo, Path workingDir, GitCredentialsList credList, Charset charset) {
        GitRemoteCommandResult remoteVResult = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead(localRepo, workingDir, charset);
        if(remoteVResult.getExitCode() != 0) {
            throw new JocGitException(String.format("remote -v command exit code <%1$d> with message: %2$s", 
                    remoteVResult.getExitCode(), remoteVResult.getStdErr()), remoteVResult.getException());
        }
        String remoteUri = "";
        Map<String, String> remoteRepos = remoteVResult.getRemotePushRepositories();
        if(!remoteRepos.isEmpty()) {
            // check if remoteRepos from the local repository matches with the git servers from the credentials of the used account
            for (String shortName : remoteRepos.keySet()) {
                String knownRemoteUri = remoteRepos.get(shortName);
                LOGGER.debug("remote URL from git remote -v command: " + knownRemoteUri);
//                if(knownRemoteUri.startsWith("git@")) {
                    // ssh
                    if(credList == null) {
                        throw new JocGitException("No git credentials configured for the current user.");
                    }
                    if (credList.getCredentials().stream().anyMatch(cred -> knownRemoteUri.contains(cred.getGitServer()))) {
                        remoteUri = knownRemoteUri;
                    } else {
                        throw new JocGitException("No git server from credentials matches with the git server of the remote repository.");
                    }
//                } else {
//                    URI uri = null;
//                    String hostPort = "";
//                    String protocol = "";
//                    String path = "";
//                    Pattern p = Pattern.compile(REGEX_HTTP_URI);
//                    Matcher m = p.matcher(knownRemoteUri);
//                    if(m.matches()) {
//                        protocol = m.group(1);
//                        hostPort = m.group(2);
//                        path = m.group(3);
//                    }
//                    if(hostPort.contains("@")) {
//                        String hp = hostPort.substring(hostPort.lastIndexOf("@") + 1);
//                        String adjustedRemoteUri = String.format(
//                                "%1$s://%2$s%3$s", protocol, hp, (path.startsWith("/") ? path : "/" + path));
//                        if(credList != null && credList.getRemoteUrls().contains(adjustedRemoteUri)) {
//                            remoteUri = adjustedRemoteUri;
//                        }
//                    } else {
//                        if(credList != null && credList.getRemoteUrls().contains(knownRemoteUri)) {
//                            remoteUri = knownRemoteUri;
//                        }
//                    }
//                }
            }
        }
        return remoteUri;
    }
    
    private static final DBItemJocConfiguration getJocConfigurationDbItem (String account, JocConfigurationDbLayer dbLayer)
            throws SOSHibernateException {
        DBItemJocConfiguration dbItem = null;
        JocConfigurationFilter dbFilter = new JocConfigurationFilter();
        dbFilter.setAccount(account);
        dbFilter.setConfigurationType(ConfigurationType.GIT.value());
        List<DBItemJocConfiguration> existing = dbLayer.getJocConfigurations(dbFilter, 0);
        if(existing != null && !existing.isEmpty()) {
            dbItem = existing.get(0);
        }
        return dbItem;
    }
    
    public static final GitCredentialsList getCredentialsList(String account, JocConfigurationDbLayer dbLayer)
            throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        GitCredentialsList credList = null;
        DBItemJocConfiguration dbItem = getJocConfigurationDbItem(account, dbLayer);
        if(dbItem != null) {
            credList =  Globals.objectMapper.readValue(dbItem.getConfigurationItem(), GitCredentialsList.class);
        }
        return credList;
        
    }
    
    private static final boolean isSshUri (String remoteUri) {
        if(remoteUri.startsWith("git@")) {
            return true;
        }
        return false;
    }
    
    public static final String getSubrepositoryFromFilter (CloneFilter filter) {
        switch(filter.getCategory()) {
        case LOCAL:
            return "local";
        case ROLLOUT:
            return "rollout";
        }
        return null;
    }

    public static final String getSubrepositoryFromFilter (CommitFilter filter) {
        switch(filter.getCategory()) {
        case LOCAL:
            return "local";
        case ROLLOUT:
            return "rollout";
        }
        return null;
    }

    public static final String getSubrepositoryFromFilter (CheckoutFilter filter) {
        switch(filter.getCategory()) {
        case LOCAL:
            return "local";
        case ROLLOUT:
            return "rollout";
        }
        return null;
    }

    public static final String getSubrepositoryFromFilter (CommonFilter filter) {
        switch(filter.getCategory()) {
        case LOCAL:
            return "local";
        case ROLLOUT:
            return "rollout";
        }
        return null;
    }
    
    private static final String getOldSshCommandValue (Charset charset) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshGet(GitConfigType.GLOBAL, charset);
            return configResult.getCurrentValue();
        } catch (SOSException e) {
            LOGGER.warn("error occured reading old sshCommand from git config:", e);
            return "";
        }
    }
    
    private static final String getOldUsernameValue (Charset charset) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUsernameGet(
                    GitConfigType.GLOBAL, charset);
            return configResult.getCurrentValue();
        } catch (SOSException e) {
            LOGGER.warn("error occured reading old sshCommand from git config:", e);
            return "";
        }
    }
    
    private static final String getOldEmailValue (Charset charset) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUserEmailGet(
                    GitConfigType.GLOBAL, charset);
            return configResult.getCurrentValue();
        } catch (SOSException e) {
            LOGGER.warn("error occured reading old sshCommand from git config:", e);
            return "";
        }
    }
    
    private static final String getOldSaveDirectory (Charset charset) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSaveDirectoryGet(
                    GitConfigType.GLOBAL, charset);
            return configResult.getCurrentValue();
        } catch (SOSException e) {
            LOGGER.warn("error occured reading old save.directory from git config:", e);
            return "";
        }
    }
    
    private static final void prepareConfigUnsetSshCommand(Charset charset) {
        try {
            GitCommand.executeGitConfigSshUnset(GitConfigType.GLOBAL, charset);
        } catch (SOSException e) {
            LOGGER.warn("could not unset old sshCommand from global git config.");
        }
    }
    
    private static final void prepareConfigUnsetUsername(Charset charset) {
        try {
            GitCommand.executeGitConfigUsernameUnset(GitConfigType.GLOBAL, charset);
        } catch (SOSException e) {
            LOGGER.warn("could not unset old user.name from global git config.");
        }
    }
    
    private static final void prepareConfigUnsetUserEmail(Charset charset) {
        try {
            GitCommand.executeGitConfigUserEmailUnset(GitConfigType.GLOBAL, charset);
        } catch (SOSException e) {
            LOGGER.warn("could not unset old user.email from global git config.");
        }
    }
    
    private static final void prepareConfigUnsetSaveDirectory(Charset charset) {
        try {
            GitCommand.executeGitConfigSaveDirectoryUnset(GitConfigType.GLOBAL, charset);
        } catch (SOSException e) {
            LOGGER.warn("could not unset old save.directory from global git config.");
        }
    }
    
    public static final Path backupGitGlobalConfigFile () throws IOException {
        Path gitConfigPath = Paths.get(System.getProperty("user.home")).resolve(GitCommandConstants.GIT_CONFIG_DEFAULT_FILENAME);
        Path gitConfigBackupPath = Paths.get(System.getProperty("user.home")).resolve(GitCommandConstants.GIT_CONFIG_BACKUP_FILENAME);
        if(Files.exists(gitConfigPath)) {
            Files.copy(gitConfigPath, gitConfigBackupPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return gitConfigBackupPath;
    }
    
    public static final void restoreOriginalGitGlobalConfigFile () throws IOException {
        Path gitConfigPath = Paths.get(System.getProperty("user.home")).resolve(GitCommandConstants.GIT_CONFIG_DEFAULT_FILENAME);
        Path gitConfigBackupPath = Paths.get(System.getProperty("user.home")).resolve(GitCommandConstants.GIT_CONFIG_BACKUP_FILENAME);
        if(Files.exists(gitConfigBackupPath)) {
            Files.copy(gitConfigBackupPath, gitConfigPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    public static final void restoreOriginalGitGlobalConfigFile (Path backupPath) throws IOException {
        Path gitConfigPath = Paths.get(System.getProperty("user.home")).resolve(GitCommandConstants.GIT_CONFIG_DEFAULT_FILENAME);
        if(Files.exists(backupPath)) {
            Files.copy(backupPath, gitConfigPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
    
    public static final GitConfigCommandResult prepareConfigFileForCloning (Charset charset, GitCredentials credentials) {
        return prepareConfigFile(charset, 
            (credentials.getKeyfilePath()!= null ? Paths.get(credentials.getKeyfilePath()) : null), 
            null, credentials.getUsername(), credentials.getEmail(), 
            (credentials.getPassword() == null ? credentials.getPersonalAccessToken() : credentials.getPassword()));
    }
    
    public static final GitConfigCommandResult prepareConfigFile (Charset charset, GitCredentials credentials, Path localRepoPath) {
        return prepareConfigFile(charset, 
            (credentials.getKeyfilePath()!= null ? Paths.get(credentials.getKeyfilePath()) : null), localRepoPath, credentials.getUsername(),
                credentials.getEmail(), (credentials.getPassword() == null ? credentials.getPersonalAccessToken() : credentials.getPassword()));
    }
    
    public static final GitConfigCommandResult prepareConfigFile (Charset charset, Path gitKeyFilePath, Path localRepoPath, String username,
            String userEmail, String pwd) {
        GitConfigCommandResult configResult = null;
        Path gitConfigPath = Paths.get(System.getProperty("user.home")).resolve(GitCommandConstants.GIT_CONFIG_DEFAULT_FILENAME);
        Path gitConfigTmpPath = Paths.get(System.getProperty("java.io.tmpdir"))
                .resolve(GitCommandConstants.GIT_CONFIG_DEFAULT_FILENAME + "_"+ username);
        // this file will be created additionally for support purposes
        Path gitConfigLastUsedPath = Paths.get(System.getProperty("user.home")).resolve(GitCommandConstants.GIT_CONFIG_DEFAULT_FILENAME + "_lastUsed");
        try {
            if(!Files.exists(gitConfigTmpPath)) {
                Files.createFile(gitConfigTmpPath);
            }
            if(gitKeyFilePath != null) {
                configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAdd(GitConfigType.FILE, gitKeyFilePath, charset,
                        gitConfigTmpPath);
                if(configResult.getExitCode() != 0) {
                    throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                            configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                }
            }
            if(username != null) {
                configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUsernameAdd(GitConfigType.FILE, username, charset,
                        gitConfigTmpPath);
                if(configResult.getExitCode() != 0) {
                    throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                            configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                }
            }
            if(pwd != null) {
              configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUserpwdAdd(GitConfigType.FILE, pwd, charset, gitConfigTmpPath);
              if(configResult.getExitCode() != 0) {
                  throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                          configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
              }
            }
            if (userEmail != null) {
                configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUserEmailAdd(GitConfigType.FILE, userEmail, charset,
                        gitConfigTmpPath);
                if(configResult.getExitCode() != 0) {
                    throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                            configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                }
            }
            if (localRepoPath != null) {
                configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSaveDirectoryAdd(GitConfigType.FILE, localRepoPath, charset,
                        gitConfigTmpPath);
                if(configResult.getExitCode() != 0) {
                    throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                            configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                }
            }
            if(Files.exists(gitConfigTmpPath)) {
                Files.copy(gitConfigTmpPath, gitConfigPath, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(gitConfigTmpPath, gitConfigLastUsedPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                LOGGER.info("No temporary git config file found!");
            }
            return configResult;
        } catch (SOSException e) {
            LOGGER.warn("could not write git config file: ", e);
            return null;
        } catch (IOException e) {
            LOGGER.warn("could not copy temporary .gitconfig file to home directory: ", e);
            return null;
        }
    }
    
    private static final GitConfigCommandResult prepareConfigSetSshCommand(Charset charset, Path gitKeyFilePath) {
        GitConfigCommandResult configResult;
        try {
            configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAdd(GitConfigType.GLOBAL, gitKeyFilePath, charset);
            if(configResult.getExitCode() != 0) {
                throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                        configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
            }
            return configResult;
        } catch (SOSException e) {
            LOGGER.warn("could not set new sshCommand to global git config.", e);
            return null;
        }
    }
    
    private static final GitConfigCommandResult prepareConfigSetUsername(Charset charset, String username) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUsernameAdd(
                    GitConfigType.GLOBAL, username, charset);
            if(configResult.getExitCode() != 0) {
                throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                        configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
            }
            return configResult;
        } catch (SOSException e) {
            LOGGER.warn("could not set new user.name to global git config.", e);
            return null;
        }
    }
    
    private static final GitConfigCommandResult prepareConfigSetUserEmail(Charset charset, String email) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUserEmailAdd(
                    GitConfigType.GLOBAL, email, charset);
            if(configResult.getExitCode() != 0) {
                throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                        configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
            }
            return configResult;
        } catch (SOSException e) {
            LOGGER.warn("could not set new user.email to global git config.", e);
            return null;
        }
    }
    
    private static final GitConfigCommandResult prepareConfigSetSaveDirectoryCommand(Charset charset, Path localRrepository) {
        GitConfigCommandResult configResult;
        try {
            configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSaveDirectoryAdd(GitConfigType.GLOBAL, localRrepository,
                    charset);
            if(configResult.getExitCode() != 0) {
                throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                        configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
            }
            return configResult;
        } catch (SOSException e) {
            LOGGER.warn("could not set new save.directory to global git config.", e);
            return null;
        }
    }
    
    private static void prepareConfigRestoreSshCommand(Charset charset, String oldSshCommandValue) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAddCustom(
                    GitConfigType.GLOBAL, oldSshCommandValue, charset);
            if(configResult.getExitCode() != 0) {
                LOGGER.warn(String.format("update config command exit code <%1$d> with message: %2$s", 
                        configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
            }
        } catch (SOSException e) {
            LOGGER.warn("could not restore old sshCommand to global git config.", e);
        }
    }
    
    private static void prepareConfigRestoreUsername(Charset charset, String oldUsernameValue) {
        try {
            if(!oldUsernameValue.isEmpty()) {
                GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUsernameAdd(
                        GitConfigType.GLOBAL, oldUsernameValue, charset);
                if(configResult.getExitCode() != 0) {
                    LOGGER.warn(String.format("update config command exit code <%1$d> with message: %2$s", 
                            configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                }
            }
        } catch (SOSException e) {
            LOGGER.warn("could not restore old sshCommand to global git config.", e);
        }
    }
    
    private static void prepareConfigRestoreUserEmail(Charset charset, String oldEmailValue) {
        try {
            if(!oldEmailValue.isEmpty()) {
                GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigUserEmailAdd(
                        GitConfigType.GLOBAL, oldEmailValue, charset);
                if(configResult.getExitCode() != 0) {
                    LOGGER.warn(String.format("update config command exit code <%1$d> with message: %2$s", 
                            configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                }
            }
        } catch (SOSException e) {
            LOGGER.warn("could not restore old sshCommand to global git config.", e);
        }
    }
    
    private static void prepareConfigRestoreSaveDirectoryCommand(Charset charset, Path oldLocalRepository) {
        try {
            GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAdd(
                    GitConfigType.GLOBAL, charset, oldLocalRepository);
            if(configResult.getExitCode() != 0) {
                LOGGER.warn(String.format("update config command exit code <%1$d> with message: %2$s", 
                        configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
            }
        } catch (SOSException e) {
            LOGGER.warn("could not restore old save.directory to global git config.", e);
        }
    }
    
    private static void createOrUpdateFolder(String foldername, SOSHibernateSession session) throws SOSHibernateException {
        InventoryDBLayer dbLayer = new InventoryDBLayer(session);
        DBItemInventoryConfiguration dbFolder = dbLayer.getConfiguration(foldername, 
                com.sos.joc.model.inventory.common.ConfigurationType.FOLDER.intValue());
        if (dbFolder == null) {
            Path folderPath = Paths.get(foldername);
            dbFolder = new DBItemInventoryConfiguration();
            dbFolder.setPath(foldername);
            dbFolder.setFolder(folderPath.getParent().toString().replace('\\', '/'));
            dbFolder.setName(folderPath.getFileName().toString());
            dbFolder.setDeployed(false);
            dbFolder.setReleased(false);
            dbFolder.setModified(Date.from(Instant.now()));
            dbFolder.setAuditLogId(0L);
            dbFolder.setContent(null);
            dbFolder.setCreated(dbFolder.getModified());
            dbFolder.setDeleted(false);
            dbFolder.setRepoControlled(true);
            dbFolder.setId(null);
            dbFolder.setTitle(null);
            dbFolder.setType(com.sos.joc.model.inventory.common.ConfigurationType.FOLDER);
            dbFolder.setValid(true);
            dbLayer.getSession().save(dbFolder);
        } else {
            dbFolder.setRepoControlled(true);
            dbFolder.setModified(Date.from(Instant.now()));
            dbLayer.getSession().update(dbFolder);
        }
    }

    public static final GitCredentials getCredentials(String account, Path workingDir, Path localRepo, JocConfigurationDbLayer dbLayer)
            throws JsonMappingException, JsonProcessingException, SOSHibernateException {
        GitCredentialsList credList = GitCommandUtils.getCredentialsList(account, dbLayer);
        String remoteUri = GitCommandUtils.getActiveRemoteUri(localRepo, workingDir, credList, StandardCharsets.UTF_8);
        for (GitCredentials creds : credList.getCredentials()) {
            if(remoteUri.contains(creds.getGitServer())) {
                return creds;
            }
        }
        return null;
    }

    public static final GitCredentials getCredentialsForCloning(String account, String newRepoRemoteUrl, JocConfigurationDbLayer dbLayer)
            throws JsonMappingException, JsonProcessingException, SOSHibernateException {
        GitCredentialsList credList = GitCommandUtils.getCredentialsList(account, dbLayer);
//        String remoteUri = GitCommandUtils.getActiveRemoteUri(localRepo, workingDir, credList, StandardCharsets.UTF_8);
        if (credList != null) {
          for (GitCredentials creds : credList.getCredentials()) {
            if(newRepoRemoteUrl.contains(creds.getGitServer())) {
                return creds;
            }
          }
        } else {
          throw new JocGitException(
              String.format("no credentials found for account - %1$s -. Configure the credentials in Profile -> GitManagement of the account.",
                  account)); 
        }
        return null;
    }
}

