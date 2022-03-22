package com.sos.commons.git;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
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

import com.sos.commons.git.results.GitCommandResult;
import com.sos.commons.git.results.GitLogCommandResult;
import com.sos.commons.git.results.GitPullCommandResult;
import com.sos.commons.git.results.GitRemoteCommandResult;
import com.sos.commons.git.results.GitStatusShortCommandResult;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitTest.class);
    
    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.debug(" **************************  Repository Tests started  *******************************");
        LOGGER.debug("");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.debug(" **************************  Deployment Tests finished  ******************************");
    }

    @Test
    public void test01aGitStatusShort() {
        LOGGER.debug(" **************************  Test 01a - git status short started  ********************");
        LOGGER.debug(" **************************         current directory      ***************************");
        LOGGER.debug(" Working Directory: " + System.getProperty("user.dir"));
        GitStatusShortCommandResult result = (GitStatusShortCommandResult) GitCommand.executeGitStatusShort();
        LOGGER.debug(" command: " + result.getCommand());
        LOGGER.debug(" ExitCode: " + result.getExitCode());
        LOGGER.debug(" StdOut:\n" + result.getStdOut());
        LOGGER.debug(" modified Files: relative and resolved path");
        for (Path filename : result.getModified()) {
            LOGGER.debug("   " + filename.toString());
            LOGGER.debug("   " + Paths.get(System.getProperty("user.dir")).resolve(filename).normalize().toString());
        }
        LOGGER.debug(" added Files: relative and resolved path");
        for(Path filename : result.getAdded()) {
            LOGGER.debug("   " + filename.toString());
            LOGGER.debug("   " + Paths.get(System.getProperty("user.dir")).resolve(filename).normalize().toString());
        }
        Assert.assertTrue(result.getExitCode() == 0);
        LOGGER.debug(" **************************  Test 01a - git status short finished  *******************");
    }

    @Ignore
    @Test
    public void test01bGitStatusShort() {
        LOGGER.debug(" **************************  Test 01b - git status short started  ********************");
        LOGGER.debug(" **************************         specific directory      **************************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug(" Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug(" Repository path: " + repository.toString());
        GitStatusShortCommandResult result = (GitStatusShortCommandResult)GitCommand.executeGitStatusShort(repository, workingDir);
        LOGGER.debug(" command: " + result.getCommand());
        LOGGER.debug(" ExitCode: " + result.getExitCode());
        LOGGER.debug(" StdOut:\n" + result.getStdOut());
        LOGGER.debug(" StdOut parsed - results:");
        LOGGER.debug(" File(s) marked as modified:");
        for (Path filename : result.getModified()) {
            LOGGER.debug("\tfilename:\t" + filename.toString());
            LOGGER.debug("\tfull path:\t" + repository.resolve(filename).normalize().toString());
        }
        LOGGER.debug("File(s) marked as added:");
        for(Path filename : result.getAdded()) {
            LOGGER.debug("\tfilename:\t" + filename.toString());
            LOGGER.debug("\tfull path:\t" + repository.resolve(filename).normalize().toString());
        }
        LOGGER.debug("**************************  Test 01b - git status short finished  *******************");
    }

    @Ignore
    @Test
    public void test02GitPull() {
        LOGGER.debug("**************************  Test 02 - git pull started  *****************************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug("Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());
        GitPullCommandResult result = (GitPullCommandResult)GitCommand.executeGitPull(repository, workingDir);
        LOGGER.debug("command: " + result.getCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("StdOut parsed - results:");
        LOGGER.debug("\tpulled changes:\t\t" + result.getChangesCount());
        LOGGER.debug("\tpulled insertions:\t" + result.getInsertionsCount());
        LOGGER.debug("\tpulled deletions:\t" + result.getDeletionsCount());
        LOGGER.debug("**************************  Test 02 - git pull finished  ****************************");
    }
    
    @Ignore
    @Test
    public void test03GitAddAll() {
        LOGGER.debug("**************************  Test 03 - git add all started  **************************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug("Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());
        GitCommandResult result = GitCommand.executeGitAddAll(repository, workingDir);
        LOGGER.debug("command: " + result.getCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("**************************  Test 03 - git add all finished  *************************");
    }
    
    @Ignore
    @Test
    public void test04GitRestoreStagedSuccessful() {
        LOGGER.debug("**************************  Test 04 - git restore --staged started  *****************");
        LOGGER.debug("**************************                 successful               *****************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug("Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());
        GitCommandResult result = GitCommand.executeGitRestore(repository, workingDir);
        LOGGER.debug("command: " + result.getCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("**************************  Test 04 - git restore --staged finished  ****************");
    }
    
    @Test
    public void test05aGitRestoreStagedFailed() {
        LOGGER.debug("**************************  Test 05a - git restore --staged started  ****************");
        LOGGER.debug("**************************                   failed                 *****************");
        LOGGER.debug("**************************         current directory      ***************************");
        GitCommandResult result = GitCommand.executeGitRestore();
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdErr: " + result.getStdErr());
        LOGGER.debug("error: " + result.getError());
        Assert.assertTrue(result.getExitCode() != 0);
        Assert.assertTrue(!result.getStdErr().isEmpty());
        LOGGER.debug("**************************  Test 05a - git restore --staged finished  ***************");
    }
    
    @Ignore
    @Test
    public void test05bGitRestoreStagedFailed() {
        LOGGER.debug("**************************  Test 05a - git restore --staged started  ****************");
        LOGGER.debug("**************************                   failed                 *****************");
        LOGGER.debug("**************************         specific directory      **************************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug("Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());
        GitCommandResult result = GitCommand.executeGitRestore(repository, workingDir);
        LOGGER.debug("command: " + result.getCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdErr: " + result.getStdErr());
        LOGGER.debug("error: " + result.getError());
        LOGGER.debug("**************************  Test 05a - git restore --staged finished  ***************");
    }
    
    @Test
    public void test06aGitRemoteV() {
        LOGGER.debug("**************************  Test 06a - git remote started  **************************");
        LOGGER.debug("**************************         current directory       **************************");

        GitRemoteCommandResult result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead();
        LOGGER.debug("command: " + result.getCommand());
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
        LOGGER.debug("**************************  Test 06a - git remote finished  *************************");
    }

    @Ignore
    @Test
    public void test06bGitRemoteV() {
        LOGGER.debug("**************************  Test 06b - git remote started  **************************");
        LOGGER.debug("**************************         specific directory      **************************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug("Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());

        GitRemoteCommandResult result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead(repository, workingDir);
        LOGGER.debug("command: " + result.getCommand());
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
        LOGGER.debug("**************************  Test 06b - git remote finished  *************************");
    }

    @Test
    public void test07aGitLog() {
        LOGGER.debug("**************************  Test 07a - git log started     **************************");
        LOGGER.debug("**************************         current directory       **************************");

        GitLogCommandResult result = (GitLogCommandResult)GitCommand.executeGitLogParseable();
        LOGGER.debug("command: " + result.getCommand());
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
        LOGGER.debug("**************************  Test 07a - git log finished  ****************************");
    }

    @Ignore
    @Test
    public void test07bGitLog() {
        LOGGER.debug("**************************  Test 07b - git log started  *****************************");
        LOGGER.debug("**************************      specific directory      *****************************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug("Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());

        GitLogCommandResult result = (GitLogCommandResult)GitCommand.executeGitLogParseable(repository, workingDir);
        LOGGER.debug("command: " + result.getCommand());
        LOGGER.debug("ExitCode: " + result.getExitCode());
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("StdOut parsed - results:");
        result.getCommits().forEach((commitHash, message) -> LOGGER.debug("commitHash: " + commitHash + " message: " + message));
        LOGGER.debug("commit count: " + result.getCommits().keySet().size());
        Assert.assertTrue(!result.getCommits().isEmpty());
        LOGGER.debug("**************************  Test 07b - git log finished  ****************************");
    }

    @Ignore
    @Test
    public void test08GitRemoteAddUpdateRemoveChain() {
        LOGGER.debug("**************************  Test 08 - git remote started  ***************************");
        // get working dir
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        LOGGER.debug("Working Directory: " + workingDir.toString());
        Path repository = Paths.get("C:/sp/devel/js7/testing/git/local_repos/sp");
        LOGGER.debug("Repository path: " + repository.toString());
        LOGGER.debug("**************************  Step 1: read current state     **************************");
        GitRemoteCommandResult result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead(repository, workingDir);
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
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteAdd(shortName, remoteUri, repository, workingDir);
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("**************************  Step 3: read state again       **************************");
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRead(repository, workingDir);
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
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteUpdate(repository, workingDir);
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
        result = (GitRemoteCommandResult)GitCommand.executeGitRemoteRemove(shortName, repository, workingDir);
        LOGGER.debug("StdOut:\n" + result.getStdOut());
        LOGGER.debug("**************************  Step 6: cleanup                **************************");
        LOGGER.debug("cleanup detached but still fetched tags from removed repo");
        String cdTo = "cd " + repository.toString();
        String cdBack = "cd " + workingDir.toString();
        SOSCommandResult r = SOSShell.executeCommand(cdTo + " && FOR /f \"tokens=*\" %a in ('git tag') DO git tag -d %a");
        SOSShell.executeCommand(cdBack);
        Assert.assertTrue(r.getExitCode() == 0);
        LOGGER.debug("**************************  Test 08 - git remote finished  **************************");
    }

}
