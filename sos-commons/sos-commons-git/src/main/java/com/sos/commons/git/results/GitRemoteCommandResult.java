package com.sos.commons.git.results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.git.enums.RepositoryLinkType;
import com.sos.commons.git.enums.RepositoryUpdateType;
import com.sos.commons.util.common.SOSCommandResult;

public class GitRemoteCommandResult extends GitCommandResult {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GitRemoteCommandResult.class);
    private static final String REGEX_REMOTE_V = "^(\\S*)\\s*(\\S*)\\s*(\\S*)$";
    
    private static final String REGEX_REMOTE_UPDATE_FETCH_REF = "^Fetching\\s+(\\S+)$";
    private static final String REGEX_REMOTE_UPDATE__INFO_REPO = "^From\\s*(\\S*)$";
    private static final String REGEX_REMOTE_UPDATE_INFO = "^\\s*\\*\\s*(\\[.*\\])\\s*(\\S+)\\s*\\->\\s*(\\S*)$";
    private static final String STDOUT_STARTS_WITH = "Fetching"; 
    private Map<String, String> remoteFetchRepositories = Collections.emptyMap();
    private Map<String, String> remotePushRepositories = Collections.emptyMap();
    private Map<String, Map<String, String>> fetchedNewBranches = Collections.emptyMap();
    private Map<String, Map<String, String>> fetchedNewTags = Collections.emptyMap();

    
    protected GitRemoteCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitRemoteCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitRemoteCommandResult(result, original);
    }
    
    public Map<String, String> getRemoteFetchRepositories() {
        return remoteFetchRepositories;
    }
    
    public Map<String, String> getRemotePushRepositories() {
        return remotePushRepositories;
    }
    
    public Map<String, Map<String, String>> getFetchedNewBranches() {
        return fetchedNewBranches;
    }

    public Map<String, Map<String, String>> getFetchedNewTags() {
        return fetchedNewTags;
    }

    @Override
    public void parseStdOut() {
        try {
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                if (getStdOut().startsWith(STDOUT_STARTS_WITH)) {
                    boolean matched = false;
                    while ((line = buff.readLine()) != null) {
                        if (line.trim().length() == 0) {
                            continue;
                        }
                        Pattern remoteUpdateRefPattern = Pattern.compile(REGEX_REMOTE_UPDATE_FETCH_REF);
                        Matcher remoteUpdateRefMatcher = remoteUpdateRefPattern.matcher(line);
                        if(remoteUpdateRefMatcher.matches()) {
                            matched = true;
                            continue;
                        }
                    }
                    if(matched) {
                        parseStdErr();
                    }
                } else {
                    Pattern remoteVersionsPattern = Pattern.compile(REGEX_REMOTE_V);
                    while ((line = buff.readLine()) != null) {
                        if (line.trim().length() == 0) {
                            continue;
                        }
                        Matcher remoteVersionsMatcher = remoteVersionsPattern.matcher(line);
                        if(remoteVersionsMatcher.matches()) {
                            if(remoteVersionsMatcher.groupCount() == 3) {
                                if(RepositoryLinkType.FETCH.value().equals(remoteVersionsMatcher.group(3))) {
                                    if(remoteFetchRepositories.isEmpty()) {
                                        remoteFetchRepositories = new HashMap<String, String>();
                                    }
                                    remoteFetchRepositories.put(remoteVersionsMatcher.group(1), remoteVersionsMatcher.group(2));
                                } else if (RepositoryLinkType.PUSH.value().equals(remoteVersionsMatcher.group(3))) {
                                    if(remotePushRepositories.isEmpty()) {
                                        remotePushRepositories = new HashMap<String, String>();
                                    }
                                    remotePushRepositories.put(remoteVersionsMatcher.group(1), remoteVersionsMatcher.group(2));
                                }
                            }
                            continue;
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void parseStdErr() {
        try {
            if (getStdErr() != null && !getStdErr().isEmpty()) {
                Reader reader = new StringReader(getStdErr());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                Pattern remoteUpdateInfoPattern = Pattern.compile(REGEX_REMOTE_UPDATE_INFO);
                Pattern remoteUpdateInfoRepoPattern = Pattern.compile(REGEX_REMOTE_UPDATE__INFO_REPO);
                String currentRepo = "";
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Matcher remoteUpdateInfoMatcher = remoteUpdateInfoPattern.matcher(line);
                    Matcher remoteUpdateInfoRepoMatcher = remoteUpdateInfoRepoPattern.matcher(line);
                    if(remoteUpdateInfoRepoMatcher.matches()) {
                        currentRepo = remoteUpdateInfoRepoMatcher.group(1);
                        continue;
                    } else if(remoteUpdateInfoMatcher.matches()) {
                        if(RepositoryUpdateType.BRANCH.value().equals(remoteUpdateInfoMatcher.group(1))) {
                            if (fetchedNewBranches.isEmpty()) {
                                fetchedNewBranches = new HashMap();
                            }
                            if(!fetchedNewBranches.containsKey(currentRepo)) {
                                Map<String,String> branch = new HashMap<String, String>();
                                branch.put(remoteUpdateInfoMatcher.group(2), remoteUpdateInfoMatcher.group(3));
                                fetchedNewBranches.put(currentRepo, branch);
                            } else {
                                fetchedNewBranches.get(currentRepo).put(remoteUpdateInfoMatcher.group(2), remoteUpdateInfoMatcher.group(3));
                            }
                        } else if (RepositoryUpdateType.TAG.value().equals(remoteUpdateInfoMatcher.group(1))) {
                            if(fetchedNewTags.isEmpty()) {
                                fetchedNewTags = new HashMap();
                            }
                            if(!fetchedNewTags.containsKey(currentRepo)) {
                                Map<String,String> tag = new HashMap<String, String>();
                                tag.put(remoteUpdateInfoMatcher.group(2), remoteUpdateInfoMatcher.group(3));
                                fetchedNewTags.put(currentRepo, tag);
                            } else {
                                fetchedNewTags.get(currentRepo).put(remoteUpdateInfoMatcher.group(2), remoteUpdateInfoMatcher.group(3));
                            }
                        }
                        continue;
                    }               }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
}
