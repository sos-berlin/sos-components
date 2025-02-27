package com.sos.yade.engine.delegators;

import java.security.MessageDigest;
import java.util.Optional;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.helpers.YADEReplacementHelper;

public class YADEProviderFile extends ProviderFile {

    private Steady steady = null;
    private TransferEntryState state = null;
    /** parent directory without trailing separator */
    private String parentFullPath;

    /** after possible rename etc. */
    private String finalFullPath;

    private YADETargetProviderFile target;

    private String checksum;

    private TransferEntryState subState;

    public YADEProviderFile(IProvider provider, String fullPath, long size, long lastModifiedMillis, YADEDirectoryMapper directoryMapper,
            boolean checkSteady) {
        super(provider, fullPath, size, lastModifiedMillis);
        parentFullPath = SOSPathUtil.getParentPath(getFullPath());
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

    /** Operations: Copy/Move(with target) */
    public void initTarget(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator) throws SOSProviderException {
        resetSteady();
        if (config.getTarget().getCumulate() != null) {
            target = null;
            return;
        }
        YADEFileNameInfo fileNameInfo = getTargetFinalFilePathInfo(config, targetDelegator);

        /** finalFileName: the final name of the file after transfer (compressed/replaced name...) */
        String finalFileName = fileNameInfo.getName();

        /** transferFileName: file name during transfer - same path as finalFileName but can contains the atomic prefix/suffix */
        String transferFileName = finalFileName;
        if (config.getTarget().getAtomic() != null) {
            transferFileName = config.getTarget().getAtomic().getPrefix() + finalFileName + config.getTarget().getAtomic().getSuffix();
        }
        String targetDirectory = sourceDelegator.getDirectoryMapper().getTargetDirectory(logger, config, targetDelegator, this, fileNameInfo);
        String transferFileFullPath = SOSPathUtil.appendPath(targetDirectory, transferFileName, targetDelegator.getProvider().getPathSeparator());
        target = new YADETargetProviderFile(targetDelegator.getProvider(), transferFileFullPath);
        if (config.getTarget().getAtomic() != null) {
            /** the final name of the file after transfer */
            target.setFinalName(finalFileName);
        }
    }

    /** Returns the final name of the file after transfer<br/>
     * May contains a path separator and have a different path than the original path if target replacement is enabled
     * 
     * @param sourceFile
     * @param config
     * @return the final name of the file after transfer */
    private YADEFileNameInfo getTargetFinalFilePathInfo(YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator) {
        // 1) Source name
        String fileName = getName();
        // 2) Compressed name
        if (config.getTarget().getCompress() != null) {
            fileName = fileName + config.getTarget().getCompress().getFileExtension();
        }
        // 3) Replaced name
        YADEFileNameInfo info = null;
        // Note: possible replacement setting is disabled when cumulative file enabled
        if (config.getTarget().isReplacementEnabled()) {
            Optional<YADEFileNameInfo> newFileNameInfo = getReplacementResultIfDifferent(targetDelegator, fileName);
            if (newFileNameInfo.isPresent()) {
                info = newFileNameInfo.get();
            }
        }
        if (info == null) {
            info = new YADEFileNameInfo(targetDelegator, fileName);
        }
        return info;
    }

    public void setFinalName(YADEFileNameInfo newNameInfo) {
        if (newNameInfo.isAbsolutePath()) {
            finalFullPath = SOSPathUtil.appendPath(newNameInfo.getParent(), newNameInfo.getName());
        } else {
            finalFullPath = parentFullPath;
            if (newNameInfo.needsParent()) {
                finalFullPath = SOSPathUtil.appendPath(finalFullPath, newNameInfo.getParent());
            }
            finalFullPath = SOSPathUtil.appendPath(finalFullPath, newNameInfo.getName());
        }
    }

    public boolean needsRename() {
        return finalFullPath != null && !finalFullPath.equalsIgnoreCase(getFullPath());
    }

    public void setFinalName(String newName) {
        finalFullPath = SOSPathUtil.appendPath(parentFullPath, newName);
    }

    public Optional<YADEFileNameInfo> getReplacementResultIfDifferent(AYADEProviderDelegator delegator) {
        return getReplacementResultIfDifferent(delegator, getName());
    }

    public Optional<YADEFileNameInfo> getReplacementResultIfDifferent(AYADEProviderDelegator delegator, String fileName) {
        return YADEReplacementHelper.getReplacementResultIfDifferent(delegator, getName(), delegator.getArgs().getReplacing().getValue(), delegator
                .getArgs().getReplacement().getValue());
    }

    public String getFinalFullPath() {
        return finalFullPath == null ? getFullPath() : finalFullPath;
    }

    public String getFinalFullPathParent() {
        return finalFullPath == null ? parentFullPath : SOSPathUtil.getParentPath(finalFullPath);
    }

    public YADETargetProviderFile getTarget() {
        return target;
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
