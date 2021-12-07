package com.sos.joc.classes.common;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.model.common.Folder;

public class FolderPathTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderPathTest.class);

    @Ignore
    @Test
    public void testFilterByUniquePath() {
        Set<Folder> folders = new HashSet<>();
        folders.add(newFolder("/test", true));
        folders.add(newFolder("/testa", true));
        folders.add(newFolder("/a", true));
        folders.add(newFolder("/test/a", true));
        folders.add(newFolder("/test/ab", true));
        // folders.add(newFolder("/", true));

        Set<Folder> foldersResult = FolderPath.filterByUniqueFolder(folders);
        if (foldersResult != null) {
            foldersResult.forEach(f -> {
                LOGGER.info("folder=" + f.getFolder());
            });
        }

        Set<String> singles = new HashSet<>();
        singles.add("/test/abc");
        singles.add("/testadc");

        Set<String> singlesResult = FolderPath.filterByFolders(foldersResult, singles);
        if (singlesResult != null) {
            singlesResult.forEach(p -> {
                LOGGER.info("single=" + p);
            });
        }
    }

    private static Folder newFolder(String path, boolean recursive) {
        Folder f = new Folder();
        f.setFolder(path);
        f.setRecursive(recursive);
        return f;
    }
}
