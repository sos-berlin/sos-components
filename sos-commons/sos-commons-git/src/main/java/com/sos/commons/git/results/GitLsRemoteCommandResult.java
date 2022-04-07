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

public class GitLsRemoteCommandResult extends GitCommandResult {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GitLsRemoteCommandResult.class);
    private static final String REGEX_LS_REMOTE_V = "^([^\\s]*)\\s*(.*)$";
    private String commitHash;
    private String revision;
    private String revisionRef;
    
    protected GitLsRemoteCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitLsRemoteCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitLsRemoteCommandResult(result, original);
    }
    
    public String getCommitHash() {
        return commitHash;
    }

    
    public String getRevision() {
        return revision;
    }

    public String getRevisionRef() {
        return revisionRef;
    }

    @Override
    public void parseStdOut() {
        try {
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Pattern remoteLsRemotePattern = Pattern.compile(REGEX_LS_REMOTE_V);
                    Matcher remoteLsRemoteMatcher = remoteLsRemotePattern.matcher(line);
                    int count = 0;
                    if(remoteLsRemoteMatcher.matches()) {
                        if(count == 0) {
                            commitHash = remoteLsRemoteMatcher.group(1);
                            revision = remoteLsRemoteMatcher.group(2);
                            count++;
                        } else {
                            revisionRef = remoteLsRemoteMatcher.group(2);
                        }
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
