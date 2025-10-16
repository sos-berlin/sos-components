package com.sos.yade.engine.commons;

import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

/** @apiNote all operations */
public class YADEProviderFile extends ProviderFile {

    private Steady steady = null;
    private YADETargetProviderFile target;

    private TransferEntryState state = null;
    private TransferEntryState subState;

    /** parent directory without trailing separator */
    private String parentFullPath;
    /** after possible rename etc. */
    private String finalFullPath;

    private String integrityHash;

    private boolean nameReplaced;

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

    // TODO TransferEntryState.SKIPPED - seems to be unused
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
        return finalFullPath != null && !finalFullPath.equalsIgnoreCase(getFullPath()) && !isSkipped();
    }

    public void setFinalFullPath(AYADEProviderDelegator delegator, String newName) {
        finalFullPath = delegator.appendPath(parentFullPath, newName);
    }

    public void setFinalFullPath(String path) {
        finalFullPath = path;
    }

    public String getFinalFullPath() {
        return finalFullPath == null ? getFullPath() : finalFullPath;
    }

    public String getFinalFullPathParent(AYADEProviderDelegator delegator) {
        return finalFullPath == null ? parentFullPath : delegator.getParentPath(finalFullPath);
    }

    public YADETargetProviderFile getTarget() {
        return target;
    }

    public void setTarget(YADETargetProviderFile val) {
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

    public boolean isNameReplaced() {
        return nameReplaced;
    }

    public void setNameReplaced(boolean val) {
        nameReplaced = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (state != null) {
            sb.append("state=").append(state.name().toLowerCase());
            sb.append(", ");
        }
        if (subState != null) {
            sb.append("subState=").append(subState.name().toLowerCase());
            sb.append(", ");
        }
        if (integrityHash != null) {
            sb.append("integrityHash=").append(integrityHash);
            sb.append(", ");
        }
        sb.append(getFinalFullPath());
        if (target != null) {
            sb.append(", Target state=").append(target.getState().name().toLowerCase());
            if (target.getSubState() != null) {
                sb.append(", subState=").append(target.getSubState().name().toLowerCase());
            }
            if (target.getIntegrityHash() != null) {
                sb.append(", integrityHash=").append(target.getIntegrityHash());
            }
            sb.append(", ").append(target.getFinalFullPath());

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
