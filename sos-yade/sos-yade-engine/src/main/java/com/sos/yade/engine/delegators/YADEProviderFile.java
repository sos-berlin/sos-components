package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.handlers.operations.YADECopyOrMoveOperationTargetFilesConfig;

public class YADEProviderFile extends ProviderFile {

    private Steady steady = null;
    private TransferEntryState state = null;
    private int index;// in the list

    private String newFullPath; // after possible rename etc
    private boolean newFullPathIsCurrentFullPath;

    private YADETargetProviderFile target;

    public YADEProviderFile(String fullPath, long size, long lastModifiedMillis, boolean checkSteady) {
        super(fullPath, size, lastModifiedMillis);
        if (checkSteady) {
            steady = new Steady(getSize());
        }
    }

    public Steady getSteady() {
        return steady;
    }

    public TransferEntryState getState() {
        return state;
    }

    public void setState(TransferEntryState val) {
        state = val;
    }

    public boolean isTransferred() {// succeeded?
        return TransferEntryState.TRANSFERRED.equals(state);
    }

    public boolean isSkipped() {
        return TransferEntryState.SKIPPED.equals(state);
    }

    public void initForOperation(int index) {
        this.index = index;
        this.steady = null;
    }

    public void initForOperation(int index, YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            YADECopyOrMoveOperationTargetFilesConfig targetFilesConfig) {
        initForOperation(index);
        target = targetDelegator.newYADETargetProviderFile(sourceDelegator, this, targetFilesConfig);
    }

    public int getIndex() {
        return index;
    }

    public void setNewName(String newName) {
        int index = getFullPath().lastIndexOf(getName());
        newFullPath = getFullPath().substring(0, index) + newName;
    }

    public String getNewFullPath() {
        return newFullPath;
    }

    public String getCurrentFullPath() {
        return newFullPathIsCurrentFullPath ? newFullPath : getFullPath();
    }

    public void confirmFullPathChange() {
        newFullPathIsCurrentFullPath = true;
    }

    public boolean isFullPathChanged() {
        return newFullPathIsCurrentFullPath;
    }

    public YADETargetProviderFile getTarget() {
        return target;
    }

    public class Steady {

        private long lastCheckedSize;
        private boolean steady;

        private Steady(long size) {
            lastCheckedSize = size;
        }

        public void checkIfSteady() {
            if (steady) {
                return;
            }
            if (lastCheckedSize == getSize()) {
                steady = true;
            } else {
                lastCheckedSize = getSize();
            }
        }

        public boolean isSteady() {
            return steady;
        }

    }

}
