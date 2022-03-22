package com.sos.commons.git.results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.SOSCommandResult;

public class GitLogCommandResult extends GitCommandResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitLogCommandResult.class);
    private static final String REGEX_LOG_PARSEABLE = "^(\\S*)\\s*(.*)$";
    private Map<String, String> commits = Collections.emptyMap();

    protected GitLogCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitLogCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitLogCommandResult(result, original);
    }
    
    public Map<String, String> getCommits() {
        return commits;
    }

    @Override
    public void parseStdOut() {
        try {
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                boolean matched = false;
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Pattern logPattern = Pattern.compile(REGEX_LOG_PARSEABLE);
                    Matcher logMatcher = logPattern.matcher(line);
                    if(logMatcher.matches()) {
                        if( commits.isEmpty()) {
                            commits = new LinkedHashMap<String, String>();
                        }
                        commits.put(logMatcher.group(1), logMatcher.group(2));
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        
    }

}
