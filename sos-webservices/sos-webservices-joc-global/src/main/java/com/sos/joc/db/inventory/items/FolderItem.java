package com.sos.joc.db.inventory.items;

import com.sos.joc.model.tree.Tree;

public class FolderItem extends Tree {

    public FolderItem() {
        //
    }
    
    public FolderItem(String path, Boolean deleted) {
        setPath(path);
        setDeleted(deleted);
        setFolders(null);
        setPermitted(null);
        setRepoControlled(path, null);
    }
    
    public FolderItem(String path, Boolean deleted, Boolean repoControlled) {
        setPath(path);
        setDeleted(deleted);
        setFolders(null);
        setPermitted(null);
        setRepoControlled(path, repoControlled);
    }
    
    private void setRepoControlled(String path, Boolean repoControlled) {
        if (path.length() <= 1 || path.substring(1).contains("/")) {
            setRepoControlled(null);
        } else {
            setRepoControlled(repoControlled == Boolean.TRUE);
        }
    }
}
