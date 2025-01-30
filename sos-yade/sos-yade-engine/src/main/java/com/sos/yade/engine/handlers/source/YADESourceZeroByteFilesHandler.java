package com.sos.yade.engine.handlers.source;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourceZeroByteFilesException;

public class YADESourceZeroByteFilesHandler {

    private int zeroSizeFilesCount = 0;

    public List<ProviderFile> filter(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineSourceZeroByteFilesException {
        if (SOSCollection.isEmpty(sourceFiles)) {
            zeroSizeFilesCount = 0;
            return sourceFiles;
        }

        final List<ProviderFile> zeroSizeFiles = new ArrayList<>();
        // parallelStream usage will change the ordering...
        List<ProviderFile> filtered = sourceFiles.stream().filter(f -> {
            if (f.getSize() <= 0) {
                zeroSizeFiles.add(f);
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        zeroSizeFilesCount = zeroSizeFiles.size();

        int i = 1;
        YADESourceArguments args = sourceDelegator.getArgs();
        switch (args.getZeroByteTransfer().getValue()) {
        case YES:      // transfer zero byte files
            break;
        case NO:       // transfer only if least one is not a zero byte file
            if (zeroSizeFilesCount == sourceFiles.size()) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("%s[%s][skip][TransferZeroByteFiles=NO][%s]Bytes=%s", sourceDelegator.getLogPrefix(), i, f.getFullPath(), f
                            .getSize());
                    i++;
                }
                throw new SOSYADEEngineSourceZeroByteFilesException(String.format(
                        "[TransferZeroByteFiles=NO]All %s files have zero byte size, transfer aborted", zeroSizeFilesCount));
            }
            break;
        case RELAXED:  // not transfer zero byte files
            i = 1;
            for (ProviderFile f : zeroSizeFiles) {
                logger.info("%s[%s][skip][TransferZeroByteFiles=RELAXED][%s]Bytes=%s", sourceDelegator.getLogPrefix(), i, f.getFullPath(), f
                        .getSize());
                i++;
            }
            sourceFiles = filtered;
            break;
        case STRICT:   // abort transfer if any zero byte file is found
            if (zeroSizeFilesCount > 0) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("%s[%s][skip][TransferZeroByteFiles=STRICT][%s]Bytes=%s", sourceDelegator.getLogPrefix(), i, f.getFullPath(), f
                            .getSize());
                    i++;
                }
                throw new SOSYADEEngineSourceZeroByteFilesException(String.format("[TransferZeroByteFiles=STRICT]%s zero byte size file(s) detected",
                        zeroSizeFilesCount));
            }
            break;
        default:
            break;
        }
        return sourceFiles;
    }

    @SuppressWarnings("unused")
    private int getZeroSizeFilesCount() {
        return zeroSizeFilesCount;
    }

}
