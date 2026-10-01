package com.sos.auth.classes;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.auth.records.AuthFolders;
import com.sos.joc.model.common.Folder;


public class FolderPermissionsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testGetPermittedFolders() {
        Set<Folder> allow = Set.of(newFolder("/a/b", true), newFolder("/a/b/c", false), newFolder("/a/d", false));
        Set<Folder> deny = Set.of(newFolder("/a/b/d", true));
        Set<Folder> request = Set.of(newFolder("/a/b/f", true));
        AuthFolders af = new AuthFolders(Optional.of(allow), Optional.of(deny));
        AuthFolders newRequest = SOSAuthDetailedFolderPermissions.getPermittedFolders(request, af);
        System.out.print("allow: ");
        newRequest.allow().ifPresent(System.out::print);
        System.out.println("");
        System.out.print("deny: ");
        newRequest.deny().ifPresent(System.out::print);
    }
    
    private Folder newFolder(String folder, boolean recursive) {
        Folder f = new Folder();
        f.setFolder(folder);
        f.setRecursive(recursive);
        return f;
    }

}
