package com.sos.joc.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.model.publish.repository.ResponseFolder;
import com.sos.joc.model.publish.repository.ResponseFolderItem;
import com.sos.joc.publish.repository.util.RepositoryUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryTest.class);
    
    @BeforeClass
    public static void logTestsStarted() {
        LOGGER.trace("**************************  Repository Tests started  *******************************");
        LOGGER.trace("");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.trace("**************************  Deployment Tests finished  ******************************");
    }

    @Ignore
    @Test
    public void test01SubPath() throws IOException, URISyntaxException {
        LOGGER.trace("**************************  Test 01 - subpath started  ******************************");
        Path oldRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo").toURI());
        Path newRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo/ScriptIncludes").toURI());
        LOGGER.trace("old root Path: " + oldRoot.toString().replace('\\', '/'));
        LOGGER.trace("new root Path: " + newRoot.toString().replace('\\', '/'));
        LOGGER.trace("relative new root Path: " + "/" + newRoot.subpath(oldRoot.getNameCount() -1, newRoot.getNameCount()).toString().replace('\\', '/'));
        LOGGER.trace("**************************  Test 01 - subpath finished  *****************************");
    }

    @Ignore
    @Test
    public void test02relativize() throws IOException, URISyntaxException {
        LOGGER.trace("**************************  Test 02 - relativize started  ***************************");
        Path oldRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo").toURI());
        Path newRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo/ScriptIncludes").toURI());

        Path newNewRoot = Paths.get("/apple");
        LOGGER.trace(newRoot.relativize(oldRoot).toString());
        LOGGER.trace(oldRoot.relativize(newRoot).toString());
        try {
            LOGGER.trace(newNewRoot.relativize(oldRoot).toString());
        } catch (Exception e) {
            LOGGER.trace("error: " + e.getMessage());
            LOGGER.trace(String.format("detail: 'this' -> %1$s", newNewRoot.toString()));
            LOGGER.trace(String.format("detail: 'other' -> %1$s", oldRoot.toString()));
        }
        try {
            LOGGER.trace(oldRoot.relativize(newNewRoot).toString());
        } catch (Exception e) {
            LOGGER.trace("error: " + e.getMessage());
            LOGGER.trace(String.format("detail: 'this' -> %1$s", oldRoot.toString()));
            LOGGER.trace(String.format("detail: 'other' -> %1$s", newNewRoot.toString()));
        }
        LOGGER.trace("**************************  Test 02 - relativize finished  **************************");
    }

    @Ignore
    @Test
    public void test03readRepoRecursive() throws Exception {
        LOGGER.trace("***************  Test 03 - read repository with recursion started  ******************");
        Path repositories = Paths.get(getClass().getResource("/joc/repositories").toURI());
        Path repository = Paths.get(getClass().getResource("/joc/repositories/ProductDemo").toURI());
        Date start = Date.from(Instant.now());
        TreeSet<java.nio.file.Path> repoTree = RepositoryUtil.readRepositoryAsTreeSet(repository);
        Set<ResponseFolderItem> responseFolderItems = repoTree.stream().filter(path -> Files.isRegularFile(path))
                .map(path -> RepositoryUtil.getResponseFolderItem(repositories, path)).collect(Collectors.toSet());
        final Map<String, Set<ResponseFolderItem>> groupedFolderItems = responseFolderItems.stream().collect(Collectors.groupingBy(
                ResponseFolderItem::getFolder, Collectors.toSet()));
        SortedSet<ResponseFolder> responseFolder = RepositoryUtil.initTreeByFolder(repository, true).stream().map(t -> {
                    ResponseFolder r = new ResponseFolder();
                    String path = Globals.normalizePath(RepositoryUtil.subPath(repositories, Paths.get(t.getPath())).toString());
                    r.setPath(path);
                    if (groupedFolderItems.containsKey(path)) {
                        r.getItems().addAll(groupedFolderItems.get(path));
                    }
                    return r;
                }).collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ResponseFolder::getPath).reversed())));
        ResponseFolder result = RepositoryUtil.getTree(responseFolder, RepositoryUtil.subPath(repositories, repository), RepositoryUtil.subPath(repositories, repositories), true);
        if (result != null && !result.getPath().isEmpty()) {
            LOGGER.trace(Globals.prettyPrintObjectMapper.writeValueAsString(result));
        }
        Date stop = Date.from(Instant.now());
        LOGGER.trace(String.format("took %1$d ms", stop.getTime() - start.getTime()));
        LOGGER.trace("***************  Test 03 - read repository with recursion finished  *****************");
    }

    @Ignore
    @Test
    public void test04GetParent() throws IOException {
        LOGGER.trace("**************************  Test 04 - get parent started  ***************************");
        Path path = Paths.get("/apple");
        if(path.getParent() != null) {
            LOGGER.trace(String.format("path: %1$s - has parent: %2$s", path.toString(), path.getParent().toString()));
        } else {
            LOGGER.trace(String.format("path: %1$s - has no parent", path.toString()));
        }
        LOGGER.trace("**************************  Test 04 - get parent finished  **************************");
   }
    
    @Ignore
    @Test
    public void test05Resolve() throws IOException, URISyntaxException {
        LOGGER.trace("**************************  Test 05 - resolve started  ******************************");
        Path repositoriesAbsolute = Paths.get(getClass().getResource("/joc/repositories").toURI());
        Path repositoriesRelative = Paths.get("/repositories");
        Path pathStartsWithSlash = Paths.get("/ProductDemo");
        Path pathNotStartsWithSlash = Paths.get("ProductDemo");
        Path oldPath = Paths.get("/aditi");
        Path newPath = Paths.get("/");
        Path oldItemPath = Paths.get("/aditi/TEST-JOC-1242/workflow");
        LOGGER.trace("old Path: " + oldPath.toString());
        LOGGER.trace("old item Path: " + oldItemPath.toString());
        LOGGER.trace("new Path: " + newPath.toString());
//        pWithoutFix.resolve(oldItemPath.relativize(oldItemPath))
        LOGGER.trace("new path relativized: " + newPath.resolve(oldPath.relativize(oldItemPath)).toString());
        LOGGER.trace("resolved path: " + oldPath.resolve(newPath).toString());        
        LOGGER.trace("absolute Path of repositories: " + repositoriesAbsolute.toString());
        LOGGER.trace("relative Path of repositories: " + repositoriesRelative.toString());
        LOGGER.trace("Sub Path starting with slash: " + pathStartsWithSlash.toString());
        LOGGER.trace("Sub Path not starting with slash: " + pathNotStartsWithSlash.toString());
        LOGGER.trace("resolved path with slash:");
        LOGGER.trace("absolute: " + repositoriesAbsolute.resolve(pathStartsWithSlash).toString());
        LOGGER.trace("relative: " + repositoriesRelative.resolve(pathStartsWithSlash).toString());
        LOGGER.trace("resolved path without slash:");
        LOGGER.trace("absolute: " + repositoriesAbsolute.resolve(pathNotStartsWithSlash).toString());
        LOGGER.trace("relative: " + repositoriesRelative.resolve(pathNotStartsWithSlash).toString());
        LOGGER.trace("**************************  Test 05 - resolve finished  *****************************");
   }
    
