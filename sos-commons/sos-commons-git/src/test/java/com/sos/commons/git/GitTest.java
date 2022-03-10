package com.sos.commons.git;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitTest.class);
    
    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.info("**************************  Repository Tests started  *******************************");
        LOGGER.info("");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.info("**************************  Deployment Tests finished  ******************************");
    }

    @Test
    public void test01GitStatus() {
        LOGGER.info("**************************  Test 01 - git status started  ***************************");
        LOGGER.info("**************************  status current directory      ***************************");
        LOGGER.info("Working Directory: " + System.getProperty("user.dir"));
        GitCommandResult result = GitCommand.executeGitStatus();
        LOGGER.info("command: " + result.getCommand());
        LOGGER.info("ExitCode: " + result.getExitCode());
        LOGGER.info("hashCode: " + result.hashCode());
        LOGGER.info("StdOut:\n" + result.getStdOut());
        LOGGER.info("StdErr: " + result.getStdErr());
        LOGGER.info("error: " + result.getError());
        LOGGER.info("**************************  Test 01 - git status finished  **************************");
    }

    @Test
    public void test02GitStatusShort() {
        LOGGER.info("**************************  Test 02 - git status short started  *********************");
        LOGGER.info("**************************  status current directory      ***************************");
        LOGGER.info("Working Directory: " + System.getProperty("user.dir"));
        GitStatusShortCommandResult result = GitCommand.executeGitStatusShort();
        LOGGER.info("command: " + result.getCommand());
        LOGGER.info("ExitCode: " + result.getExitCode());
        LOGGER.info("hashCode: " + result.hashCode());
        LOGGER.info("StdOut:\n" + result.getStdOut());
        LOGGER.info("StdErr: " + result.getStdErr());
        LOGGER.info("error: " + result.getError());
        LOGGER.info("modified Files: filename and full path");
        for (Path filename : result.getModified()) {
            LOGGER.info("   " + filename.toString());
            LOGGER.info("   " + Paths.get(System.getProperty("user.dir")).resolve(filename).normalize().toString());
        }
        LOGGER.info("added Files: filename and full path");
        for(Path filename : result.getAdded()) {
            LOGGER.info("   " + filename.toString());
            LOGGER.info("   " + Paths.get(System.getProperty("user.dir")).resolve(filename).normalize().toString());
        }
        LOGGER.info("**************************  Test 02 - git status short finished  ********************");
    }

    @Ignore
    @Test
    public void test03GitStatusShort() {
        LOGGER.info("**************************  Test 03 - git status short started  *********************");
        LOGGER.info("**************************  status specific directory      **************************");
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.info("Working Directory " + repository.toString());
        GitStatusShortCommandResult result = GitCommand.executeGitStatusShort(repository);
        LOGGER.info("command: " + result.getCommand());
        LOGGER.info("ExitCode: " + result.getExitCode());
        LOGGER.info("hashCode: " + result.hashCode());
        LOGGER.info("StdOut:\n" + result.getStdOut());
        LOGGER.info("StdErr: " + result.getStdErr());
        LOGGER.info("error: " + result.getError());
        LOGGER.info("modified Files: filename and full path");
        for (Path filename : result.getModified()) {
            LOGGER.info("   " + filename.toString());
            LOGGER.info("   " + repository.resolve(filename).normalize().toString());
        }
        LOGGER.info("added Files: filename and full path");
        for(Path filename : result.getAdded()) {
            LOGGER.info("   " + filename.toString());
            LOGGER.info("   " + repository.resolve(filename).normalize().toString());
        }
        LOGGER.info("**************************  Test 03 - git status short finished  ********************");
    }

    @Test
    public void test04GitPull() {
        LOGGER.info("**************************  Test 04 - git pull started  *****************************");
        GitCommandResult result = GitCommand.executeGitPull();
        LOGGER.info("ExitCode: " + result.getExitCode());
        LOGGER.info("hashCode: " + result.hashCode());
        LOGGER.info("StdOut:\n" + result.getStdOut());
        LOGGER.info("StdErr: " + result.getStdErr());
        LOGGER.info("error: " + result.getError());
        LOGGER.info("**************************  Test 04 - git pull finished  ****************************");
    }
    
}
