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
    private static final String REGEX_FIND_NEW = "\\?\\?\\s(.*)";
    private static final String REGEX_FIND_ADDED = "A[^M]\\s*(.*)";
    private static final String REGEX_FIND_ADDED_AND_MODIFIED = "AM\\s*(.*)";
    private Set<Path> modified = Collections.emptySet();
    private Set<Path> toAdd = Collections.emptySet();
    private Set<Path> added = Collections.emptySet();
    private Set<Path> addedAndModified = Collections.emptySet();
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
    
    public Set<Path> getToAdd() {
        return toAdd;
    }
    
    public Set<Path> getAdded() {
        return added;
    }
    
    public Set<Path> getAddedAndModified() {
        return addedAndModified;
    }
    
    @Override
    public void parseStdOut() {
        try {
            if (getStdOut() != null && !getStdOut().isEmpty()) {
                Pattern modifiedPattern = Pattern.compile(REGEX_FIND_MODIFIED);
                Pattern toAddPattern = Pattern.compile(REGEX_FIND_NEW);
                Pattern addedPattern = Pattern.compile(REGEX_FIND_ADDED);
                Pattern addedAndModifiedPattern = Pattern.compile(REGEX_FIND_ADDED_AND_MODIFIED);
                Reader reader = new StringReader(getStdOut());
                BufferedReader buff = new BufferedReader(reader);
                String line = null;
                while ((line = buff.readLine()) != null) {
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    Matcher modifiedMatcher = modifiedPattern.matcher(line);
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
                    Matcher toAddMatcher = toAddPattern.matcher(line);
                    if(toAddMatcher.matches()) {
                        if(toAdd.isEmpty()) {
                            toAdd = new HashSet<Path>();
                        }
                        String newToAdd = toAddMatcher.group(1);
                        if (newToAdd.contains("\"")) {
                            newToAdd = newToAdd.replace("\"", "");
                        }
                        toAdd.add(Paths.get(newToAdd));
                        continue;
                    }
                    Matcher addedMatcher = addedPattern.matcher(line);
                    if(addedMatcher.matches()) {
                        if(added.isEmpty()) {
                            added = new HashSet<Path>();
                        }
                        String alreadyAdded = addedMatcher.group(1);
                        if (alreadyAdded.contains("\"")) {
                            alreadyAdded = alreadyAdded.replace("\"", "");
                        }
                        added.add(Paths.get(alreadyAdded));
                        continue;
                    }
                    Matcher addedAndModifiedMatcher = addedAndModifiedPattern.matcher(line);
                    if(addedAndModifiedMatcher.matches()) {
                        if(addedAndModified.isEmpty()) {
                            addedAndModified = new HashSet<Path>();
                        }
                        String alreadyAddedAndModified = addedAndModifiedMatcher.group(1);
                        if (alreadyAddedAndModified.contains("\"")) {
                            alreadyAddedAndModified = alreadyAddedAndModified.replace("\"", "");
                        }
                        addedAndModified.add(Paths.get(alreadyAddedAndModified));
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
