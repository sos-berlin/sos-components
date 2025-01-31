package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;

public class YADEProviderFile extends ProviderFile {

    private Steady steady = null;
    private TransferEntryState state = null;
    private int index;// in the list

    private String newFullPath;
    private boolean newFullPathIsCurrentFullPath;

    private YADEProviderFile target;

    public YADEProviderFile(String fullPath, long size, long lastModifiedMillis, boolean checkSteady) {
        super(fullPath, size, lastModifiedMillis);
        if (checkSteady) {
            steady = new Steady(getSize());
        }
    }

    public Steady getSteady() {
        return steady;
    }

    public void resetSteady() {
        steady = null;
    }

    private TransferEntryState getState() {
        return state;
    }

    public static TransferEntryState getState(ProviderFile file) {
        return ((YADEProviderFile) file).getState();
    }

    private void setState(TransferEntryState val) {
        state = val;
    }

    public static void setState(ProviderFile file, TransferEntryState state) {
        ((YADEProviderFile) file).setState(state);
    }

    public boolean isTransferred() {// succeeded?
        return TransferEntryState.TRANSFERRED.equals(state);
    }

    private boolean isSkipped() {
        return TransferEntryState.SKIPPED.equals(state);
    }

    public static boolean isSkipped(ProviderFile file) {
        return ((YADEProviderFile) file).isSkipped();
    }

    private void setIndex(int val) {
        index = val;
    }

    public static void setIndex(ProviderFile file, int index) {
        ((YADEProviderFile) file).setIndex(index);
    }

    private int getIndex() {
        return index;
    }

    public static int getIndex(ProviderFile file) {
        return ((YADEProviderFile) file).getIndex();
    }

    public void setNewFullPath(String val) {
        newFullPath = val;
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

    public YADEProviderFile getTarget() {
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
