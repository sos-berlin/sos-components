package com.sos.yade.engine.common.handler.source;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.exception.SOSYADEEngineSourceZeroByteFilesException;

public class YADEEngineSourceZeroByteFilesHandler {

    private int count = 0;

    public List<ProviderFile> filter(ISOSLogger logger, YADESourceArguments args, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineSourceZeroByteFilesException {
        if (SOSCollection.isEmpty(sourceFiles)) {
            count = 0;
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
        count = zeroSizeFiles.size();

        int i = 1;
        switch (args.getZeroByteTransfer().getValue()) {
        case YES:      // transfer zero byte files
            break;
        case NO:       // transfer only if least one is not a zero byte file
            if (count == sourceFiles.size()) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("[Source][%s][skip][TransferZeroByteFiles=NO][%s]Bytes=%s", i, f.getFullPath(), f.getSize());
                    i++;
                }
                throw new SOSYADEEngineSourceZeroByteFilesException(String.format(
                        "[TransferZeroByteFiles=NO]All %s files have zero byte size, transfer aborted", count));
            }
            break;
        case RELAXED:  // not transfer zero byte files
            i = 1;
            for (ProviderFile f : zeroSizeFiles) {
                logger.info("[Source][%s][skip][TransferZeroByteFiles=RELAXED][%s]Bytes=%s", i, f.getFullPath(), f.getSize());
                i++;
            }
            sourceFiles = filtered;
            break;
        case STRICT:   // abort transfer if any zero byte file is found
            if (count > 0) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("[Source][%s][skip][TransferZeroByteFiles=STRICT][%s]Bytes=%s", i, f.getFullPath(), f.getSize());
                    i++;
                }
                throw new SOSYADEEngineSourceZeroByteFilesException(String.format("[TransferZeroByteFiles=STRICT]%s zero byte size file(s) detected",
                        count));
            }
            break;
        default:
            break;
        }
        return sourceFiles;
    }

    public int getCount() {
        return count;
    }

}
