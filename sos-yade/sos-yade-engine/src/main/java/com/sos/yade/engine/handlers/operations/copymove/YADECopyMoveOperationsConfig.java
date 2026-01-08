package com.sos.yade.engine.handlers.operations.copymove;

import com.sos.commons.vfs.commons.IProvider;
import com.sos.commons.vfs.ftp.FTPProvider;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEParallelExecutorFactory;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

public class YADECopyMoveOperationsConfig {

    private final Source source;
    private final Target target;

    private final TransferOperation operation;
    private final String integrityHashAlgorithm;
    private final int bufferSize;
    private final int maxRetries;
    private final int parallelism;
    private final boolean transactional;
    private final boolean checkFileSize;

    public YADECopyMoveOperationsConfig(final TransferOperation operation, final YADEArguments args,
            final YADESourceProviderDelegator sourceDelegator, final YADETargetProviderDelegator targetDelegator, int sourceFilesSize) {
        this.operation = operation;
        this.transactional = args.getTransactional().isTrue();
        // integrityHashAlgorithm - Note: currently source/target only supports md5
        // - TODO - different treatment of integrityHashAlgorithm for source and target
        this.integrityHashAlgorithm = sourceDelegator.getArgs().getIntegrityHashAlgorithm().getValue();
        this.bufferSize = args.getBufferSize().getValue().intValue();

        this.source = new Source(sourceDelegator);
        this.target = new Target(targetDelegator);
        // based on source|target
        this.maxRetries = getMaxRetries(sourceDelegator, targetDelegator);
        this.parallelism = getParallelism(args, sourceFilesSize);
        this.checkFileSize = getCheckFileSize(sourceDelegator, targetDelegator, target);
    }

    public boolean isMoveOperation() {
        return TransferOperation.MOVE.equals(operation);
    }

