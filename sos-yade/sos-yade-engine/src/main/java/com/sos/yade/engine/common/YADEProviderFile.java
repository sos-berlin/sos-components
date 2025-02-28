package com.sos.yade.engine.common;

import java.security.MessageDigest;

import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.common.delegators.AYADEProviderDelegator;

/** @apiNote all operations */
public class YADEProviderFile extends ProviderFile {

    private Steady steady = null;
    private YADEProviderFile target;

    private TransferEntryState state = null;
    private TransferEntryState subState;

    /** parent directory without trailing separator */
    private String parentFullPath;
    /** after possible rename etc. */
    private String finalFullPath;
    private String checksum;

    public YADEProviderFile(AYADEProviderDelegator delegator, String fullPath, long size, long lastModifiedMillis,
            YADEDirectoryMapper directoryMapper, boolean checkSteady) {
        super(delegator.getProvider(), fullPath, size, lastModifiedMillis);
        parentFullPath = delegator.getParentPath(getFullPath());
        if (directoryMapper != null) {
            directoryMapper.addSourceFileDirectory(parentFullPath);
        }
        if (checkSteady) {
            steady = new Steady(getSize());
        }
    }

    public String getParentFullPath() {
        return parentFullPath;
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

    public boolean isTransferredOrTransferring() {// succeeded?
        return TransferEntryState.TRANSFERRED.equals(state) || TransferEntryState.TRANSFERRING.equals(state);
    }

    public boolean isSkipped() {
        return TransferEntryState.SKIPPED.equals(state) || TransferEntryState.NOT_OVERWRITTEN.equals(state);
    }

    public void resetSteady() {
        this.steady = null;
    }

    public void resetTarget() {
        target = null;
    }

    public void resetChecksum() {
        checksum = null;
        if (target != null) {
            target.resetChecksum();
        }
    }

    public boolean needsRename() {
        return finalFullPath != null && !finalFullPath.equalsIgnoreCase(getFullPath());
    }

    public void setFinalFullPath(AYADEProviderDelegator delegator, String newName) {
        finalFullPath = delegator.appendPath(parentFullPath, newName);
    }

    public String getFinalFullPath() {
        return finalFullPath == null ? getFullPath() : finalFullPath;
    }

    public String getFinalFullPathParent(AYADEProviderDelegator delegator) {
        return finalFullPath == null ? parentFullPath : delegator.getParentPath(finalFullPath);
    }

    public YADEProviderFile getTarget() {
        return target;
    }

    public void setTarget(YADEProviderFile val) {
        target = val;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String val) {
        checksum = val;
    }

    public void setSubState(TransferEntryState val) {
        subState = val;
    }

    public TransferEntryState getSubState() {
        return subState;
    }

    public void setChecksum(MessageDigest digest) {
        if (digest == null) {
            checksum = null;
            return;
        }
        // byte[] toHexString
        byte[] b = digest.digest();
        char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        int length = b.length * 2;
        StringBuilder sb = new StringBuilder(length);
        for (byte element : b) {
            sb.append(hexChar[(element & 0xf0) >>> 4]);
            sb.append(hexChar[element & 0x0f]);
        }
        checksum = sb.toString();
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
