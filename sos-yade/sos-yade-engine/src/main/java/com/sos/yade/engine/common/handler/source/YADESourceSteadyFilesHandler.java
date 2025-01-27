package com.sos.yade.engine.common.handler.source;

import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.common.YADEHelper;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.exception.SOSYADEEngineSourceSteadyFilesException;

public class YADESourceSteadyFilesHandler {

    public static boolean checkFilesSteady(ISOSLogger logger, IProvider sourceProvider, YADESourceArguments args, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineSourceSteadyFilesException {
        if (!args.checkSteadyState()) {
            return true;
        }
        if (SOSCollection.isEmpty(sourceFiles)) {
            return true;
        }
        String lp = sourceProvider.getContext().getLogPrefix();
        logger.info("%s[start]checkFilesSteady", lp);
        boolean steady = true;
        int total = args.getCheckSteadyCount().getValue().intValue();
        long interval = YADEHelper.getIntervalInSeconds(args.getCheckSteadyStateInterval(), 1);

        ml: for (int i = 0; i < total; i++) {
            steady = true;

            String position = String.format("%sof%s", i + 1, total);
            logger.info(String.format("%s[%s][wait]%ss...", lp, position, interval));
            YADEHelper.waitFor(interval);

            l: for (ProviderFile sourceFile : sourceFiles) {
                if (!checkSourceFileSteady(sourceProvider, sourceFile)) {
                    steady = false;
                    break l;
                }
            }
            if (steady) {
                logger.info("%s[%s][all files seem steady]extra waiting %ss for late comers.", lp, position, interval);
                YADEHelper.waitFor(interval);
                nl: for (ProviderFile sourceFile : sourceFiles) {
                    if (!checkSourceFileSteady(sourceProvider, sourceFile)) {
                        steady = false;
                        break nl;
                    }
                }
            }
            if (steady) {
                logger.info("%s[%s][break]all files are steady.", lp, position);
                break ml;
            }
        }
        if (!steady) {
            throw new SOSYADEEngineSourceSteadyFilesException("[not all files are steady][not steady]" + sourceFiles.stream().filter(
                    f -> !((YADEProviderFile) f).getSteady().isSteady()).map(f -> f.getFullPath()).collect(Collectors.joining(";")));
        }
        return steady;
    }

    private static boolean checkSourceFileSteady(IProvider sourceProvider, ProviderFile sourceFile) throws SOSYADEEngineSourceSteadyFilesException {
        YADEProviderFile yf = (YADEProviderFile) sourceFile;
        if (yf.getSteady().isSteady()) {
            return true;
        }
        try {
            String path = sourceFile.getFullPath();
            sourceFile = sourceProvider.rereadFileIfExists(sourceFile);
            if (sourceFile == null) {
                throw new SOSYADEEngineSourceSteadyFilesException("[" + path + "]not found");
            }
            yf = (YADEProviderFile) sourceFile;
            yf.getSteady().checkIfSteady();

            return yf.getSteady().isSteady();
        } catch (SOSYADEEngineSourceSteadyFilesException e) {
            throw e;
        } catch (Throwable e) {
            throw new SOSYADEEngineSourceSteadyFilesException(e);
        }
    }

}