    private boolean isCopyOperation() {
        return TransferOperation.COPY.equals(operation);
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
            return 0;
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

    private boolean isBinaryMode(IProvider provider) {
        if (!(provider instanceof FTPProvider)) {
            return true;
        }
        return ((FTPProvider) provider).getArguments().isBinaryTransferMode();
    }

    public String getIntegrityHashAlgorithm() {
        return integrityHashAlgorithm;
    }

    public String getIntegrityHashFileExtensionWithDot() {
        return "." + integrityHashAlgorithm;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getParallelism() {
        return parallelism;
    }

    private int getParallelism(YADEArguments args, int sourceFilesSize) {
        if (target.getCumulate() != null) {
            return 1;
        }
        return YADEParallelExecutorFactory.getParallelism(args, sourceFilesSize);
    }

    public boolean processFilesSequentially() {
        return parallelism == 1;
    }

    public boolean isCheckFileSizeEnabled() {
        return checkFileSize;
    }

    public boolean isTransactionalEnabled() {
        return transactional;
    }

    public Source getSource() {
        return source;
    }

    public Target getTarget() {
        return target;
    }

    public class Source {

        private final String directory;
        private final String pathSeparator;
        private final boolean recursiveSelection;
        private final boolean replacemenEnabled;
        private final boolean checkIntegrityHash;
        private final boolean needsFilePostProcessing;

        private Source(YADESourceProviderDelegator sourceDelegator) {
            // directory path without trailing separator
            this.directory = sourceDelegator.getDirectory() == null ? "" : sourceDelegator.getDirectory();
            this.pathSeparator = sourceDelegator.getProvider().getPathSeparator();
            this.recursiveSelection = sourceDelegator.getArgs().getRecursive().isTrue();
            this.replacemenEnabled = isCopyOperation() && sourceDelegator.getArgs().isReplacementEnabled();
            this.checkIntegrityHash = sourceDelegator.getArgs().getCheckIntegrityHash().isTrue();
            this.needsFilePostProcessing = !isMoveOperation() && sourceDelegator.getArgs().isReplacementEnabled() || sourceDelegator.getArgs()
                    .getCommands().isFilePostProcessingEnabled();
        }

        public String getDirectory() {
            return directory;
        }

        public String getPathSeparator() {
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

        public boolean needsFilePostProcessing() {
            return needsFilePostProcessing;
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
        private final boolean needsFilePostProcessing;

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
            needsFilePostProcessing = targetDelegator.getArgs().getCommands().isFilePostProcessingEnabled() || atomic != null || replacementEnabled
                    || keepModificationDate || createIntegrityHashFile;
        }

        private Cumulate initializeCumulate(final YADETargetProviderDelegator targetDelegator, Compress compress, Atomic atomic) {
            if (!targetDelegator.getArgs().isCumulateFilesEnabled()) {
                return null;
            }
            return new Cumulate(targetDelegator, compress, atomic);
        }

        private Compress initializeCompress(final YADETargetProviderDelegator targetDelegator) {
            if (!targetDelegator.getArgs().isCompressFilesEnabled()) {
                return null;
            }
            return new Compress(targetDelegator.getArgs().getCompressedFileExtension().getValue());
        }

        private Atomic initializeAtomic(final YADETargetProviderDelegator targetDelegator) {
            if (!transactional && !targetDelegator.getArgs().isAtomicityEnabled()) {
                return null;
            }
            // Transactional: Source(Any Provider) -> Jump(SSHProvider)
            // - does not require atomic transfer (if not defined) for transactions, since an additional directory is created at the jump host
            // -- this speeds up the transfer as no additional renaming is required
            if (transactional && targetDelegator.isJumpHost() && !targetDelegator.getArgs().isAtomicityEnabled()) {
                return null;
            }
            return new Atomic(targetDelegator.getArgs().getAtomicPrefix().getValue(), targetDelegator.getArgs().getAtomicSuffix().getValue());
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

        public boolean needsFilePostProcessing() {
            return needsFilePostProcessing;
        }

    }

    public class Cumulate {

        private final static String EXISTING_FILE_SUFFIX_IF_COMPRESS = ".tmp.orig";
        private final static String TRANSFER_FILE_SUFFIX_IF_COMPRESS = ".tmp.current";

        private final YADETargetProviderFile file;
        private final String fileSeparator;
        private final boolean deleteFile;

        private Cumulate(final YADETargetProviderDelegator targetDelegator, final Compress compress, final Atomic atomic) {
            this.file = getFile(targetDelegator, compress, atomic);
            this.fileSeparator = targetDelegator.getArgs().getCumulativeFileSeparator().getValue();
            this.deleteFile = targetDelegator.getArgs().getCumulativeFileDelete().isTrue();
        }

        /** If the cumulative file should not be deleted before the operation (it means: the contents should be appended to an existing cumulative file)<br/>
         * - the existing cumulative file is de-compressed to a temporary file If compress - it is not possible to update a compressed file directly with the
         * compressed contents of a single source file.<br/>
         * -- it means: the content of single source file are written to a temporary cumulative file without compression<br/>
         * - When all source files have been processed:<br/>
         * -- transactional=true<br/>
         * --- on failure - the temporary file is deleted<br/>
         * --- on success - the temporary file is compressed to the defined file name<br/>
         * -- transactional=false<br/>
         * --- on failure - a single source file error is ignored(cumulative file content may be inconsistent)<br/>
         * --- at the end - the temporary file is compressed<br/>
         * 
         * @param targetDelegator
         * @param compress
         * @param atomic
         * @return */
        private YADETargetProviderFile getFile(final YADETargetProviderDelegator targetDelegator, final Compress compress, final Atomic atomic) {
            YADETargetProviderFile tmp = new YADETargetProviderFile(targetDelegator, getFileFullPath(targetDelegator));

            // See YADEProviderFile.initTarget
            // Note: compress extension is not used because the cumulative file provides the file name with extension
            /** finalFileName: the final name of the file after transfer (compressed) */
            String finalFileName = tmp.getName();
            /** transferFileName: file name during transfer - same path as finalFileName but can contains the atomic prefix/suffix */
            String transferFileName = finalFileName;
            if (compress != null) {
                transferFileName = finalFileName + TRANSFER_FILE_SUFFIX_IF_COMPRESS;
            } else if (atomic != null) {
                transferFileName = atomic.getPrefix() + finalFileName + atomic.getSuffix();
            }

            String transferFileFullPath = targetDelegator.appendPath(tmp.getParentFullPath(), transferFileName);
            YADETargetProviderFile file = new YADETargetProviderFile(targetDelegator, transferFileFullPath);
            file.setFinalFullPath(targetDelegator, finalFileName);
            return file;
        }

        private String getFileFullPath(final YADETargetProviderDelegator targetDelegator) {
            String path = targetDelegator.getProvider().toPathStyle(targetDelegator.getArgs().getCumulativeFileName().getValue());
            if (!targetDelegator.getProvider().isAbsolutePath(path)) {
                if (targetDelegator.getDirectory() != null) {
                    path = targetDelegator.appendPath(targetDelegator.getDirectory(), path);
                }
            }
            return path;
        }

        public YADETargetProviderFile getFile() {
            return file;
        }

        public String getTmpFullPathOfExistingFileForDecompress() {
            return file.getFinalFullPath() + EXISTING_FILE_SUFFIX_IF_COMPRESS;
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

        private final String prefix;
        private final String suffix;

        private Atomic(String prefix, String suffix) {
            this.prefix = getOrDefault(prefix, "");
            this.suffix = this.prefix.isEmpty() ? getOrDefault(suffix, "~") : getOrDefault(suffix, "");
        }

        private static String getOrDefault(String val, String defaultVal) {
            return val == null ? defaultVal : val.trim();
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }

    }

}
