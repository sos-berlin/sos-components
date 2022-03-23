package com.sos.commons.git.results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.SOSCommandResult;

public class GitCloneCommandResult extends GitCommandResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitCloneCommandResult.class);
    private static final String REGEX_CLONIG_INTO = "^Cloning into\\s'(.*)'.*$";
    private static final String REGEX_COUNT = "^remote\\:\\s*Total\\s*(\\d*)\\s*\\(delta\\s(\\d*)\\),\\s*reused\\s*(\\d*)\\s*\\(delta\\s*(\\d*)\\),\\s*pack-reused\\s*(\\d*)$";
    private String clonedInto = "";
    private Integer total = 0;
    private Integer totalDelta = 0;
    private Integer reused = 0;
    private Integer reusedDelta = 0;
    private Integer packReused = 0;

    protected GitCloneCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitCloneCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitCloneCommandResult(result, original);
    }
    
    
    public String getClonedInto() {
        return clonedInto;
    }

    
    public Integer getTotal() {
        return total;
    }

    
    public Integer getTotalDelta() {
        return totalDelta;
    }

    
    public Integer getReused() {
        return reused;
    }

    
    public Integer getReusedDelta() {
        return reusedDelta;
    }

    
    public Integer getPackReused() {
        return packReused;
    }

    @Override
    public void parseStdOut() {
        try {
            Pattern cloningIntoPattern = Pattern.compile(REGEX_CLONIG_INTO);
            Pattern countPattern = Pattern.compile(REGEX_COUNT);
            Reader reader = null;
            BufferedReader buff = null;
            String line = null;
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                reader = new StringReader(getStdOut());
            } else if (getStdErr() != null && !getStdErr().isEmpty()) {
                reader = new StringReader(getStdErr());
            }
            if(reader != null) {
                buff = new BufferedReader(reader);
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Matcher cloningIntoMatcher = cloningIntoPattern.matcher(line);
                    Matcher countMatcher = countPattern.matcher(line);
                    if(cloningIntoMatcher.matches()) {
                        clonedInto = cloningIntoMatcher.group(1);
                        continue;
                    }
                    if(countMatcher.matches()) {
                        total = Integer.parseInt(countMatcher.group(1));
                        totalDelta = Integer.parseInt(countMatcher.group(2));
                        reused = Integer.parseInt(countMatcher.group(3));
                        reusedDelta = Integer.parseInt(countMatcher.group(4));
                        packReused = Integer.parseInt(countMatcher.group(5));
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
