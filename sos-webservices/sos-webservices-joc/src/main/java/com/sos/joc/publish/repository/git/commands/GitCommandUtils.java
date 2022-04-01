package com.sos.joc.publish.repository.git.commands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.git.GitCommand;
import com.sos.commons.git.enums.GitConfigType;
import com.sos.commons.git.results.GitCloneCommandResult;
import com.sos.commons.git.results.GitConfigCommandResult;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocGitException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.publish.git.GitCredentials;
import com.sos.joc.model.publish.git.GitCredentialsList;
import com.sos.joc.model.publish.git.commands.CloneFilter;

public class GitCommandUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommandUtils.class);
    private static final String REGEX_GIT_URI_SSH = "^\\S*@(\\S*):(\\S*)$";
    private static final String REGEX_GIT_URI_HTTP = "^(\\S*)://(\\S*):(\\S*)$";
    private static final String DEFAULT_PROTOCOL = "ssh";
    
    public static final void cloneGitRepository (CloneFilter filter, String account, JocConfigurationDbLayer dbLayer)
            throws JsonMappingException, JsonProcessingException, SOSException {
        String hostPort = "";
        String path = "";
        String protocol = "";
        if (isSshUri(filter.getRemoteUri())) {
            Pattern sshGitUriPattern = Pattern.compile(REGEX_GIT_URI_SSH);
            Matcher sshGitUriMatcher = sshGitUriPattern.matcher(filter.getRemoteUri());
            if(sshGitUriMatcher.matches()) {
                protocol = DEFAULT_PROTOCOL;
                hostPort = sshGitUriMatcher.group(1);
                path = sshGitUriMatcher.group(2);
            }
        } else {
            Pattern httpGitUriPattern = Pattern.compile(REGEX_GIT_URI_HTTP);
            Matcher httpGitUriMatcher = httpGitUriPattern.matcher(filter.getRemoteUri());
            if(httpGitUriMatcher.matches()) {
                protocol = httpGitUriMatcher.group(1);
                hostPort = httpGitUriMatcher.group(2);
                path = httpGitUriMatcher.group(3);
            }
        }
        if (hostPort.isEmpty() || path.isEmpty() || protocol.isEmpty()) {
            throw new JocBadRequestException(String.format("%1$s is not a Git Uri.", filter.getRemoteUri()));
        }

        try {
            JocConfigurationFilter dbFilter = new JocConfigurationFilter();
            dbFilter.setAccount(account);
            dbFilter.setConfigurationType(ConfigurationType.GIT.value());
            List<DBItemJocConfiguration> existing = dbLayer.getJocConfigurations(dbFilter, 0);
            GitCredentialsList credList = null;
            DBItemJocConfiguration dbItem = null;
            if(existing != null && !existing.isEmpty()) {
                dbItem = existing.get(0);
                credList =  Globals.objectMapper.readValue(dbItem.getConfigurationItem(), GitCredentialsList.class);
            }
            if(!credList.getRemoteUris().contains(filter.getRemoteUri())) {
                credList.getRemoteUris().add(filter.getRemoteUri());
                dbItem.setConfigurationItem(Globals.objectMapper.writeValueAsString(credList));
                dbLayer.saveOrUpdateConfiguration(dbItem);
            }
            Path workingDir = Paths.get(System.getProperty("user.dir"));

            Path repositoryBase = Globals.sosCockpitProperties.resolvePath("repositories").resolve(getSubrepositoryFromFilter(filter));
            if(protocol.equals(DEFAULT_PROTOCOL)) {
                Path gitKeyfilePath = null;
                for(GitCredentials credentials : credList.getCredentials()) {
                    if(credentials.getGitServer().equals(hostPort)) {
                        if(credentials.getKeyfilePath().isEmpty()) {
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
                // prepare git config environment
                GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshGet(GitConfigType.GLOBAL);
                String oldValue = configResult.getCurrentValue();
                configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshUnset(GitConfigType.GLOBAL);
                configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAdd(GitConfigType.GLOBAL, gitKeyfilePath);
                if(configResult.getExitCode() != 0) {
                    throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                            configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                }
                // clone
                String folder = filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder();
                GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(
                        filter.getRemoteUri(), folder, repositoryBase, workingDir);
                if(result.getExitCode() != 0) {
                    throw new JocGitException(String.format("clone command exit code <%1$d> with message: %2$s", 
                            result.getExitCode(), result.getStdErr()), result.getException());
                }
                // cleanup git config environment
                configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshUnset(GitConfigType.GLOBAL);
                if(!oldValue.isEmpty()) {
                    configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAddCustom(GitConfigType.GLOBAL, oldValue);
                    if(configResult.getExitCode() != 0) {
                        throw new JocGitException(String.format("update config command exit code <%1$d> with message: %2$s", 
                                configResult.getExitCode(), configResult.getStdErr()), configResult.getException());
                    }
                }
            } else {
                String gitAccount = null;
                String pwopat = null;
                for(GitCredentials credentials : credList.getCredentials()) {
                    if(credentials.getGitServer().equals(hostPort)) {
                        gitAccount = credentials.getGitAccount();
                        if(credentials.getPersonalAccessToken() != null && !credentials.getPersonalAccessToken().isEmpty()) {
                            pwopat = credentials.getPersonalAccessToken();
                        } else if (credentials.getPassword() != null && !credentials.getPassword().isEmpty()) {
                            pwopat = credentials.getPassword();
                        }
                        break;
                    }
                }
                if(gitAccount == null || pwopat == null) {
                    throw new JocGitException(String.format("No credentials found for Git Server %1$s", hostPort));
                }
                // prepare Uri
                String updatedUri = String.format("%1$s://%2$s:%3$s@%4$s:%5$s", protocol, gitAccount, pwopat, hostPort, path);
                // clone
                String folder = filter.getFolder().startsWith("/") ? filter.getFolder().substring(1) : filter.getFolder();
                GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(
                        updatedUri, folder, repositoryBase, workingDir);
                if(result.getExitCode() != 0) {
                    throw new JocGitException(String.format("clone command exit code <%1$d> with message: %2$s", 
                            result.getExitCode(), result.getStdErr()), result.getException());
                }
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        
    }
    
    private static final boolean isSshUri (String remoteUri) {
        if(remoteUri.startsWith("git@")) {
            return true;
        }
        return false;
    }
    
    private static final String getSubrepositoryFromFilter (CloneFilter filter) {
        switch(filter.getCategory()) {
        case LOCAL:
            return "local";
        case ROLLOUT:
            return "rollout";
        }
        return null;
    }
}
