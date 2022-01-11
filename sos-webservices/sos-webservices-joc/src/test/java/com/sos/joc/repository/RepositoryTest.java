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
        LOGGER.info("**************************  Repository Tests started  *******************************");
        LOGGER.info("");
    }

    @AfterClass
    public static void logTestsFinished() {
        LOGGER.info("**************************  Deployment Tests finished  ******************************");
    }

    @Test
    public void test01SubPath() throws IOException, URISyntaxException {
        LOGGER.info("**************************  Test 01 - subpath started  ******************************");
        Path oldRoot = Paths.get(getClass().getResource("/joc/repositories/apple").toURI());
        Path newRoot = Paths.get(getClass().getResource("/joc/repositories/apple/aapg").toURI());
        LOGGER.info("old root Path: " + oldRoot.toString().replace('\\', '/'));
        LOGGER.info("new root Path: " + newRoot.toString().replace('\\', '/'));
        LOGGER.info("relative new root Path: " + "/" + newRoot.subpath(oldRoot.getNameCount() -1, newRoot.getNameCount()).toString().replace('\\', '/'));
        LOGGER.info("**************************  Test 01 - subpath finished  *****************************");
    }

    @Test
    public void test02relativize() throws IOException, URISyntaxException {
        LOGGER.info("**************************  Test 02 - relativize started  ***************************");
        Path oldRoot = Paths.get(getClass().getResource("/joc/repositories/apple").toURI());
        Path newRoot = Paths.get(getClass().getResource("/joc/repositories/apple/aapg").toURI());
        Path newNewRoot = Paths.get("/apple");
        LOGGER.info(newRoot.relativize(oldRoot).toString());
        LOGGER.info(oldRoot.relativize(newRoot).toString());
        try {
            LOGGER.info(newNewRoot.relativize(oldRoot).toString());
        } catch (Exception e) {
            LOGGER.info("error: " + e.getMessage());
            LOGGER.info(String.format("detail: 'this' -> %1$s", newNewRoot.toString()));
            LOGGER.info(String.format("detail: 'other' -> %1$s", oldRoot.toString()));
        }
        try {
            LOGGER.info(oldRoot.relativize(newNewRoot).toString());
        } catch (Exception e) {
            LOGGER.info("error: " + e.getMessage());
            LOGGER.info(String.format("detail: 'this' -> %1$s", oldRoot.toString()));
            LOGGER.info(String.format("detail: 'other' -> %1$s", newNewRoot.toString()));
        }
        LOGGER.info("**************************  Test 02 - relativize finished  **************************");
    }

    @Test
    public void test03readRepoRecursive() throws Exception {
        LOGGER.info("***************  Test 03 - read repository with recursion started  ******************");
        Path repositories = Paths.get(getClass().getResource("/joc/repositories").toURI());
        Path repository = Paths.get(getClass().getResource("/joc/repositories/apple").toURI());
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
        LOGGER.info(String.format("took %1$d ms", stop.getTime() - start.getTime()));
        LOGGER.info("***************  Test 03 - read repository with recursion finished  *****************");
    }

    @Test
    public void test04GetParent() throws IOException {
        LOGGER.info("**************************  Test 04 - get parent started  ***************************");
        Path path = Paths.get("/apple");
        if(path.getParent() != null) {
            LOGGER.info(String.format("path: %1$s - has parent: %2$s", path.toString(), path.getParent().toString()));
        } else {
            LOGGER.info(String.format("path: %1$s - has no parent", path.toString()));
        }
        LOGGER.info("**************************  Test 04 - get parent finished  **************************");
   }
    
}
