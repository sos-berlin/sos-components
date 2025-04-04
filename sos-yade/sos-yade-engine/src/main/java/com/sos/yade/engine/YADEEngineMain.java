package com.sos.yade.engine;

import java.nio.file.Path;
import java.util.Map;

import com.sos.commons.util.SOSCLIArgumentsParser;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLArgumentsLoader;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public class YADEEngineMain {

    private static final int ERROR_EXIT_CODE = 99;

    private YADEEngine engine;
    private ISOSLogger logger;

    public static void main(String[] args) {
        YADEEngineMain main = new YADEEngineMain();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (main.engine != null) {
                    main.engine.cancel(main.logger);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }));

        System.exit(main.execute(args));
    }

    private int execute(String[] args) {
        logger = new SLF4JLogger();
        try {
            // CLI Arguments
            Map<String, String> cliArgs = SOSCLIArgumentsParser.parse(args);
            if (cliArgs.size() < 2) {
                throw new YADEEngineSettingsLoadException("missing -" + YADEArguments.STARTUP_ARG_SETTINGS + ",-"
                        + YADEArguments.STARTUP_ARG_PROFILE);
            }
            Path settings = Path.of(getRequiredArgumentValue(cliArgs, YADEArguments.STARTUP_ARG_SETTINGS));
            String profile = getRequiredArgumentValue(cliArgs, YADEArguments.STARTUP_ARG_PROFILE);
            Boolean settingsReplacerCaseSensitive = getBooleanValue(cliArgs, YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE,
                    YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE_DEFAULT);
            Boolean settingsReplacerKeepUnresolved = getBooleanValue(cliArgs, YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED,
                    YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED_DEFAULT);

            // Load XML Settings file configuration
            YADEXMLArgumentsLoader argsLoader = new YADEXMLArgumentsLoader().load(logger, settings, profile, System.getenv(),
                    settingsReplacerCaseSensitive, settingsReplacerKeepUnresolved);

            argsLoader.getArgs().getParallelism().setValue(getIntegerValue(cliArgs, YADEArguments.STARTUP_ARG_PARALLELISM, "1"));
            // Overrides some settings with the CLI Arguments
            applyOverrides(argsLoader, cliArgs);

            // Execute YADE Transfer
            YADEEngine engine = new YADEEngine();
            engine.execute(logger, argsLoader, true);

            return 0;
        } catch (Throwable e) {
            logger.error(e.toString(), e);
            return ERROR_EXIT_CODE;
        } finally {
            engine = null;
        }
    }

    private void applyOverrides(AYADEArgumentsLoader argsLoader, Map<String, String> args) {
        // Source
        setOptionalStringArgument(argsLoader.getSourceArgs().getDirectory(), args, YADEArguments.STARTUP_ARG_SOURCE_DIR);
        setOptionalStringArgument(argsLoader.getSourceArgs().getExcludedDirectories(), args, YADEArguments.STARTUP_ARG_SOURCE_EXCLUDED_DIRECTORIES);
        setOptionalPathArgument(argsLoader.getSourceArgs().getFileList(), args, YADEArguments.STARTUP_ARG_SOURCE_FILE_LIST);
        setOptionalSourceFilePath(argsLoader.getSourceArgs(), args, YADEArguments.STARTUP_ARG_SOURCE_FILE_PATH);
        setOptionalStringArgument(argsLoader.getSourceArgs().getFileSpec(), args, YADEArguments.STARTUP_ARG_SOURCE_FILE_SPEC);
        // Target
        setOptionalStringArgument(argsLoader.getTargetArgs().getDirectory(), args, YADEArguments.STARTUP_ARG_TARGET_DIR);
    }

    private String getRequiredArgumentValue(Map<String, String> args, String name) throws YADEEngineSettingsLoadException {
        String val = args.get(name);
        if (SOSString.isEmpty(val)) {
            throw new YADEEngineSettingsLoadException("missing -" + name);
        }
        return val;
    }

    private void setOptionalStringArgument(SOSArgument<String> arg, Map<String, String> args, String name) {
        String val = args.get(name);
        if (!SOSString.isEmpty(val)) {
            arg.setValue(val);
        }
    }

    private void setOptionalPathArgument(SOSArgument<Path> arg, Map<String, String> args, String name) {
        String val = args.get(name);
        if (!SOSString.isEmpty(val)) {
            arg.setValue(Path.of(val));
        }
    }

    private void setOptionalSourceFilePath(YADESourceArguments sourceArgs, Map<String, String> args, String name) {
        String val = args.get(name);
        if (!SOSString.isEmpty(val)) {
            sourceArgs.setFilePath(val);
        }
    }

    private Boolean getBooleanValue(Map<String, String> args, String name, Boolean defaultValue) throws YADEEngineSettingsLoadException {
        return Boolean.valueOf(args.getOrDefault(name, defaultValue + ""));
    }

    private Integer getIntegerValue(Map<String, String> args, String name, String defaultValue) throws YADEEngineSettingsLoadException {
        return Integer.valueOf(args.getOrDefault(name, defaultValue));
    }
}
