package com.sos.yade.engine.handlers.operations;

import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;

public class YADECopyOrMoveOperationTargetFilesConfig {

    private static final String DEFAULT_ATOMIC_SUFFIX = "~";

    private final Source source;
    private final Cumulate cumulate;
    private final Compress compress;
    private final Atomic atomic;

    private boolean replacingEnabled;
    private boolean createDirectories;

    public YADECopyOrMoveOperationTargetFilesConfig(final YADESourceProviderDelegator sourceDelegator,
            final YADETargetProviderDelegator targetDelegator) {
        source = new Source(sourceDelegator);
        cumulate = initializeCumulate(targetDelegator);
        compress = initializeCompress(targetDelegator);
        atomic = initializeAtomic(targetDelegator);

        replacingEnabled = targetDelegator.getArgs().isReplacingEnabled();
        createDirectories = targetDelegator.getArgs().getCreateDirectories().isTrue();

        // ?? YADE1 not use compressFileExtension if cumulate
        // if (cumulate && compress) {
        // cumulativeFileFullPath = cumulativeFileFullPath + compressFileExtension;
        // }
    }

    private Cumulate initializeCumulate(final YADETargetProviderDelegator targetDelegator) {
        if (targetDelegator.getArgs().getCumulativeFileName().isEmpty()) {
            return null;
        }
        return new Cumulate(targetDelegator);
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

        if (!transactional && prefix == null && suffix == null) {
            return null;
        }
        return new Atomic(transactional, prefix, suffix);
    }

    public Source getSource() {
        return source;
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

    public boolean isReplacingEnabled() {
        return replacingEnabled;
    }

    public boolean createDirectories() {
        return createDirectories;
    }

    public class Source {

        private final ProviderDirectoryPath directory;
        private final String pathSeparatot;
        private final boolean recursiveSelection;
        private final boolean singleFilesSelection;

        private Source(YADESourceProviderDelegator sourceDelegator) {
            directory = sourceDelegator.getDirectory();
            pathSeparatot = sourceDelegator.getPathSeparator();
            recursiveSelection = sourceDelegator.getArgs().getRecursive().isTrue();
            singleFilesSelection = sourceDelegator.getArgs().isSingleFilesSelection();
        }

        public ProviderDirectoryPath getDirectory() {
            return directory;
        }

        public String getPathSeparatot() {
            return pathSeparatot;
        }

        public boolean isRecursiveSelection() {
            return recursiveSelection;
        }

        public boolean isSingleFilesSelection() {
            return singleFilesSelection;
        }
    }

    public class Cumulate {

        private final boolean deleteFile;
        private final String file;

        private Cumulate(final YADETargetProviderDelegator targetDelegator) {
            this.deleteFile = targetDelegator.getArgs().getCumulativeFileDelete().isTrue();
            this.file = getFileFullPath(targetDelegator);
        }

        private String getFileFullPath(final YADETargetProviderDelegator targetDelegator) {
            String path = targetDelegator.normalizePath(targetDelegator.getArgs().getCumulativeFileName().getValue());
            if (!targetDelegator.getProvider().isAbsolutePath(path)) {
                if (targetDelegator.getDirectory() != null) {
                    path = targetDelegator.getDirectory().getPathWithTrailingSeparator() + path;
                }
            }
            return path;
        }

        public boolean deleteFile() {
            return deleteFile;
        }

        public String getFile() {
            return file;
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
            this.suffix = getOrDefault(suffix, DEFAULT_ATOMIC_SUFFIX);
        }

        private static String getOrDefault(String val, String defaultVal) {
            return val == null ? defaultVal : val.trim();
        }

        public boolean isTransactional() {
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
