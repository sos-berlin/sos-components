package com.sos.yade.engine.handlers.operations.copymove;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderFile;
import com.sos.yade.engine.helpers.YADEArgumentsHelper;

public class YADECopyMoveOperationConfig {

    private final Source source;
    private final Target target;

    private final String integrityHashAlgorithm;
    private final int bufferSize;
    private final int maxRetries;
    private final boolean checkFileSize;

    public YADECopyMoveOperationConfig(final TransferOperation operation, final YADEArguments args, final YADESourceProviderDelegator sourceDelegator,
            final YADETargetProviderDelegator targetDelegator) {
        source = new Source(operation, sourceDelegator);
        target = new Target(targetDelegator);

        integrityHashAlgorithm = YADEArgumentsHelper.getIntegrityHashAlgorithm(args, sourceDelegator, targetDelegator);
        bufferSize = args.getBufferSize().getValue().intValue();
        maxRetries = getMaxRetries(sourceDelegator, targetDelegator);
        checkFileSize = getCheckFileSize(sourceDelegator, targetDelegator, target);
    }

    private int getMaxRetries(final YADESourceProviderDelegator sourceDelegator, final YADETargetProviderDelegator targetDelegator) {
        if (sourceDelegator.getArgs().isRetryOnConnectionErrorEnabled() && targetDelegator.getArgs().isRetryOnConnectionErrorEnabled()) {
            return Math.max(sourceDelegator.getArgs().getConnectionErrorRetryCountMax().getValue().intValue(), targetDelegator.getArgs()
                    .getConnectionErrorRetryCountMax().getValue().intValue());
        } else if (sourceDelegator.getArgs().isRetryOnConnectionErrorEnabled()) {
            return sourceDelegator.getArgs().getConnectionErrorRetryCountMax().getValue().intValue();
        } else if (targetDelegator.getArgs().isRetryOnConnectionErrorEnabled()) {
            return targetDelegator.getArgs().getConnectionErrorRetryCountMax().getValue().intValue();
        } else {
            return 1;
        }
    }

    // TODO YADE1 checks source/target FTPS(S) provider because of a possible ASCII transfer mode - in this case the check should also be suppressed
    private boolean getCheckFileSize(YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, Target target) {
        if (!targetDelegator.getArgs().getCheckSize().isTrue()) {
            return false;
        }
        if (target.getCompress() != null) {
            return false;
        }
        if (!isBinaryMode(sourceDelegator.getProvider())) {
            return false;
        }
        if (!isBinaryMode(targetDelegator.getProvider())) {
            return false;
        }
        return true;
    }

    // TODO
    private boolean isBinaryMode(IProvider provider) {
        // if (!(provider instanceof FTPProvider)) {
        // return true;
        // }
        // return ((FTPProvider) provider).isBinaryMode();
        return true;
    }

