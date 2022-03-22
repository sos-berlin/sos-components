package com.sos.commons.git.results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.git.util.GitCommandConstants;
import com.sos.commons.util.common.SOSCommandResult;

public class GitCommitCommandResult extends GitCommandResult {

    private static final String REGEX_COMMIT_HASH_AND_MESSAGE = "\\[.{4,}\\s([a-z,0-9]{7})\\]\\s(.*)";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommitCommandResult.class);
    private String commitHash;
    private String commitMessage;
    private Integer changesCount;
    private Integer insertionsCount;
    private Integer deletionsCount;

    protected GitCommitCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitCommitCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitCommitCommandResult(result, original);
    }
    
    public String getCommitHash() {
        return commitHash;
    }
    
    public String getCommitMessage() {
        return commitMessage;
    }

    public Integer getChangesCount() {
        return changesCount;
    }

    public Integer getInsertionsCount() {
        return insertionsCount;
    }
    
    public Integer getDeletionsCount() {
        return deletionsCount;
    }

    @Override
    public void parseStdOut() {
        try {
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                Pattern hashAndMessagePattern = Pattern.compile(REGEX_COMMIT_HASH_AND_MESSAGE);
                Pattern commitChangesCountPattern = Pattern.compile(GitCommandConstants.REGEX_CHANGES_COUNT);
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Matcher hashAndMessageMatcher = hashAndMessagePattern.matcher(line);
                    Matcher commitChangesCountMatcher = commitChangesCountPattern.matcher(line);
                    if(hashAndMessageMatcher.matches()) {
                        commitHash = hashAndMessageMatcher.group(1);
                        commitMessage = hashAndMessageMatcher.group(2);
                        continue;
                    }
                    if(commitChangesCountMatcher.matches()) {
                        changesCount = Integer.parseInt(commitChangesCountMatcher.group(1));
                        insertionsCount = Integer.parseInt(commitChangesCountMatcher.group(2));
                        deletionsCount = Integer.parseInt(commitChangesCountMatcher.group(3));
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
