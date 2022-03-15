package com.sos.commons.git.results;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.SOSCommandResult;

public class GitStatusShortCommandResult extends GitCommandResult {
    
    private static final String REGEX_FIND_MODIFIED = "\\sM\\s(.*)";
    private static final String REGEX_FIND_ADDED = "\\?\\?\\s(.*)";
    private Set<Path> modified = Collections.emptySet();
    private Set<Path> added = Collections.emptySet();
    private static final Logger LOGGER = LoggerFactory.getLogger(GitStatusShortCommandResult.class);
    

    protected GitStatusShortCommandResult(SOSCommandResult result) {
        super(result);
        parseStdOut();
    }

    protected GitStatusShortCommandResult(SOSCommandResult result, String original) {
        super(result, original);
        parseStdOut();
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result) {
        return getInstance(result, null);
    }
    
    public static GitCommandResult getInstance(SOSCommandResult result, String original) {
        return new GitStatusShortCommandResult(result, original);
    }
    
    public Set<Path> getModified() {
        return modified;
    }
    
    public Set<Path> getAdded() {
        return added;
    }
    
    @Override
    public void parseStdOut() {
        try {
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                Pattern modifiedPattern = Pattern.compile(REGEX_FIND_MODIFIED);
                Pattern addedPattern = Pattern.compile(REGEX_FIND_ADDED);
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Matcher modifiedMatcher = modifiedPattern.matcher(line);
                    Matcher addedMatcher = addedPattern.matcher(line);
                    if(modifiedMatcher.matches()) {
                        if(modified.isEmpty()) {
                            modified = new HashSet<Path>();
                        }
                        String toModify = modifiedMatcher.group(1);
                        if(toModify.contains("\"")) {
                            toModify = toModify.replace("\"", "");
                        }
                        modified.add(Paths.get(toModify));
                        continue;
                    }
                    if(addedMatcher.matches()) {
                        if(added.isEmpty()) {
                            added = new HashSet<Path>();
                        }
                        String toAdd = addedMatcher.group(1);
                        if (toAdd.contains("\"")) {
                            toAdd = toAdd.replace("\"", "");
                        }
                        added.add(Paths.get(toAdd));
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
