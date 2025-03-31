package com.sos.yade.engine.commons;

import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;

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

    private String integrityHash;

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

    public void setSubState(TransferEntryState val) {
        subState = val;
    }

    public TransferEntryState getSubState() {
        return subState;
    }

    public void setIntegrityHash(String val) {
        integrityHash = val;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (state != null) {
            sb.append("state=").append(state.name().toLowerCase());
            sb.append(",");
        }
        if (subState != null) {
            sb.append("subState=").append(subState.name().toLowerCase());
            sb.append(",");
        }
        if (integrityHash != null) {
            sb.append("integrityHash=").append(integrityHash);
            sb.append(",");
        }
        sb.append(getFinalFullPath());
        if (target != null) {
            sb.append(", Target state=").append(target.getState().name().toLowerCase());
            if (target.getSubState() != null) {
                sb.append(",subState=").append(target.getSubState().name().toLowerCase());
            }
            if (target.getIntegrityHash() != null) {
                sb.append("integrityHash=").append(target.getIntegrityHash());
                sb.append(",");
            }
            sb.append(",").append(target.getFinalFullPath());

        }
        return sb.toString();
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