    public String getIntegrityHashAlgorithm() {
        return integrityHashAlgorithm;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public boolean isCheckFileSizeEnabled() {
        return checkFileSize;
    }

    public Source getSource() {
        return source;
    }

    public Target getTarget() {
        return target;
    }

    public class Source {

        private final String directory;
        private final char pathSeparator;
        private final boolean recursiveSelection;
        private final boolean replacemenEnabled;
        private final boolean checkIntegrityHash;

        private Source(TransferOperation operation, YADESourceProviderDelegator sourceDelegator) {
            // directory path without trailing separator
            this.directory = sourceDelegator.getDirectory() == null ? "" : sourceDelegator.getDirectory().getPath();
            this.pathSeparator = sourceDelegator.getPathSeparator();
            this.recursiveSelection = sourceDelegator.getArgs().getRecursive().isTrue();
            this.replacemenEnabled = TransferOperation.COPY.equals(operation) && sourceDelegator.getArgs().isReplacementEnabled();
            this.checkIntegrityHash = sourceDelegator.getArgs().getCheckIntegrityHash().isTrue();
        }

        public String getDirectory() {
            return directory;
        }

        public char getPathSeparator() {
            return pathSeparator;
        }

        public boolean isRecursiveSelection() {
            return recursiveSelection;
        }

        public boolean isReplacementEnabled() {
            return replacemenEnabled;
        }

        public boolean isCheckIntegrityHashEnabled() {
            return checkIntegrityHash;
        }
    }

    public class Target {

        private final Cumulate cumulate;
        private final Compress compress;
        private final Atomic atomic;

        private final boolean createDirectories;
        private final boolean replacementEnabled;
        private final boolean overwriteFiles;
        private final boolean append;
        private final boolean createIntegrityHashFile;
        private final boolean keepModificationDate;

        private Target(YADETargetProviderDelegator targetDelegator) {
            this.compress = initializeCompress(targetDelegator);
            this.atomic = initializeAtomic(targetDelegator);
            this.cumulate = initializeCumulate(targetDelegator, compress, atomic);

            this.createDirectories = targetDelegator.getArgs().getCreateDirectories().isTrue();
            this.replacementEnabled = cumulate == null && targetDelegator.getArgs().isReplacementEnabled();
            this.overwriteFiles = cumulate == null && targetDelegator.getArgs().getOverwriteFiles().isTrue();
            this.append = cumulate == null ? targetDelegator.getArgs().getAppendFiles().isTrue() : true;
            this.createIntegrityHashFile = targetDelegator.getArgs().getCreateIntegrityHashFile().isTrue();
            this.keepModificationDate = targetDelegator.getArgs().getKeepModificationDate().isTrue();
            // ?? YADE1 not use compressFileExtension if cumulate
            // if (cumulate && compress) {
            // cumulativeFileFullPath = cumulativeFileFullPath + compressFileExtension;
            // }
        }

        private Cumulate initializeCumulate(final YADETargetProviderDelegator targetDelegator, Compress compress, Atomic atomic) {
            if (targetDelegator.getArgs().getCumulativeFileName().isEmpty() || targetDelegator.getArgs().getCumulativeFileSeparator().isEmpty()) {
                return null;
            }
            return new Cumulate(targetDelegator, atomic);
        }

        private Compress initializeCompress(final YADETargetProviderDelegator targetDelegator) {
            if (targetDelegator.getArgs().getCompressedFileExtension().isEmpty()) {
                return null;
            }
            return new Compress(targetDelegator.getArgs().getCompressedFileExtension().getValue());
        }

        private Atomic initializeAtomic(final YADETargetProviderDelegator targetDelegator) {
            boolean transactional = targetDelegator.getArgs().getTransactional().isTrue();
            String prefix = targetDelegator.getArgs().getAtomicPrefix().getValue();
            String suffix = targetDelegator.getArgs().getAtomicSuffix().getValue();

            if (!transactional && SOSString.isEmpty(prefix) && SOSString.isEmpty(suffix)) {
                return null;
            }
            return new Atomic(transactional, prefix, suffix);
        }

        public Cumulate getCumulate() {
            return cumulate;
        }

        public Compress getCompress() {
            return compress;
        }

        public Atomic getAtomic() {
            return atomic;
        }

        public boolean isReplacementEnabled() {
            return replacementEnabled;
        }

        public boolean isCreateDirectoriesEnabled() {
            return createDirectories;
        }

        public boolean isOverwriteFilesEnabled() {
            return overwriteFiles;
        }

        public boolean isAppendEnabled() {
            return append;
        }

        public boolean isCreateIntegrityHashFileEnabled() {
            return createIntegrityHashFile;
        }

        public boolean isKeepModificationDateEnabled() {
            return keepModificationDate;
        }

        public boolean isDeleteCumulativeFileEnabled() {
            return cumulate != null && cumulate.deleteFile;
        }

    }

    public class Cumulate {

        private final YADETargetProviderFile file;
        private final String fileSeparator;
        private final boolean deleteFile;

        private Cumulate(final YADETargetProviderDelegator targetDelegator, final Atomic atomic) {
            this.file = getFile(targetDelegator, atomic);
            this.fileSeparator = targetDelegator.getArgs().getCumulativeFileSeparator().getValue();
            this.deleteFile = targetDelegator.getArgs().getCumulativeFileDelete().isTrue();
        }

        // TODO optimize - clone methods ....
        private YADETargetProviderFile getFile(final YADETargetProviderDelegator targetDelegator, final Atomic atomic) {
            YADETargetProviderFile tmp = new YADETargetProviderFile(getFileFullPath(targetDelegator));

            // See YADEProviderFile.initTarget
            // Note: compress extension is not used because the cumulative file provides the file name with extension
            /** finalFileName: the final name of the file after transfer (compressed) */
            String finalFileName = tmp.getName();
            /** transferFileName: file name during transfer - same path as finalFileName but can contains the atomic prefix/suffix */
            String transferFileName = finalFileName;
            if (atomic != null) {
                transferFileName = atomic.getPrefix() + finalFileName + atomic.getSuffix();
            }

            String transferFileFullPath = SOSPathUtil.appendPath(tmp.getParentFullPath(), transferFileName, targetDelegator.getPathSeparator());
            YADETargetProviderFile file = new YADETargetProviderFile(transferFileFullPath);
            if (atomic != null) {
                file.setFinalName(finalFileName);
            }
            return file;
        }

        private String getFileFullPath(final YADETargetProviderDelegator targetDelegator) {
            String path = targetDelegator.normalizePath(targetDelegator.getArgs().getCumulativeFileName().getValue());
            if (!targetDelegator.getProvider().isAbsolutePath(path)) {
                if (targetDelegator.getDirectory() != null) {
                    path = SOSPathUtil.appendPath(targetDelegator.getDirectory().getPath(), path, targetDelegator.getPathSeparator());
                }
            }
            return path;
        }

        public YADETargetProviderFile getFile() {
            return file;
        }

        public String getFileSeparator() {
            return fileSeparator;
        }
    }

    public class Compress {

        private final String fileExtension;

        private Compress(String fileExtension) {
            this.fileExtension = getFileExtension(fileExtension);
        }

        public String getFileExtension() {
            return fileExtension;
        }

        private String getFileExtension(String extension) {
            return extension.startsWith(".") ? extension : "." + extension;
        }
    }

    public class Atomic {

        private final boolean transactional;
        private final String prefix;
        private final String suffix;

        private Atomic(boolean transactional, String prefix, String suffix) {
            this.transactional = transactional;
            this.prefix = getOrDefault(prefix, "");
            this.suffix = this.prefix.isEmpty() ? getOrDefault(suffix, "~") : getOrDefault(suffix, "");
        }

        private static String getOrDefault(String val, String defaultVal) {
            return val == null ? defaultVal : val.trim();
        }

        public boolean isTransactionalEnabled() {
            return transactional;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }

    }

}
