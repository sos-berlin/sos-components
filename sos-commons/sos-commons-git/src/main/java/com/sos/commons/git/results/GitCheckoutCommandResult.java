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

public class GitCheckoutCommandResult extends GitCommandResult {

    private static final String REGEX_AHEAD = "^.*ahead.*'(.*)'\\D*(\\d*).*$";
    private static final String REGEX_BEHIND = "^.*behind.*'(.*)'\\D*(\\d*).*$";
    private static final String REGEX_UPTODATE = "^.*up to date.*'(.*)'.*$";
    private static final String REGEX_TAG_NAME = "^Note.*'(.*)'.*$";
    private static final String REGEX_TAG_COMMIT_HASH = "^HEAD.*at\\s([a-z,0-9]{7,})\\s(.*)$";
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCheckoutCommandResult.class);
    private String remoteBranchName;
    private String tagname;
    private String commitHash;
    private String commitMsg;
    private Integer aheadCount = 0;
    private Integer behindCount = 0;

    protected GitCheckoutCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitCheckoutCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }

    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitCheckoutCommandResult(result, original);
    }
    
    public String getRemoteBranchName() {
        return remoteBranchName;
    }
    
    public String getTagname() {
        return tagname;
    }

    public Integer getAheadCount() {
        return aheadCount;
    }

    public Integer getBehindCount() {
        return behindCount;
    }
    
    public String getCommitHash() {
        return commitHash;
    }

    public String getCommitMsg() {
        return commitMsg;
    }

    @Override
    public void parseStdOut() {
        try {
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                Pattern upToDatePattern = Pattern.compile(REGEX_UPTODATE);
                Pattern aheadPattern = Pattern.compile(REGEX_AHEAD);
                Pattern behindPattern = Pattern.compile(REGEX_BEHIND);
                Pattern tagnamePattern = Pattern.compile(REGEX_TAG_NAME);
                Pattern commitHashPattern = Pattern.compile(REGEX_TAG_COMMIT_HASH);
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Matcher upToDateMatcher = upToDatePattern.matcher(line);
                    Matcher aheadMatcher = aheadPattern.matcher(line);
                    Matcher behindMatcher = behindPattern.matcher(line);
                    Matcher tagnameMatcher = tagnamePattern.matcher(line);
                    Matcher commitHashMatcher = commitHashPattern.matcher(line);
                    if(upToDateMatcher.matches()) {
                        remoteBranchName = upToDateMatcher.group(1);
                        continue;
                    }
                    if(aheadMatcher.matches()) {
                        remoteBranchName = upToDateMatcher.group(1);
                        aheadCount = Integer.parseInt(aheadMatcher.group(2));
                        continue;
                    }
                    if (behindMatcher.matches()) {
                        remoteBranchName = upToDateMatcher.group(1);
                        behindCount = Integer.parseInt(aheadMatcher.group(2));
                        continue;
                    }
                    if(tagnameMatcher.matches()) {
                        tagname = tagnameMatcher.group(1);
                    }
                    if(commitHashMatcher.matches()) {
                        commitHash = commitHashMatcher.group(1);
                        commitMsg = commitHashMatcher.group(2);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
