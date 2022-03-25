package com.sos.joc.repository;

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
        LOGGER.debug("**************************  Repository Tests started  *******************************");
        LOGGER.debug("");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.debug("**************************  Deployment Tests finished  ******************************");
    }

    @Ignore
    @Test
    public void test01SubPath() throws IOException, URISyntaxException {
        LOGGER.debug("**************************  Test 01 - subpath started  ******************************");
        Path oldRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo").toURI());
        Path newRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo/ScriptIncludes").toURI());
        LOGGER.debug("old root Path: " + oldRoot.toString().replace('\\', '/'));
        LOGGER.debug("new root Path: " + newRoot.toString().replace('\\', '/'));
        LOGGER.debug("relative new root Path: " + "/" + newRoot.subpath(oldRoot.getNameCount() -1, newRoot.getNameCount()).toString().replace('\\', '/'));
        LOGGER.debug("**************************  Test 01 - subpath finished  *****************************");
    }

    @Ignore
    @Test
    public void test02relativize() throws IOException, URISyntaxException {
        LOGGER.debug("**************************  Test 02 - relativize started  ***************************");
        Path oldRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo").toURI());
        Path newRoot = Paths.get(getClass().getResource("/joc/repositories/ProductDemo/ScriptIncludes").toURI());

        Path newNewRoot = Paths.get("/apple");
        LOGGER.debug(newRoot.relativize(oldRoot).toString());
        LOGGER.debug(oldRoot.relativize(newRoot).toString());
        try {
            LOGGER.debug(newNewRoot.relativize(oldRoot).toString());
        } catch (Exception e) {
            LOGGER.debug("error: " + e.getMessage());
            LOGGER.debug(String.format("detail: 'this' -> %1$s", newNewRoot.toString()));
            LOGGER.debug(String.format("detail: 'other' -> %1$s", oldRoot.toString()));
        }
        try {
            LOGGER.debug(oldRoot.relativize(newNewRoot).toString());
        } catch (Exception e) {
            LOGGER.debug("error: " + e.getMessage());
            LOGGER.debug(String.format("detail: 'this' -> %1$s", oldRoot.toString()));
            LOGGER.debug(String.format("detail: 'other' -> %1$s", newNewRoot.toString()));
        }
        LOGGER.debug("**************************  Test 02 - relativize finished  **************************");
    }

    @Ignore
    @Test
    public void test03readRepoRecursive() throws Exception {
        LOGGER.debug("***************  Test 03 - read repository with recursion started  ******************");
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
        LOGGER.debug(String.format("took %1$d ms", stop.getTime() - start.getTime()));
        LOGGER.debug("***************  Test 03 - read repository with recursion finished  *****************");
    }

    @Ignore
    @Test
    public void test04GetParent() throws IOException {
        LOGGER.debug("**************************  Test 04 - get parent started  ***************************");
        Path path = Paths.get("/apple");
        if(path.getParent() != null) {
            LOGGER.debug(String.format("path: %1$s - has parent: %2$s", path.toString(), path.getParent().toString()));
        } else {
            LOGGER.debug(String.format("path: %1$s - has no parent", path.toString()));
        }
        LOGGER.debug("**************************  Test 04 - get parent finished  **************************");
   }
    
//    @Ignore
    @Test
    public void test05Resolve() throws IOException, URISyntaxException {
        LOGGER.debug("**************************  Test 05 - resolve started  ******************************");
        Path repositoriesAbsolute = Paths.get(getClass().getResource("/joc/repositories").toURI());
        Path repositoriesRelative = Paths.get("/repositories");
        Path pathStartsWithSlash = Paths.get("/ProductDemo");
        Path pathNotStartsWithSlash = Paths.get("ProductDemo");
        Path oldPath = Paths.get("/aditi");
        Path newPath = Paths.get("/");
        Path oldItemPath = Paths.get("/aditi/TEST-JOC-1242/workflow");
        LOGGER.debug("old Path: " + oldPath.toString());
        LOGGER.debug("old item Path: " + oldItemPath.toString());
        LOGGER.debug("new Path: " + newPath.toString());
//        pWithoutFix.resolve(oldItemPath.relativize(oldItemPath))
        LOGGER.debug("new path relativized: " + newPath.resolve(oldPath.relativize(oldItemPath)).toString());
        LOGGER.debug("resolved path: " + oldPath.resolve(newPath).toString());        
        LOGGER.debug("absolute Path of repositories: " + repositoriesAbsolute.toString());
        LOGGER.debug("relative Path of repositories: " + repositoriesRelative.toString());
        LOGGER.debug("Sub Path starting with slash: " + pathStartsWithSlash.toString());
        LOGGER.debug("Sub Path not starting with slash: " + pathNotStartsWithSlash.toString());
        LOGGER.debug("resolved path with slash:");
        LOGGER.debug("absolute: " + repositoriesAbsolute.resolve(pathStartsWithSlash).toString());
        LOGGER.debug("relative: " + repositoriesRelative.resolve(pathStartsWithSlash).toString());
        LOGGER.debug("resolved path without slash:");
        LOGGER.debug("absolute: " + repositoriesAbsolute.resolve(pathNotStartsWithSlash).toString());
        LOGGER.debug("relative: " + repositoriesRelative.resolve(pathNotStartsWithSlash).toString());
        LOGGER.debug("**************************  Test 05 - resolve finished  *****************************");
   }
    
