package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

public class YADERetryFileHelper {

    private final boolean retryEnabled;

    /** AppendFiles/CumulateFiles */
    private boolean appendEnabled;

    private boolean resumeEnabled;
    private long targetSizeBeforeTransfer;
    private boolean beforeTransferExecuted;

    public YADERetryFileHelper(boolean retryEnabled) {
        this.retryEnabled = retryEnabled;
        this.beforeTransferExecuted = !retryEnabled;
    }

    public void beforeTransfer(ISOSLogger logger, YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile,
            boolean appendEnabled, boolean resumeEnabled) throws Exception {
        if (beforeTransferExecuted) {
            return;
        }

        this.appendEnabled = appendEnabled;
        this.resumeEnabled = resumeEnabled;
        this.targetSizeBeforeTransfer = 0L;
        if (this.appendEnabled) {
            ProviderFile f = targetDelegator.getProvider().rereadFileIfExists(targetFile);
            if (f != null) {
                this.targetSizeBeforeTransfer = f.getSize();
            }
            if (logger.isDebugEnabled()) {
                String msg = f == null ? "not found" : "Bytes=" + targetSizeBeforeTransfer;
                logger.debug(String.format("  [%s][%s][beforeTransfer]%s", targetDelegator.getLabel(), targetFile.getFullPath(), msg));
            }
        }
        beforeTransferExecuted = true;
    }

    public boolean isBeforeTransferExecuted() {
        return beforeTransferExecuted;
    }

    public YADETargetProviderFile afterTransfer(ISOSLogger logger, YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile,
            boolean compressTarget) throws Exception {
        if (!retryEnabled) {
            return targetFile;
        }
        if (appendEnabled || compressTarget) {
            return targetFile;
        }
        ProviderFile f = targetDelegator.getProvider().rereadFileIfExists(targetFile);
        if (f != null) {
            targetFile.setBytesProcessed(f.getSize()); // if not append/cumulate
        }
        if (logger.isDebugEnabled()) {
            String msg = f == null ? "not found" : "Bytes=" + targetFile.getBytesProcessed();
            logger.debug(String.format("  [%s][%s][afterTransfer]%s", targetDelegator.getLabel(), targetFile.getFullPath(), msg));
        }
        return targetFile;
    }

    public Result onRetry(ISOSLogger logger, String fileTransferLogPrefix, int maxAttempts, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile, boolean useCumulativeTargetFile) throws Exception {

        // 1) no retry defined - skip
        if (!retryEnabled) {
            return Result.skip(targetFile.getBytesProcessed());
        }

        // 2) "normal" transfer - without append, cumulate and resume
        // - Restart transfer from offset=0
        if (!appendEnabled && !resumeEnabled) {
            logger.info(getRetryMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, null, targetFile
                    .getBytesProcessed() + "", "restart from offset 0"));

            return Result.restart();
        }

        ProviderFile rereadTarget = targetDelegator.getProvider().rereadFileIfExists(targetFile);

