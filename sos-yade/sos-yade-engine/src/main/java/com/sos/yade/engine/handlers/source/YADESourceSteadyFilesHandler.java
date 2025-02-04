package com.sos.yade.engine.handlers.source;

import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourceSteadyFilesException;
import com.sos.yade.engine.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.helpers.YADEHelper;

public class YADESourceSteadyFilesHandler {

    public static void checkFilesSteady(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineSourceSteadyFilesException {

        YADESourceArguments args = sourceDelegator.getArgs();
        if (!args.isCheckSteadyStateEnabled()) {
            return;
        }
        if (SOSCollection.isEmpty(sourceFiles)) {
            return;
        }
        logger.info("%s[start]checkFilesSteady", sourceDelegator.getLogPrefix());
        boolean steady = true;
        int total = args.getCheckSteadyCount().getValue().intValue();
        long interval = YADEArgumentsHelper.getIntervalInSeconds(args.getCheckSteadyStateInterval(), 1);

        ml: for (int i = 0; i < total; i++) {
            steady = true;

            String position = String.format("%sof%s", i + 1, total);
            logger.info(String.format("%s[%s][wait]%ss...", sourceDelegator.getLogPrefix(), position, interval));
            YADEHelper.waitFor(interval);

            l: for (ProviderFile sourceFile : sourceFiles) {
                if (!checkSourceFileSteady(sourceDelegator, sourceFile)) {
                    steady = false;
                    break l;
                }
            }
            if (steady) {
                logger.info("%s[%s][all files seem steady]extra waiting %ss for late comers.", sourceDelegator.getLogPrefix(), position, interval);
                YADEHelper.waitFor(interval);
                nl: for (ProviderFile sourceFile : sourceFiles) {
                    if (!checkSourceFileSteady(sourceDelegator, sourceFile)) {
                        steady = false;
                        break nl;
                    }
                }
            }
            if (steady) {
                logger.info("%s[%s][break]all files are steady.", sourceDelegator.getLogPrefix(), position);
                break ml;
            }
        }
        if (!steady) {
            throw new SOSYADEEngineSourceSteadyFilesException("[not all files are steady][not steady]" + sourceFiles.stream().filter(
                    f -> !((YADEProviderFile) f).getSteady().isSteady()).map(f -> f.getFullPath()).collect(Collectors.joining(";")));
        }
    }

    private static boolean checkSourceFileSteady(YADESourceProviderDelegator sourceDelegator, ProviderFile sourceFile)
            throws SOSYADEEngineSourceSteadyFilesException {
        YADEProviderFile yf = (YADEProviderFile) sourceFile;
        if (yf.getSteady().isSteady()) {
            return true;
        }
        try {
            String path = sourceFile.getFullPath();
            sourceFile = sourceDelegator.getProvider().rereadFileIfExists(sourceFile);
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