//    @Ignore
    @Test
    public void test06JocAndRepoPaths() throws IOException, URISyntaxException {
        LOGGER.debug("**************************  Test 06 - JOC and Repo paths started  *******************");
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

        LOGGER.debug("repositoryRolloutBaseAbsolute: " + repositoryRolloutBaseAbsolute.toString().replace('\\', '/'));
        LOGGER.debug("repositoryLocalBaseAbsolute: " + repositoryLocalBaseAbsolute.toString().replace('\\', '/'));
        LOGGER.debug("oldPathJoc: " + oldPathJoc.toString().replace('\\', '/'));
        LOGGER.debug("newPathJoc: " + newPathWithFolderJoc.toString().replace('\\', '/'));
        LOGGER.debug("pathBaseJoc: " + pathBaseJoc.toString().replace('\\', '/'));
        LOGGER.debug("oldPathRelative: " + oldPathRelative.toString().replace('\\', '/'));
        LOGGER.debug("newPathRelative: " + newPathRelative.toString().replace('\\', '/'));
        LOGGER.debug("newPathJocResolved: " + newPathJocResolved.toString().replace('\\', '/'));
        LOGGER.debug("newPathRolloutAbsolute: " + newPathRolloutAbsolute.toString().replace('\\', '/'));
        LOGGER.debug("newPathLocalAbsolute: " + newPathLocalAbsolute.toString().replace('\\', '/'));

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
        LOGGER.debug("wf1Relativized: " + wf1Relativized.toString().replace('\\', '/'));
        LOGGER.debug("wf2Relativized: " + wf2Relativized.toString().replace('\\', '/'));
        LOGGER.debug("wf3Relativized: " + wf3Relativized.toString().replace('\\', '/'));
        LOGGER.debug("wf4Relativized: " + wf4Relativized.toString().replace('\\', '/'));
        LOGGER.debug("wf5Relativized: " + wf5Relativized.toString().replace('\\', '/'));
        LOGGER.debug("wf1NewPathWithFolderResolved: " + wf1NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf2NewPathWithFolderResolved: " + wf2NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf3NewPathWithFolderResolved: " + wf3NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf4NewPathWithFolderResolved: " + wf4NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf5NewPathWithFolderResolved: " + wf5NewPathWithFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf1NewPathWithoutFolderResolved: " + wf1NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf2NewPathWithoutFolderResolved: " + wf2NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf3NewPathWithoutFolderResolved: " + wf3NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf4NewPathWithoutFolderResolved: " + wf4NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        LOGGER.debug("wf5NewPathWithoutFolderResolved: " + wf5NewPathWithoutFolderResolved.toString().replace('\\', '/'));
        
        Path oldPathJoc2 = Paths.get("/Examples.Unix");
        Path newPathWithFolderJoc2 = Paths.get("/sp/2022-03-09/Examples.Unix");
        Path newPathWithoutFolderJoc2 = Paths.get("/sp/2022-03-09");
        Path oldItemPath = Paths.get("/Examples.Unix/01_HelloWorld/jduHelloWorld");
        Path oldItemPathRelativized = oldPathJoc2.relativize(oldItemPath);
        Path newPathWithFolderJoc2Resolved = newPathWithFolderJoc2.resolve(oldItemPathRelativized);
        Path newPathWithoutFolderJoc2Resolved = newPathWithoutFolderJoc2.resolve(oldItemPathRelativized);
        LOGGER.debug("oldPathJoc 2: ", oldPathJoc2.toString().replace('\\', '/'));
        LOGGER.debug("newPathWithFolderJoc 2: ", newPathWithFolderJoc2.toString().replace('\\', '/'));
//        LOGGER.debug(": ", .toString().replace('\\', '/'));
//        LOGGER.debug(": ", .toString().replace('\\', '/'));
//        LOGGER.debug(": ", .toString().replace('\\', '/'));
//        LOGGER.debug(": ", .toString().replace('\\', '/'));
//        LOGGER.debug(": ", .toString().replace('\\', '/'));
        
        LOGGER.debug("**************************  Test 06 - JOC and Repo paths finished  ******************");
    }

    @Test
    public void test07JocAndRepoPaths() throws IOException, URISyntaxException {
        LOGGER.debug("**************************  Test 07 - JOC and Repo paths started  *******************");
        Path root = Paths.get("/");
        Path localBase = Paths.get(getClass().getResource("/joc/repositories/local").toURI());
        Path rolloutBase = Paths.get(getClass().getResource("/joc/repositories/rollout").toURI());
        Path localFolder  = Paths.get("/sp/2022-03-15/repoStoreAndDelete");
        Path rolloutFolder  = Paths.get("/sp/2022-03-15/repoStoreAndDelete");
        Path localFolderRel  = root.relativize(localFolder);
        Path rolloutFolderRel  = root.relativize(rolloutFolder);
        Path localAbs = localBase.resolve(localFolderRel);
        Path rolloutAbs = rolloutBase.resolve(rolloutFolderRel);
        LOGGER.debug("from local repository:");
        TreeSet<Path> entries = RepositoryUtil.readRepositoryAsTreeSet(localAbs);
        if(entries.contains(localAbs)) {
            entries.remove(localAbs);
        }
        for(Path path: entries) {
            Path rel = localBase.relativize(path);
            LOGGER.debug("path: " + localFolder.resolve(rel));
        }
        LOGGER.debug("from remote repository:");
        entries = RepositoryUtil.readRepositoryAsTreeSet(rolloutAbs);
        if(entries.contains(rolloutAbs)) {
            entries.remove(rolloutAbs);
        }
        for(Path path: entries) {
            Path rel = rolloutBase.relativize(path);
            LOGGER.debug("path: " + localFolder.resolve(rel));
        }
        LOGGER.debug("**************************  Test 07 - JOC and Repo paths finished  ******************");
    }

}
