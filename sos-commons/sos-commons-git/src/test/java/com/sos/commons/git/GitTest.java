package com.sos.commons.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.commons.git.enums.GitConfigType;
import com.sos.commons.git.results.GitCloneCommandResult;
import com.sos.commons.git.results.GitCommandResult;
import com.sos.commons.git.results.GitCommitCommandResult;
import com.sos.commons.git.results.GitConfigCommandResult;
import com.sos.commons.git.results.GitLogCommandResult;
import com.sos.commons.git.results.GitPullCommandResult;
import com.sos.commons.git.results.GitRemoteCommandResult;
import com.sos.commons.git.results.GitStatusShortCommandResult;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitTest.class);
    private static final String FILE_ENTRY = "test entry with random number ";
    private static Path workingDir = Paths.get(System.getProperty("user.dir"));
    private static Path homeDir = Paths.get(System.getProperty("user.home"));
    private static String repoToClone = "git@github.com:sos-berlin/test.git";
    private static Path repositoryParent = homeDir.resolve("tmp/test_repos");
    private static Path repository = repositoryParent.resolve("test");
    // Windows -> development environment OR Linux -> Jenkins Build
    private static Path gitKeyfilePath = SOSShell.IS_WINDOWS ? homeDir.resolve(".ssh/id_rsa") : Paths.get("~/.ssh/id_rsa");
    
    
    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.debug(" **************************  Git Command Tests started  ******************************");
        LOGGER.debug("Working Directory: " + workingDir.toString());
        LOGGER.debug("Repository parent path: " + repositoryParent.toString());
        LOGGER.debug("Working Test Repository: " + repoToClone);
        LOGGER.debug("Repository path: " + repository.toString());
        LOGGER.debug("keyfile path: " + gitKeyfilePath.toString());
        // prepare target folder for repository tests
        try {
            if(!Files.exists(repositoryParent)) {
                Files.createDirectories(repositoryParent);
            }
        } catch (IOException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.debug("target folder cleanup");
        try {
            if(Files.exists(repositoryParent)) {
                deleteFolderRecursively(repositoryParent);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LOGGER.debug(" **************************  Git Command Tests finished ******************************");
    }

    @Test
    public void test01GitStatusShort() {
        LOGGER.debug(" **************************  Test 01 - git status short started   ********************");
        GitStatusShortCommandResult result = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(Charset.defaultCharset());
        LOGGER.debug(" command: " + result.getOriginalCommand());
        LOGGER.debug(" ExitCode: " + result.getExitCode());
        LOGGER.debug(" StdOut:\n" + result.getStdOut());
        LOGGER.debug(" modified Files: relative and resolved path");
        for (Path filename : result.getModified()) {
            LOGGER.debug("   " + filename.toString());
            LOGGER.debug("   " + workingDir.resolve(filename).normalize().toString());
        }
        LOGGER.debug(" Files to add: relative and resolved path");
        for(Path filename : result.getToAdd()) {
            LOGGER.debug("   " + filename.toString());
            LOGGER.debug("   " + workingDir.resolve(filename).normalize().toString());
        }
        LOGGER.debug(" added Files: relative and resolved path");
        for(Path filename : result.getAdded()) {
            LOGGER.debug("   " + filename.toString());
            LOGGER.debug("   " + workingDir.resolve(filename).normalize().toString());
        }
        LOGGER.debug(" **************************  Test 01a - git status short finished  *******************");
    }

    @Test
    public void test02GitPull() {
        LOGGER.debug("**************************  Test 02 - git pull started  *****************************");
        GitCloneCommandResult cloneResult = (GitCloneCommandResult)GitCommand.executeGitClone(repoToClone, repositoryParent, workingDir, Charset.defaultCharset());
        Path repository = repositoryParent.resolve(cloneResult.getClonedInto());
        LOGGER.debug("Repository path: " + repository.toString());
        GitPullCommandResult pullResult = (GitPullCommandResult)GitCommand.executeGitPull(repository, workingDir, Charset.defaultCharset());
        LOGGER.debug("command: " + pullResult.getOriginalCommand());
        LOGGER.debug("ExitCode: " + pullResult.getExitCode());
        LOGGER.debug("StdOut:\n" + pullResult.getStdOut());
        LOGGER.debug("StdOut parsed - results:");
        LOGGER.debug("\tpulled changes:\t\t" + pullResult.getChangesCount());
        LOGGER.debug("\tpulled insertions:\t" + pullResult.getInsertionsCount());
        LOGGER.debug("\tpulled deletions:\t" + pullResult.getDeletionsCount());
//        Assert.assertTrue(pullResult.getExitCode() == 0);
        Assert.assertTrue(Files.exists(repositoryParent.resolve(cloneResult.getClonedInto())));
        if(Files.exists(repositoryParent.resolve(cloneResult.getClonedInto()))) {
            LOGGER.debug("target folder cleanup");
            try {
                deleteFolderRecursively(repositoryParent.resolve(cloneResult.getClonedInto()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Assert.assertTrue(!Files.exists(repositoryParent.resolve(cloneResult.getClonedInto())));
        LOGGER.debug("**************************  Test 02 - git pull finished  ****************************");
    }
    
    @Test
    public void test03GitAddAll() {
        LOGGER.debug("**************************  Test 03 - git add all started  **************************");
        GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(repoToClone, repositoryParent, workingDir, Charset.defaultCharset());
        if (result.getExitCode() == 0) {
            Path repository = repositoryParent.resolve(result.getClonedInto());
            String testfileName = "sp_git_test%1$d.txt";
            for (int i = 1; i <= 3; i++)
                try {
                    if (!Files.exists(repository.resolve(String.format(testfileName, i)))) {
                        createFile(repository.resolve(String.format(testfileName, i)));
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            GitStatusShortCommandResult statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            LOGGER.debug("Status before ADD");
            LOGGER.debug("command: " + statusResult.getOriginalCommand());
            LOGGER.debug("StdOut:\n" + statusResult.getStdOut());
            Assert.assertTrue(!statusResult.getToAdd().isEmpty());
            Assert.assertTrue(statusResult.getModified().isEmpty());
            Assert.assertTrue(statusResult.getAdded().isEmpty());
            GitCommandResult addAllResult = GitCommand.executeGitAddAll(repository, workingDir, Charset.defaultCharset());
            LOGGER.debug("command: " + addAllResult.getOriginalCommand());
            LOGGER.debug("ExitCode: " + addAllResult.getExitCode());
            LOGGER.debug("StdOut: " + addAllResult.getStdOut());
            LOGGER.debug("StdErr: " + addAllResult.getStdErr());
            LOGGER.debug("Status after ADD");
            statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            LOGGER.debug("command: " + statusResult.getOriginalCommand());
            LOGGER.debug("StdOut:\n" + statusResult.getStdOut());
            Assert.assertTrue(statusResult.getToAdd().isEmpty());
            Assert.assertTrue(statusResult.getModified().isEmpty());
            Assert.assertTrue(!statusResult.getAdded().isEmpty());
            
        }
        LOGGER.debug("**************************  Test 03 - git add all finished  *************************");
    }
    
    @Test
    public void test04GitCommit() {
        LOGGER.debug("**************************  Test 04 - git commit started            *****************");
        GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(repoToClone, repositoryParent, workingDir, Charset.defaultCharset());
        if (result.getExitCode() == 0) {
            Path repository = repositoryParent.resolve(result.getClonedInto());
            String testfileName = "sp_git_test.txt";
            try {
                if (!Files.exists(repository.resolve(testfileName))) {
                    createFile(repository.resolve(testfileName));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            GitStatusShortCommandResult statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            LOGGER.debug("Status before ADD");
            LOGGER.debug("StdOut:\n" + statusResult.getStdOut());
            Assert.assertTrue(!statusResult.getToAdd().isEmpty());
            GitCommandResult addAllResult = GitCommand.executeGitAddAll(repository, workingDir, Charset.defaultCharset());
            statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            LOGGER.debug("Status after first ADD");
            LOGGER.debug("StdOut:\n" + statusResult.getStdOut());
            Assert.assertTrue(!statusResult.getAdded().isEmpty());
            try {
                addEntryToFile(repository.resolve(testfileName));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            Assert.assertTrue(!statusResult.getAddedAndModified().isEmpty());
            addAllResult = GitCommand.executeGitAddAll(repository, workingDir, Charset.defaultCharset());
            statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            Assert.assertTrue(!statusResult.getAdded().isEmpty());
            Assert.assertTrue(statusResult.getAddedAndModified().isEmpty());
            GitCommitCommandResult commitResult = (GitCommitCommandResult)GitCommand.executeGitCommitFormatted("from Junit commit test",repository, workingDir, Charset.defaultCharset());
            LOGGER.debug("ExitCode: " + commitResult.getExitCode());
            LOGGER.debug("StdOut:\n" + commitResult.getStdOut());
        }
        LOGGER.debug("**************************  Test 04 - git commit finished            ****************");
    }
    
    @Ignore
    @Test
    public void test05GitCheckout() {
        LOGGER.debug("**************************  Test 05 - git checkout started          *****************");
        GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(repoToClone, repositoryParent, workingDir, Charset.defaultCharset());
        if (result.getExitCode() == 0) {
            Path repository = repositoryParent.resolve(result.getClonedInto());
            String testfileName = "sp_git_test.txt";
            try {
                if (!Files.exists(repository.resolve(testfileName))) {
                    createFile(repository.resolve(testfileName));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            GitStatusShortCommandResult statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            LOGGER.debug("Status before ADD");
            LOGGER.debug("StdOut:\n" + statusResult.getStdOut());
            Assert.assertTrue(!statusResult.getToAdd().isEmpty());
            GitCommandResult addAllResult = GitCommand.executeGitAddAll(repository, workingDir, Charset.defaultCharset());
            statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            LOGGER.debug("Status after first ADD");
            LOGGER.debug("StdOut:\n" + statusResult.getStdOut());
            Assert.assertTrue(!statusResult.getAdded().isEmpty());
            try {
                addEntryToFile(repository.resolve(testfileName));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            Assert.assertTrue(!statusResult.getAddedAndModified().isEmpty());
            addAllResult = GitCommand.executeGitAddAll(repository, workingDir, Charset.defaultCharset());
            statusResult = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort(repository, Charset.defaultCharset());
            Assert.assertTrue(!statusResult.getAdded().isEmpty());
            Assert.assertTrue(statusResult.getAddedAndModified().isEmpty());
            GitCommitCommandResult commitResult = (GitCommitCommandResult)GitCommand.executeGitCommitFormatted("from Junit commit test",repository, workingDir, Charset.defaultCharset());
            LOGGER.debug("ExitCode: " + commitResult.getExitCode());
            LOGGER.debug("StdOut:\n" + commitResult.getStdOut());
        }
        LOGGER.debug("**************************  Test 05 - git checkout finished          ****************");
    }
    
    @Test
    public void test06GitPush() {
        LOGGER.debug("**************************  Test 06 - git push started              *****************");
        GitCloneCommandResult cloneResult = (GitCloneCommandResult)GitCommand.executeGitClone(repoToClone, repositoryParent, workingDir, Charset.defaultCharset());
        if (cloneResult.getExitCode() == 0) {
            Path repository = repositoryParent.resolve(cloneResult.getClonedInto());
            String testfileName = "sp_git_test.txt";
            try {
                if (!Files.exists(repository.resolve(testfileName))) {
                    createFile(repository.resolve(testfileName));
                } else {
                    addEntryToFile(repository.resolve(testfileName));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            GitCommandResult result = GitCommand.executeGitAddAll(repository, workingDir, Charset.defaultCharset());
            result = GitCommand.executeGitCommitFormatted("from Junit commit test",repository, workingDir, Charset.defaultCharset());
            result = GitCommand.executeGitPush(repository, workingDir, Charset.defaultCharset());
            LOGGER.debug("ExitCode: " + result.getExitCode());
            LOGGER.debug("StdOut:\n" + result.getStdOut());
            Assert.assertTrue(result.getExitCode() == 0);
        }
        LOGGER.debug("**************************  Test 06 - git commit finished            ****************");
    }

    @Ignore
    @Test
    public void test07GitRestoreStagedSuccessful() {
        LOGGER.debug("**************************  Test 07 - git restore --staged started  *****************");
        LOGGER.debug("**************************                 successful               *****************");
        LOGGER.debug("Repository path: " + repository.toString());
        GitCommandResult result = GitCommand.executeGitRestore(repository, workingDir, Charset.defaultCharset());
        LOGGER.debug("command: " + result.getOriginalCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("**************************  Test 07 - git restore --staged finished  ****************");
    }
    
    @Test
    public void test08GitRestoreStagedFailed() {
        LOGGER.debug("**************************  Test 08 - git restore --staged started  *****************");
        LOGGER.debug("**************************                   failed                 *****************");
        GitCommandResult result = GitCommand.executeGitRestore(Charset.defaultCharset());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdErr: " + result.getStdErr());
        LOGGER.debug("error: " + result.getError());
        Assert.assertTrue(result.getExitCode() != 0);
        Assert.assertTrue(!result.getStdErr().isEmpty());
        LOGGER.debug("**************************  Test 08 - git restore --staged finished  ****************");
    }
    
    @Test
    public void test09GitRemoteV() {
        LOGGER.debug("**************************  Test 09 - git remote started   **************************");
        GitRemoteCommandResult result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead(Charset.defaultCharset());
        LOGGER.debug("command: " + result.getOriginalCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("StdOut parsed - results:");
        LOGGER.debug("fetch repositories: ");
        result.getRemoteFetchRepositories().forEach((k,v) -> {
            LOGGER.debug("short name: " + k + "\tURI: " + v);
        });
        LOGGER.debug("push repositories: ");
        result.getRemotePushRepositories().forEach((k,v) -> {
            LOGGER.debug("short name: " + k + "\tURI: " + v);
        });
        Assert.assertTrue(!result.getRemoteFetchRepositories().isEmpty());
        Assert.assertTrue(!result.getRemotePushRepositories().isEmpty());
        LOGGER.debug("**************************  Test 09 - git remote finished  **************************");
    }

    @Test
    public void test10GitLog() {
        LOGGER.debug("**************************  Test 10 - git log started      **************************");
        GitLogCommandResult result = (GitLogCommandResult)GitCommand.executeGitLogParseable(Charset.defaultCharset());
        LOGGER.debug("command: " + result.getOriginalCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut parsed - results:");
        int i = 0;
        LOGGER.debug("Last four commits:");
        for(Map.Entry<String, String> entry : result.getCommits().entrySet()) {
            if(i++ <= 3) {
                LOGGER.debug("commitHash: " + entry.getKey() + " message: " + entry.getValue());
            }
        }
        LOGGER.debug("commit count: " + result.getCommits().keySet().size());
        Assert.assertTrue(!result.getCommits().isEmpty());
        LOGGER.debug("**************************  Test 10 - git log finished  *****************************");
    }

    @Test
    public void test11Clone() {
        LOGGER.debug("**************************  Test 11 - git clone started    **************************");
        GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(repoToClone, repositoryParent, workingDir, Charset.defaultCharset());
        LOGGER.debug("command: " + result.getOriginalCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("StdErr: " + result.getStdErr());
        LOGGER.debug("StdOut parsed - results:");
        LOGGER.debug("  child folder " + result.getClonedInto() + " with git entries created in " + repositoryParent.toString() + " !");
//        Assert.assertTrue(Files.exists(repositoryParent.resolve(result.getClonedInto())));
        if(Files.exists(repositoryParent.resolve(result.getClonedInto()))) {
            try {
                deleteFolderRecursively(repositoryParent.resolve(result.getClonedInto()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            LOGGER.debug("target folder cleanup");
            Assert.assertTrue(!Files.exists(repositoryParent.resolve(result.getClonedInto())));
        }
        LOGGER.debug("**************************  Test 11 - git clone finished   **************************");
    }

    @Test
    public void test12ChangeSSHConfigThenClone() throws SOSException {
        LOGGER.debug("**************************  Test 12 - git config started   **************************");
        LOGGER.debug("**************************         specific directory      **************************");
        LOGGER.debug("**************************  Step 1: read current config    **************************");
        GitConfigCommandResult configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshGet(GitConfigType.GLOBAL, Charset.defaultCharset());
        LOGGER.debug("command: " + configResult.getCommand());
        LOGGER.debug("ExitCode: " + configResult.getExitCode());
        LOGGER.debug("StdOut:\n" + configResult.getStdOut());
        LOGGER.debug("StdErr: " + configResult.getStdErr());
        String oldValue = configResult.getCurrentValue();
        LOGGER.debug("**************************  Step 2: remove current config  **************************");
        configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshUnset(GitConfigType.GLOBAL, Charset.defaultCharset());
        LOGGER.debug("command: " + configResult.getCommand());
        LOGGER.debug("ExitCode: " + configResult.getExitCode());
        LOGGER.debug("StdOut:\n" + configResult.getStdOut());
        LOGGER.debug("StdErr: " + configResult.getStdErr());
        LOGGER.debug("**************************  Step 3: add new config         **************************");
        configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAdd(GitConfigType.GLOBAL, gitKeyfilePath, Charset.defaultCharset());
        LOGGER.debug("command: " + configResult.getCommand());
        LOGGER.debug("ExitCode: " + configResult.getExitCode());
        LOGGER.debug("StdOut:\n" + configResult.getStdOut());
        LOGGER.debug("StdErr: " + configResult.getStdErr());
        LOGGER.debug("**************************  Step 4: clone                  **************************");
        GitCloneCommandResult result = (GitCloneCommandResult)GitCommand.executeGitClone(repoToClone, repositoryParent, workingDir, Charset.defaultCharset());
        LOGGER.debug("command: " + result.getOriginalCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("StdErr: " + result.getStdErr());
        LOGGER.debug("StdOut parsed - results:");
        LOGGER.debug("  child folder " + result.getClonedInto() + " with git entries created in " + repositoryParent.toString() + " !");
        try {
            if(Files.exists(repositoryParent.resolve(result.getClonedInto()))) {
                deleteFolderRecursively(repositoryParent.resolve(result.getClonedInto()));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LOGGER.debug("target folder cleanup");
        Assert.assertTrue(!Files.exists(repositoryParent.resolve(result.getClonedInto())));
        LOGGER.debug("**************************  Step 5: remove new config      **************************");
        configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshUnset(GitConfigType.GLOBAL, Charset.defaultCharset());
        LOGGER.debug("command: " + configResult.getCommand());
        LOGGER.debug("ExitCode: " + configResult.getExitCode());
        LOGGER.debug("StdOut:\n" + configResult.getStdOut());
        LOGGER.debug("StdErr: " + configResult.getStdErr());
        LOGGER.debug("**************************  Step 6: restore current config **************************");
        if(!oldValue.isEmpty()) {
            configResult = (GitConfigCommandResult)GitCommand.executeGitConfigSshAddCustom(GitConfigType.GLOBAL, oldValue, Charset.defaultCharset());
            LOGGER.debug("command: " + configResult.getCommand());
            LOGGER.debug("ExitCode: " + configResult.getExitCode());
            LOGGER.debug("StdOut:\n" + configResult.getStdOut());
            LOGGER.debug("StdErr: " + configResult.getStdErr());
        }
        LOGGER.debug("**************************  Test 12 - git config finished  **************************");
    }

    @Ignore
    @Test
    public void test13GitRemoteAddUpdateRemoveChain() {
        LOGGER.debug("**************************  Test 13 - git remote started  ***************************");
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());
        LOGGER.debug("**************************  Step 1: read current state     **************************");
        GitRemoteCommandResult result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead(repository, workingDir, Charset.defaultCharset());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("StdOut parsed - results:");
        LOGGER.debug("fetch repositories: ");
        result.getRemoteFetchRepositories().forEach((k,v) -> {
            LOGGER.debug("short name: " + k + "\tURI: " + v);
        });
        LOGGER.debug("push repositories: ");
        result.getRemotePushRepositories().forEach((k,v) -> {
            LOGGER.debug("short name: " + k + "\tURI: " + v);
        });
        LOGGER.debug("**************************  Step 2: add other remote repo  **************************");
        String shortName = "other";
        String remoteUri = "git@github.com:sos-berlin/sos-components.git";
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteAdd(shortName, remoteUri, repository, workingDir, Charset.defaultCharset());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("**************************  Step 3: read state again       **************************");
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead(repository, workingDir, Charset.defaultCharset());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("StdOut parsed - results:");
        LOGGER.debug("fetch repositories: ");
        Map <String,String> remoteRepos = result.getRemoteFetchRepositories();
        remoteRepos.forEach((k,v) -> {
            LOGGER.debug("short name: " + k + "\tURI: " + v);
        });
        LOGGER.debug("push repositories: ");
        result.getRemotePushRepositories().forEach((k,v) -> {
            LOGGER.debug("short name: " + k + "\tURI: " + v);
        });
        LOGGER.debug("**************************  Step 4: update from added repo **************************");
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteUpdate(repository, workingDir, Charset.defaultCharset());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
//        LOGGER.debug("StdErr:\n" + result.getStdErr());
        LOGGER.debug("StdOut parsed - results:");
        Map<String, Map<String, String>> newBranches = result.getFetchedNewBranches();
        Map<String, Map<String, String>> newTags = result.getFetchedNewTags();
        LOGGER.debug("new Branches:");
        newBranches = newBranches.entrySet().stream().collect(Collectors.toMap(
                entry -> {
                    String longName = entry.getKey();
                    for (Map.Entry<String, String> e : remoteRepos.entrySet()) {
                        if(e.getValue().contains(longName)) {
                            return e.getKey();
                        }
                    }
                    return entry.getKey();
                }, entry -> entry.getValue()));
        newTags = newTags.entrySet().stream().collect(Collectors.toMap(
                entry -> {
                    String longName = entry.getKey();
                    for (Map.Entry<String, String> e : remoteRepos.entrySet()) {
                        if(e.getValue().contains(longName)) {
                            return e.getKey();
                        }
                    }
                    return entry.getKey();
                }, entry -> entry.getValue()));
        newBranches.forEach((repo, branches) -> {
            LOGGER.debug("from repo: " + repo);
            LOGGER.debug("fetched branches:");
            TreeMap <String, String> sortedBranches = new TreeMap<String, String>(branches);
            sortedBranches.forEach((local, remote) -> {
                LOGGER.debug("local: " + local + " remote: " + remote);
            });
        });
        newTags.forEach((repo, tags) -> {
            LOGGER.debug("from repo: " + repo);
            LOGGER.debug("fetched tags:");
            TreeMap<String, String> sortedTags = new TreeMap<String, String>(tags);
            sortedTags.forEach((local, remote) -> {
                LOGGER.debug("local: " + local + " remote: " + remote);
            });
        });
        LOGGER.debug("**************************  Step 5: remove added repo      **************************");
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRemove(shortName, repository, workingDir, Charset.defaultCharset());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("**************************  Step 6: cleanup                **************************");
        LOGGER.debug("cleanup detached but still fetched tags from removed repo");
        String cdTo = "cd " + repository.toString();
        String cdBack = "cd " + workingDir.toString();
        SOSCommandResult r = SOSShell.executeCommand(cdTo + " && FOR /f \"tokens=*\" %a in ('git tag') DO git tag -d %a");
        SOSShell.executeCommand(cdBack);
        Assert.assertTrue(r.getExitCode() == 0);
        LOGGER.debug("**************************  Test 13 - git remote finished  **************************");
    }

    @Ignore
    @Test
    public void testFiles() {
        String testfileName = "sp_git_tests.txt";
        try {
            createFile(repositoryParent.resolve(testfileName));
            addEntryToFile(repositoryParent.resolve(testfileName));
            addEntryToFile(repositoryParent.resolve(testfileName));
            addEntryToFile(repositoryParent.resolve(testfileName));
            overwriteEntryInFile(repositoryParent.resolve(testfileName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static void deleteFolderRecursively(Path path) throws IOException {
        Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
    }
    
    private void createFile (Path file) throws IOException {
        if(!Files.exists(file)) {
            Files.createFile(file);
        }
        String fileEntry = "new file entry:\n" + FILE_ENTRY + getRandomNumber() + "\n";
        Files.write(file, fileEntry.getBytes(), StandardOpenOption.APPEND);
    }

    private void addEntryToFile (Path file) throws IOException {
        String fileEntry = "added file entry:\n" + FILE_ENTRY + getRandomNumber() + "\n"; 
        if(Files.exists(file)) {
            Files.write(file, fileEntry.getBytes(), StandardOpenOption.APPEND);
        }
    }
    
    private void overwriteEntryInFile (Path file) throws IOException {
        String numberEntryRegex = "^(\\D+\\s\\d+)$";
        String titleEntryRegex = "^(added\\D*:)$";
        String getNumberRegex = "\\D*(\\d+)";
        if(Files.exists(file)) {
            String content = new String(Files.readAllBytes(file));
            Reader reader = new StringReader(content);
            BufferedReader buff = new BufferedReader(reader);
            String line = null;
            String lastContentFound = null;
            String lastTitleFound = null;
            while ((line = buff.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                Pattern findEntry = Pattern.compile(numberEntryRegex);
                Matcher findEntryMatcher = findEntry.matcher(line);
                if (findEntryMatcher.matches()) {
                    lastContentFound = findEntryMatcher.group(1);
                    continue;
                }
                Pattern findTitle = Pattern.compile(titleEntryRegex);
                Matcher findTitleMatcher = findTitle.matcher(line);
                if(findTitleMatcher.matches()) {
                    lastTitleFound = findTitleMatcher.group(1);
                }
            }
            if(lastContentFound != null) {
                String toReplaceCombined = lastTitleFound + "\n" + lastContentFound;
                String newContent = toReplaceCombined.replace("added", "overwritten");
                Pattern numberPattern = Pattern.compile(getNumberRegex);
                Matcher numberMatcher = numberPattern.matcher(newContent);
                String oldNumber=null;
                if(numberMatcher.matches()) {
                    oldNumber = numberMatcher.group(1);
                }
                newContent = newContent.replace(oldNumber, "" + getRandomNumber());
                newContent = content.replace(toReplaceCombined, newContent);
                Files.write(file, newContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
    }
    
    private Integer getRandomNumber() {
        Random rnd = new Random();
        return rnd.nextInt(1000);
    }
}