        // 3) Target not exists (includes cases: append, cumulate and with/without resume)
        // - Restart transfer from offset=0
        if (rereadTarget == null) {
            if (resumeEnabled) {
                if (appendEnabled) {
                    logger.info(getResumeAppendFilesMessage(useCumulativeTargetFile, fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator,
                            targetFile, 0L, "restart from offset 0 (Target not found) ..."));
                } else {
                    logger.info(getResumeMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, 0L,
                            "restart from offset 0 (Target not found) ..."));
                }
            } else {
                if (appendEnabled) {
                    logger.info(getAppendFilesRetryMsg(useCumulativeTargetFile, fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator,
                            targetFile, 0L, "restart from offset 0 (Target not found) ..."));
                }
            }
            return Result.restart();
        }

        long sourceSize = sourceFile.getSize();
        long rereadTargetSize = rereadTarget.getSize();

        // 4) includes cases: append, cumulate and with/without resume
        // - "Resume" transfer from offset: <diff>
        if (appendEnabled) {
            long diff = rereadTargetSize - targetSizeBeforeTransfer;
            if (diff < 0) {
                throw new Exception("[" + getAppendFilesArgName(useCumulativeTargetFile, targetDelegator) + "] " + "Target Bytes=" + rereadTargetSize
                        + " less than before Retry(Bytes=" + targetSizeBeforeTransfer + ")");
            }
            if (resumeEnabled) {
                logger.info(getResumeAppendFilesMessage(useCumulativeTargetFile, fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator,
                        targetFile, diff, "resume at offset " + diff + " ..."));
            } else {
                logger.info(getAppendFilesRetryMsg(useCumulativeTargetFile, fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator,
                        targetFile, diff, "resume at offset " + diff + " ..."));
            }

            if (diff != targetFile.getBytesProcessed()) {
                // TODO - hash?
            }
            return Result.resume(diff);
        }

        // 5) "normal" transfer resume (without append, cumulate)
        if (resumeEnabled) {
            // already transferred completelly
            if (rereadTargetSize == sourceSize) {
                // extra check targetFile.getBytesProcessed() - because it can be an old file, which should be overwritten by the current transfer
                if (rereadTargetSize == targetFile.getBytesProcessed()) {
                    logger.info(getResumeMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, rereadTargetSize,
                            "no resume required (Source and Target sizes match: " + rereadTargetSize + " Bytes)"));
                    return Result.completed(rereadTargetSize);
                } else {
                    logger.info(getResumeMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, 0L,
                            "restart from offset 0"));
                    return Result.restart();
                }
            }
            // size mismatch
            else if (rereadTargetSize > sourceSize) {
                logger.info(getResumeMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, rereadTargetSize,
                        "Target Bytes greater than Source Bytes (size mismatch), restart from offset 0"));
                return Result.restart();
            } else if (rereadTargetSize < sourceSize) {
                if (rereadTargetSize == 0) {
                    logger.info(getResumeMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, rereadTargetSize,
                            "restart from offset 0"));
                    return Result.restart();
                }
                // else resume at rereadTargetSize
            }

            logger.info(getResumeMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, rereadTargetSize,
                    "resume at offset " + rereadTargetSize + " ..."));
            return Result.resume(rereadTargetSize);
        }

        return Result.restart();
    }

    private String getAppendFilesRetryMsg(boolean useCumulativeTargetFile, String fileTransferLogPrefix, int maxAttempts, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile, long bytesProcessed, String add) {
        return getRetryMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, getAppendFilesArgName(
                useCumulativeTargetFile, targetDelegator), bytesProcessed + "", add);
    }

    private String getResumeAppendFilesMessage(boolean useCumulativeTargetFile, String fileTransferLogPrefix, int maxAttempts,
            YADEProviderFile sourceFile, YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile, long bytesProcessed,
            String add) {
        return getRetryMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, targetDelegator.getArgs().getResumeFiles()
                .getName() + "][" + getAppendFilesArgName(useCumulativeTargetFile, targetDelegator), bytesProcessed + "", add);
    }

    private String getResumeMessage(String fileTransferLogPrefix, int maxAttempts, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile, long bytesProcessed, String add) {
        return getRetryMessage(fileTransferLogPrefix, maxAttempts, sourceFile, targetDelegator, targetFile, targetDelegator.getArgs().getResumeFiles()
                .getName(), bytesProcessed + "", add);
    }

    public static String getRetryMessage(String fileTransferLogPrefix, int maxAttempts, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile, String argName, String bytesProcessed, String add) {
        String arg = argName == null ? "" : "[" + argName + "]";
        return String.format("[%s][%s][Retry %s/%s]%s[%s][%s][Bytes(processed)=%s/%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(
                targetFile.getState()), targetFile.getAttempt(), maxAttempts, arg, targetDelegator.getLabel(), targetFile.getFullPath(),
                bytesProcessed, sourceFile.getSize(), add);
    }

    private String getAppendFilesArgName(boolean useCumulativeTargetFile, YADETargetProviderDelegator targetDelegator) {
        return useCumulativeTargetFile ? "CumulateFiles" : targetDelegator.getArgs().getAppendFiles().getName();
    }

    public static final class Result {

        public static enum Type {
            RESTART, RESUME, COMPLETED, SKIPPED
        }

        final Type type;
        final long offset;

        private Result(Type type, long offset) {
            this.type = type;
            this.offset = offset;
        }

        static Result restart() {
            return new Result(Type.RESTART, 0L);
        }

        static Result resume(long offset) {
            return new Result(Type.RESUME, offset);
        }

        static Result completed(long offset) {
            return new Result(Type.COMPLETED, offset);
        }

        static Result skip(long offset) {
            return new Result(Type.SKIPPED, offset);
        }

        public Type getType() {
            return type;
        }

        public long getOffset() {
            return offset;
        }
    }
}