//    @Ignore
    @Test
    public void test06JocAndRepoPaths() throws IOException, URISyntaxException {
        LOGGER.trace("**************************  Test 06 - JOC and Repo paths started  *******************");
        Path repositoryRolloutBaseAbsolute = Paths.get("C:/ProgramData/sos-berlin.com/js7/joc/jetty_base/resources/joc/repositories/rollout");
        Path repositoryLocalBaseAbsolute = Paths.get("C:/ProgramData/sos-berlin.com/js7/joc/jetty_base/resources/joc/repositories/local");
        Path oldPathJoc = Paths.get("/aditi");
        Path newPathWithFolderJoc = Paths.get("/sp/aditi");
        Path newPathWithoutFolderJoc = Paths.get("/sp");
        Path pathBaseJoc = Paths.get("/");
        Path oldPathRelative = pathBaseJoc.relativize(oldPathJoc);
        Path newPathRelative = pathBaseJoc.relativize(newPathWithFolderJoc);
        Path newPathJocResolved = pathBaseJoc.resolve(newPathRelative);
        Path newPathRolloutAbsolute = repositoryRolloutBaseAbsolute.resolve(newPathRelative);
        Path newPathLocalAbsolute = repositoryLocalBaseAbsolute.resolve(newPathRelative);

        LOGGER.trace("repositoryRolloutBaseAbsolute: " + repositoryRolloutBaseAbsolute.toString().replace('\\', '/'));
        LOGGER.trace("repositoryLocalBaseAbsolute: " + repositoryLocalBaseAbsolute.toString().replace('\\', '/'));
        LOGGER.trace("oldPathJoc: " + oldPathJoc.toString().replace('\\', '/'));
        LOGGER.trace("newPathJoc: " + newPathWithFolderJoc.toString().replace('\\', '/'));
        LOGGER.trace("pathBaseJoc: " + pathBaseJoc.toString().replace('\\', '/'));
        LOGGER.trace("oldPathRelative: " + oldPathRelative.toString().replace('\\', '/'));
        LOGGER.trace("newPathRelative: " + newPathRelative.toString().replace('\\', '/'));
        LOGGER.trace("newPathJocResolved: " + newPathJocResolved.toString().replace('\\', '/'));
        LOGGER.trace("newPathRolloutAbsolute: " + newPathRolloutAbsolute.toString().replace('\\', '/'));
        LOGGER.trace("newPathLocalAbsolute: " + newPathLocalAbsolute.toString().replace('\\', '/'));

        Path wf1 = Paths.get("/aditi/TEST-JOC-1242/a-aditi");
        Path wf2 = Paths.get("/aditi/TEST-JOC-1242/b-aditi");
        Path wf3 = Paths.get("/aditi/TEST-JOC-1242/c-aditi");
        Path wf4 = Paths.get("/aditi/TEST-JOC-1242/d-aditi");
        Path wf5 = Paths.get("/aditi/TEST-JOC-1242/e-aditi");
        Path wf1Relativized = oldPathJoc.relativize(wf1);
        Path wf2Relativized = oldPathJoc.relativize(wf2);
        Path wf3Relativized = oldPathJoc.relativize(wf3);
        Path wf4Relativized = oldPathJoc.relativize(wf4);
        Path wf5Relativized = oldPathJoc.relativize(wf5);
        // withFolder
        Path wf1NewPathWithFolderResolved = newPathWithFolderJoc.resolve(wf1Relativized);
        Path wf2NewPathWithFolderResolved = newPathWithFolderJoc.resolve(wf2Relativized);
        Path wf3NewPathWithFolderResolved = newPathWithFolderJoc.resolve(wf3Relativized);
        Path wf4NewPathWithFolderResolved = newPathWithFolderJoc.resolve(wf4Relativized);
        Path wf5NewPathWithFolderResolved = newPathWithFolderJoc.resolve(wf5Relativized);
        // withoutFolder
        Path wf1NewPathWithoutFolderResolved = newPathWithoutFolderJoc.resolve(wf1Relativized);
        Path wf2NewPathWithoutFolderResolved = newPathWithoutFolderJoc.resolve(wf2Relativized);
        Path wf3NewPathWithoutFolderResolved = newPathWithoutFolderJoc.resolve(wf3Relativized);
        Path wf4NewPathWithoutFolderResolved = newPathWithoutFolderJoc.resolve(wf4Relativized);
        Path wf5NewPathWithoutFolderResolved = newPathWithoutFolderJoc.resolve(wf5Relativized);
        LOGGER.trace("wf1Relativized: " + wf1Relativized.toString().replace('\\', '/'));
        LOGGER.trace("wf2Relativized: " + wf2Relativized.toString().replace('\\', '/'));
        LOGGER.trace("wf3Relativized: " + wf3Relativized.toString().replace('\\', '/'));
        LOGGER.trace("wf4Relativized: " + wf4Relativized.toString().replace('\\', '/'));
        LOGGER.trace("wf5Relativized: " + wf5Relativized.toString().replace('\\', '/'));
        LOGGER.trace("wf1NewPathWithFolderResolved: " + wf1NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf2NewPathWithFolderResolved: " + wf2NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf3NewPathWithFolderResolved: " + wf3NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf4NewPathWithFolderResolved: " + wf4NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf5NewPathWithFolderResolved: " + wf5NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf1NewPathWithoutFolderResolved: " + wf1NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf2NewPathWithoutFolderResolved: " + wf2NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf3NewPathWithoutFolderResolved: " + wf3NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf4NewPathWithoutFolderResolved: " + wf4NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.trace("wf5NewPathWithoutFolderResolved: " + wf5NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        
        Path oldPathJoc2 = Paths.get("/Examples.Unix");
        Path newPathWithFolderJoc2 = Paths.get("/sp/2022-03-09/Examples.Unix");
        Path newPathWithoutFolderJoc2 = Paths.get("/sp/2022-03-09");
        Path oldItemPath = Paths.get("/Examples.Unix/01_HelloWorld/jduHelloWorld");
        Path oldItemPathRelativized = oldPathJoc2.relativize(oldItemPath);
        Path newPathWithFolderJoc2Resolved = newPathWithFolderJoc2.resolve(oldItemPathRelativized);
        Path newPathWithoutFolderJoc2Resolved = newPathWithoutFolderJoc2.resolve(oldItemPathRelativized);
        LOGGER.trace("oldPathJoc 2: ", oldPathJoc2.toString().replace('\\', '/'));
        LOGGER.trace("newPathWithFolderJoc 2: ", newPathWithFolderJoc2.toString().replace('\\', '/'));
//        LOGGER.trace(": ", .toString().replace('\\', '/'));
//        LOGGER.trace(": ", .toString().replace('\\', '/'));
//        LOGGER.trace(": ", .toString().replace('\\', '/'));
//        LOGGER.trace(": ", .toString().replace('\\', '/'));
//        LOGGER.trace(": ", .toString().replace('\\', '/'));
        
        LOGGER.trace("**************************  Test 06 - JOC and Repo paths finished  ******************");
    }

    @Ignore
    @Test
    public void test07JocAndRepoPaths() throws IOException, URISyntaxException {
        LOGGER.trace("**************************  Test 07 - JOC and Repo paths started  *******************");
        Path root = Paths.get("/");
        Path localBase = Paths.get(getClass().getResource("/joc/repositories/local").toURI());
        Path rolloutBase = Paths.get(getClass().getResource("/joc/repositories/rollout").toURI());
        Path localFolder  = Paths.get("/sp/2022-03-15/repoStoreAndDelete");
        Path rolloutFolder  = Paths.get("/sp/2022-03-15/repoStoreAndDelete");
        Path localFolderRel  = root.relativize(localFolder);
        Path rolloutFolderRel  = root.relativize(rolloutFolder);
        Path localAbs = localBase.resolve(localFolderRel);
        Path rolloutAbs = rolloutBase.resolve(rolloutFolderRel);
        LOGGER.trace("from local repository:");
        TreeSet<Path> entries = RepositoryUtil.readRepositoryAsTreeSet(localAbs);
        if(entries.contains(localAbs)) {
            entries.remove(localAbs);
        }
        for(Path path: entries) {
            Path rel = localBase.relativize(path);
            LOGGER.trace("path: " + localFolder.resolve(rel));
        }
        LOGGER.trace("from remote repository:");
        entries = RepositoryUtil.readRepositoryAsTreeSet(rolloutAbs);
        if(entries.contains(rolloutAbs)) {
            entries.remove(rolloutAbs);
        }
        for(Path path: entries) {
            Path rel = rolloutBase.relativize(path);
            LOGGER.trace("path: " + localFolder.resolve(rel));
        }
        LOGGER.trace("**************************  Test 07 - JOC and Repo paths finished  ******************");
    }

    @Test
    public void test08ParsePaths() throws IOException, URISyntaxException {
        String regex = "^[a-zA-Z]:?([\\\\|[^/\\\\<>?:\\\"\\n,!|*]][^/\\\\<>?:\\\"\\n,!|*]+)+\\\\?$|^([/|[^/\\\\<>?:\\\"\\n,!|*]][^/\\\\<>?:\\\"\\n,!|*]+)+$";
        Pattern pat = Pattern.compile(regex);
        // absolute path, relative path
        Path linux_path_abs = Paths.get("/some/absolute/path/test");
        Path linux_path_abs_with_spaces = Paths.get("/some/absolute/path/with spaces/test");
        Path linux_path_rel = Paths.get("some/relative/path/test");
        Path linux_path_rel_with_spaces = Paths.get("some/relative/path/with spaces/test");
        Path win_path_abs = Paths.get("C:\\some\\absolute\\path\\test");
        Path win_path_abs_with_spaces = Paths.get("C:\\some\\absolute\\path\\with spaces\\test");
        Path win_path_rel = Paths.get("some\\relative\\path\\test");
        Path win_path_rel_with_spaces = Paths.get("some\\relative\\path\\with spaces\\test");
        Matcher linAbs = pat.matcher(linux_path_abs.toString().replace('\\', '/'));
        Matcher linAbsWithSpaces = pat.matcher(linux_path_abs_with_spaces.toString().replace('\\', '/'));
        Matcher linRel = pat.matcher(linux_path_rel.toString().replace('\\', '/'));
        Matcher linRelWithSpaces = pat.matcher(linux_path_rel_with_spaces.toString().replace('\\', '/'));
        Matcher winAbs = pat.matcher(win_path_abs.toString());
        Matcher winAbsWithSpaces = pat.matcher(win_path_abs_with_spaces.toString());
        Matcher winRel = pat.matcher(win_path_rel.toString());
        Matcher winRelWithSpaces = pat.matcher(win_path_rel_with_spaces.toString());
        assertTrue(linAbs.matches());
        assertTrue(linAbsWithSpaces.matches());
        assertTrue(linRel.matches());
        assertTrue(linRelWithSpaces.matches());
        assertTrue(winAbs.matches());
        assertTrue(winAbsWithSpaces.matches());
        assertTrue(winRel.matches());
        assertTrue(winRelWithSpaces.matches());
        // filename
        Matcher filename = pat.matcher("linux_filename");
        Matcher filenameWithSpace = pat.matcher("win filename with spaces.txt");
        assertTrue(filename.matches());
        assertTrue(filenameWithSpace.matches());
        // no path
        String noPath1 = "not a path!";
        String noPath2 = "also, not a path.";
        String noPath3 = "and just another <> so path";
        Matcher not1 = pat.matcher(noPath1);
        Matcher not2 = pat.matcher(noPath2);
        Matcher not3 = pat.matcher(noPath3);
        assertFalse(not1.matches());
        assertFalse(not2.matches());
        assertFalse(not3.matches());
    }
    
}
