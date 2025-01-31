package com.sos.yade.engine.delegators;

import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;

public class YADEProviderFile extends ProviderFile {

    private Steady steady = null;
    private TransferEntryState state = null;
    private int index;// in the list

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

    public boolean transferred() {// succeeded?
        return TransferEntryState.TRANSFERRED.equals(state);
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
