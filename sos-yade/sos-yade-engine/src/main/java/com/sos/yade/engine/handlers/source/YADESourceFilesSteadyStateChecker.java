package com.sos.yade.engine.handlers.source;

import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEClientHelper;
import com.sos.yade.engine.exceptions.YADEEngineSourceSteadyFilesException;

public class YADESourceFilesSteadyStateChecker {

    public static void check(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles)
            throws YADEEngineSourceSteadyFilesException {

        YADESourceArguments args = sourceDelegator.getArgs();
        if (!args.isCheckSteadyStateEnabled()) {
            return;
        }
        if (SOSCollection.isEmpty(sourceFiles)) {
            return;
        }
        boolean steady = true;
        int total = args.getCheckSteadyCount().getValue().intValue();
        long interval = SOSArgumentHelper.asSeconds(args.getCheckSteadyStateInterval(), 1L);

        ml: for (int i = 0; i < total; i++) {
            steady = true;

            String position = String.format("checkSteadyCount=%s of %s", i + 1, total);
            logger.info(String.format("[%s][%s][wait]%ss...", sourceDelegator.getLabel(), position, interval));
            YADEClientHelper.waitFor(interval);

            l: for (ProviderFile sourceFile : sourceFiles) {
                if (!checkFileSteadyState(sourceDelegator, sourceFile)) {
                    steady = false;
                    break l;
                }
            }
            if (steady) {
                logger.info("[%s][%s][all files seem steady]extra waiting %ss for late comers", sourceDelegator.getLabel(), position, interval);
                YADEClientHelper.waitFor(interval);
                nl: for (ProviderFile sourceFile : sourceFiles) {
                    if (!checkFileSteadyState(sourceDelegator, sourceFile)) {
                        steady = false;
                        break nl;
                    }
                }
            }
            if (steady) {
                logger.info("[%s][%s][break]all files are steady", sourceDelegator.getLabel(), position);
                break ml;
            }
        }
        if (!steady) {
            throw new YADEEngineSourceSteadyFilesException("[not all files are steady][not steady]" + sourceFiles.stream().filter(
                    f -> !((YADEProviderFile) f).getSteady().isSteady()).map(f -> f.getFullPath()).collect(Collectors.joining(";")));
        }
    }

    private static boolean checkFileSteadyState(YADESourceProviderDelegator sourceDelegator, ProviderFile providerFile)
            throws YADEEngineSourceSteadyFilesException {
        YADEProviderFile file = (YADEProviderFile) providerFile;
        if (file.getSteady().isSteady()) {
            return true;
        }
        try {
            String path = providerFile.getFullPath();
            providerFile = sourceDelegator.getProvider().rereadFileIfExists(providerFile);
            if (providerFile == null) {
                throw new YADEEngineSourceSteadyFilesException("[" + path + "]not found");
            }
            file = (YADEProviderFile) providerFile;
            file.getSteady().checkIfSteady();

            return file.getSteady().isSteady();
        } catch (YADEEngineSourceSteadyFilesException e) {
            throw e;
        } catch (Throwable e) {
            throw new YADEEngineSourceSteadyFilesException(e);
        }
    }

}
