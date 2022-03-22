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

public class GitPullCommandResult extends GitCommandResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitPullCommandResult.class);
    private Integer changesCount = 0;
    private Integer insertionsCount = 0;
    private Integer deletionsCount = 0;

    protected GitPullCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitPullCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitPullCommandResult(result, original);
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
                Pattern pulledChangesCountPattern = Pattern.compile(GitCommandConstants.REGEX_CHANGES_COUNT);
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Matcher pulledChangesCountMatcher = pulledChangesCountPattern.matcher(line);
                    if(pulledChangesCountMatcher.matches()) {
                        changesCount = Integer.parseInt(pulledChangesCountMatcher.group(1));
                        insertionsCount = Integer.parseInt(pulledChangesCountMatcher.group(2));
                        deletionsCount = Integer.parseInt(pulledChangesCountMatcher.group(3));
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
