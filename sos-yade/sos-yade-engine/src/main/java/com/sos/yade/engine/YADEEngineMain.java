package com.sos.yade.engine;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sos.commons.util.SOSCLIArgumentsParser;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade;
import com.sos.yade.engine.commons.YADEOutcomeHistory;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLArgumentsLoader;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public class YADEEngineMain {

    private static final String STARTUP_SWITCH_HELP_1 = "h";
    private static final String STARTUP_SWITCH_HELP_2 = "help";
    // the standard names uses '_' see normalizeArguments(...)
    private static final String STARTUP_ARG_RETURN_VALUES = "return_values";

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

        String historyReturnValuesFile = null;
        AYADEArgumentsLoader argsLoader = null;
        List<ProviderFile> files = null;
        Throwable exception = null;
        try {
            // CLI Arguments
            Map<String, String> cliArgs = SOSCLIArgumentsParser.parse(args);
            Map<String, String> normalizedArgs = normalizeArguments(cliArgs, Set.of("file_spec", "file_path", "file_list"));
            if (logger.isDebugEnabled()) {
                logger.debug("[cliArgs]" + cliArgs);
                logger.debug("[normalizedArgs]" + normalizedArgs);
            }
            if (!checkArguments(normalizedArgs)) {
                return 0;
            }

            historyReturnValuesFile = normalizedArgs.get(STARTUP_ARG_RETURN_VALUES);

            Path settings = Path.of(getRequiredArgumentValue(normalizedArgs, YADEArguments.STARTUP_ARG_SETTINGS));
            String profile = getRequiredArgumentValue(normalizedArgs, YADEArguments.STARTUP_ARG_PROFILE);
            Boolean settingsReplacerCaseSensitive = getBooleanValue(normalizedArgs, YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE,
                    YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE_DEFAULT);
            Boolean settingsReplacerKeepUnresolved = getBooleanValue(normalizedArgs, YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED,
                    YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED_DEFAULT);

            // Load XML Settings file configuration
            argsLoader = new YADEXMLArgumentsLoader().load(logger, settings, profile, System.getenv(), settingsReplacerCaseSensitive,
                    settingsReplacerKeepUnresolved);

            argsLoader.getArgs().getParallelism().setValue(getIntegerValue(normalizedArgs, YADEArguments.STARTUP_ARG_PARALLELISM, String.valueOf(
                    YADEArguments.STARTUP_ARG_PARALLELISM_DEFAULT)));
            // Overrides some settings with the CLI Arguments
            applyOverrides(argsLoader, normalizedArgs);

            // Execute YADE Transfer
            YADEEngine engine = new YADEEngine();
            files = engine.execute(logger, argsLoader, true);

            return 0;
        } catch (Throwable e) {
            exception = e;
            logger.error(e.toString(), e);
            return ERROR_EXIT_CODE;
        } finally {
            if (historyReturnValuesFile != null) {
                writeHistoryToReturnValuesFile(logger, historyReturnValuesFile, argsLoader, files, exception);
            }
            engine = null;
        }
    }

    private static boolean checkArguments(Map<String, String> args) throws YADEEngineSettingsLoadException {
        if (args.size() > 0 && (args.containsKey(STARTUP_SWITCH_HELP_1) || args.containsKey(STARTUP_SWITCH_HELP_2))) {
            printUsage();
            return false;
        }

        List<String> missingRequired = new ArrayList<>();
        if (!args.containsKey(YADEArguments.STARTUP_ARG_SETTINGS)) {
            missingRequired.add(YADEArguments.STARTUP_ARG_SETTINGS);
        }
        if (!args.containsKey(YADEArguments.STARTUP_ARG_PROFILE)) {
            missingRequired.add(YADEArguments.STARTUP_ARG_PROFILE);
        }
        if (missingRequired.size() > 0) {
            printUsage();
            throw new YADEEngineSettingsLoadException("[missing required arguments]" + String.join(",", missingRequired));
        }
        return true;
    }

    /** argument names can contain: '-' or '_', e.g. 'source_dir' or 'source-dir'<br/>
     * some arguments are adjusted to use a 'source_' prefix, e.g. 'file_spec' -> 'source_file_spec' */
    private static Map<String, String> normalizeArguments(Map<String, String> cliArgs, Set<String> sourceArgsWithoutPrefix) {
        Map<String, String> r = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : cliArgs.entrySet()) {
            String newKey = entry.getKey().toLowerCase().replace('-', '_');
            if (sourceArgsWithoutPrefix.contains(newKey)) {
                newKey = "source_" + newKey;
            }
            r.put(newKey, entry.getValue());
        }
        return r;
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

    private static void writeHistoryToReturnValuesFile(ISOSLogger logger, String historyReturnValuesFile, AYADEArgumentsLoader argsLoader,
            List<ProviderFile> files, Throwable exception) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("[writeHistoryToReturnValuesFile][%s]...", historyReturnValuesFile);
            }
            String serialized = YADEOutcomeHistory.get(argsLoader, files, exception);
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(historyReturnValuesFile), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                writer.write(new StringBuilder(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES).append("=").append(serialized).append(System.lineSeparator())
                        .toString());
                writer.flush();
            }
        } catch (Exception e) {
            logger.error("[writeHistoryToReturnValuesFile]" + e.toString(), e);
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  Options:");
        System.out.println("    Transfer Options (required):");
        printArgumentUsage(YADEArguments.STARTUP_ARG_SETTINGS, "<location of the settings xml file>", "");
        printArgumentUsage(YADEArguments.STARTUP_ARG_PROFILE, "<profile id>", "");

        System.out.println("    Transfer Options:");
        printArgumentUsage(YADEArguments.STARTUP_ARG_SOURCE_DIR, "<...>", null);
        printArgumentUsage(YADEArguments.STARTUP_ARG_SOURCE_EXCLUDED_DIRECTORIES, "<...>", null);
        printArgumentUsage(YADEArguments.STARTUP_ARG_SOURCE_FILE_LIST, "<...>", null);
        printArgumentUsage(YADEArguments.STARTUP_ARG_SOURCE_FILE_PATH, "<...>", null);
        printArgumentUsage(YADEArguments.STARTUP_ARG_SOURCE_FILE_SPEC, "<...>", null);
        printArgumentUsage(YADEArguments.STARTUP_ARG_TARGET_DIR, "<...>", null);

        System.out.println("    Processing Options:");
        printArgumentUsage(YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE, "<boolean>", "default: "
                + YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE_DEFAULT);
        printArgumentUsage(YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED, "<boolean>", "default: "
                + YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED_DEFAULT);
        printArgumentUsage(YADEArguments.STARTUP_ARG_PARALLELISM, "<integer>", "default: " + YADEArguments.STARTUP_ARG_PARALLELISM_DEFAULT);

        System.out.println("    Switches:");
        printArgumentUsage("-" + STARTUP_SWITCH_HELP_1 + " | --" + STARTUP_SWITCH_HELP_2, null, "displays usage", false);
    }

    private static void printArgumentUsage(String name, String valueDescription, String description) {
        printArgumentUsage(name, valueDescription, description, true);
    }

    private static void printArgumentUsage(String name, String valueDescription, String description, boolean useNamePrefix) {
        String value = SOSString.isEmpty(valueDescription) ? "" : "=" + valueDescription;
        String desc = "";
        String namePrefix = useNamePrefix ? "--" : "";
        if (!SOSString.isEmpty(description)) {
            desc = "| " + description;
        }
        System.out.println(String.format("      %-46s%s", namePrefix + (formatAsShellOption(name) + value), desc));
    }

    private static String formatAsShellOption(String arg) {
        return arg.replace("_", "-");
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
