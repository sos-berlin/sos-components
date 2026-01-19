package com.sos.yade.commons.cli;

import java.io.BufferedWriter;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sos.yade.commons.Yade;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.commons.Yade.TransferProtocol;
import com.sos.yade.commons.result.YadeTransferResult;
import com.sos.yade.commons.result.YadeTransferResultEntry;
import com.sos.yade.commons.result.YadeTransferResultProtocol;
import com.sos.yade.commons.result.YadeTransferResultSerializer;

public class TransferHistory {

    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");

    private static final String ENV_VAR_JS7_RETURN_VALUES = "JS7_RETURN_VALUES";
    private static final String ENV_VAR_JS1_RETURN_VALUES = "SCHEDULER_RETURN_VALUES";// compatibility mode v1

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 0;

    private static final int EXIT_STATUS_SUCCESS = 0;
    private static final int EXIT_STATUS_ERROR = 0;

    private static String DELIMITER = ",";
    private static String HOSTNAME;
    private static List<String> UNKNOWN_OPTIONS;
    private static List<String> EMPTY_OPTIONS;
    private static List<String> PARSE_ERRORS;

    public static void main(String[] args) {
        int exitStatus = EXIT_STATUS_SUCCESS;

        if (args.length == 0 || (args.length == 1 && args[0].trim().matches("|-h|--help"))) {
            displayUsage();
        } else {
            exitStatus = process(args);
        }
        System.exit(exitStatus);
    }

    private static int process(String[] args) {
        UNKNOWN_OPTIONS = new ArrayList<>();
        EMPTY_OPTIONS = new ArrayList<>();
        PARSE_ERRORS = new ArrayList<>();

        YadeTransferResult result = new YadeTransferResult();
        YadeTransferResultProtocol source = new YadeTransferResultProtocol();
        YadeTransferResultProtocol target = new YadeTransferResultProtocol();
        List<String> entries = new ArrayList<>();

        boolean hasTarget = false;
        boolean displayUsage = false;
        boolean displayArgs = false;
        boolean displayResult = false;
        boolean dryRun = false;
        for (String arg : args) {
            String[] arr = arg.split("=");
            String pn = arr[0];
            String pv = arr.length > 1 ? getValue(arr[1].trim()) : "";

            switch (pn) {
            case "-h":
            case "--help":
                displayUsage = true;
                break;
            case "--display-args":
                displayArgs = true;// getBoolean(pv, true);
                break;
            case "--display-result":
                displayResult = true;// getBoolean(pv, true);
                break;
            case "--dry-run":
                displayArgs = true;
                displayResult = true;
                dryRun = true;
                break;
            // TRANSFER META
            case "--operation":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                result.setOperation(getOperation(arg, pv));
                break;
            case "--start-time":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                result.setStart(toInstant(arg, pv));
                break;
            case "--end-time":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                result.setEnd(toInstant(arg, pv));
                break;
            case "--error":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                    result.setErrorMessage(null);
                } else {
                    result.setErrorMessage(pv);
                }
                break;

            // SOURCE META
            case "--source-protocol":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                source.setProtocol(getProtocol(arg, pv));
                break;
            case "--source-account":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                source.setAccount(getAccount(pv));
                break;
            case "--source-host":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                source.setHost(pv);
                break;
            case "--source-port":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                source.setPort(getPort(arg, pv));
                break;

            // TARGET META
            case "--target-protocol":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                target.setProtocol(getProtocol(arg, pv));
                hasTarget = true;
                break;
            case "--target-account":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                target.setAccount(getAccount(pv));
                hasTarget = true;
                break;
            case "--target-host":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                target.setHost(pv);
                hasTarget = true;
                break;
            case "--target-port":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                }
                target.setPort(getPort(arg, pv));
                hasTarget = true;
                break;

            // TRANSFER ENTRIES
            case "--delimiter":
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                } else {
                    DELIMITER = pv;
                }
                break;

            case "--transfer-file": // multiple times
                if (isEmpty(pv)) {
                    EMPTY_OPTIONS.add(pn);
                } else {
                    entries.add(pv);
                }
                break;
            default:
                UNKNOWN_OPTIONS.add(arg);
                break;
            }
        }

        if (displayUsage) {
            displayUsage();
        }
        if (displayArgs) {
            displayArguments(args);
        }
        if (UNKNOWN_OPTIONS.size() > 0) {
            System.out.println(String.format("[%s][unknown options]%s", TransferHistory.class.getSimpleName(), String.join(", ", UNKNOWN_OPTIONS)));
        }
        if (EMPTY_OPTIONS.size() > 0) {
            System.out.println(String.format("[%s][empty options]%s", TransferHistory.class.getSimpleName(), String.join(", ", EMPTY_OPTIONS)));
        }
        if (PARSE_ERRORS.size() > 0) {
            System.out.println(String.format("[%s][invalid values]%s", TransferHistory.class.getSimpleName(), String.join(", ", PARSE_ERRORS)));
        }

        try {
            YadeTransferResultSerializer<YadeTransferResult> serializer = new YadeTransferResultSerializer<>();
            String serialized = serializer.serialize(complete(result, source, hasTarget ? target : null, entries));
            if (displayResult) {
                displayResult(serializer, serialized);
            }

            if (!dryRun) {
                String returnValues = System.getenv(ENV_VAR_JS7_RETURN_VALUES);
                if (returnValues == null) {
                    returnValues = System.getenv(ENV_VAR_JS1_RETURN_VALUES);
                    if (returnValues == null) {
                        System.err.println("Missing environment variable: " + ENV_VAR_JS7_RETURN_VALUES + " or " + ENV_VAR_JS1_RETURN_VALUES);
                        return EXIT_STATUS_ERROR;
                    }
                }
                serialize2File(returnValues, serialized);
            }
            return EXIT_STATUS_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_STATUS_ERROR;
        }
    }

    private static void serialize2File(String returnValues, String serialized) throws Exception {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(returnValues), StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(new StringBuilder(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES).append("=").append(serialized).append(System.lineSeparator())
                    .toString());
            writer.flush();
        }
    }

    private static String getValue(String val) {
        if (val.startsWith("\"")) {
            int len = val.length();
            int end = -1;
            if (len > 1 && val.endsWith("\"")) {
                end = len - 1;
            }
            return end > 0 ? val.substring(1, end) : val.substring(1);
        }
        return val;
    }

    @SuppressWarnings("unused")
    private static boolean getBoolean(String val, boolean defaultValue) {
        boolean result = defaultValue;
        if (!isEmpty(val)) {
            try {
                result = Boolean.parseBoolean(val);
            } catch (Exception e) {
            }
        }
        return result;
    }

    private static YadeTransferResult complete(YadeTransferResult result, YadeTransferResultProtocol source, YadeTransferResultProtocol target,
            List<String> entries) {
        // TRANSFER META
        if (isEmpty(result.getOperation())) {
            result.setOperation(TransferOperation.COPY.value());
        }
        TransferOperation operation = TransferOperation.fromValue(result.getOperation());
        if (result.getStart() == null) {
            result.setStart(Instant.now());
        }
        if (result.getEnd() == null) {
            result.setEnd(Instant.now());
        }
        // SOURCE/TARGET META
        result.setSource(complete("--source-", source));
        if (target == null) {
            switch (operation) {
            case COPY:
            case COPYFROMINTERNET:
            case COPYTOINTERNET:
            case RENAME:
            case MOVE:
                target = new YadeTransferResultProtocol();
                target.setProtocol(source.getProtocol());
                target.setHost(source.getHost());
                target.setPort(source.getPort());
                target.setAccount(source.getAccount());
                result.setTarget(target);
                break;
            case GETLIST:
            case REMOVE:
            case UNKNOWN:
                break;

            }
        } else {
            result.setTarget(complete("--target-", target));
        }
        // TRANSFER ENTRIES
        result.setEntries(complete(operation, entries));

        return result;
    }

    private static List<YadeTransferResultEntry> complete(TransferOperation operation, List<String> entries) {
        List<YadeTransferResultEntry> r = new ArrayList<>();
        for (String e : entries) {
            r.add(getEntry(operation, e));
        }
        return r;
    }

    private static Instant toInstant(String arg, String val) {
        String dateTime = val;
        String format = null;
        switch (val.length()) {
        case 24:// 2023-01-01 12:00:00-0200
            dateTime = dateTime.replace(' ', 'T');
            format = "yyyy-MM-dd'T'HH:mm:ssZ";
            break;
        default:// "2023-01-01 12:00:00"
            format = "yyyy-MM-dd HH:mm:ss";
        }

        DateFormat df = new SimpleDateFormat(format);
        try {
            return df.parse(dateTime).toInstant();
        } catch (Exception e) {
            PARSE_ERRORS.add("[" + arg + "]set datetime to now");
            return null;
        }
    }

    private static String getOperation(String arg, String val) {
        // return TransferOperation.fromValue(val).value();
        try {
            return TransferOperation.valueOf(val.toUpperCase()).value();
        } catch (Exception e) {
            PARSE_ERRORS.add("[" + arg + "]set operation to UNKNOWN");
            return TransferOperation.UNKNOWN.value();
        }
    }

    private static String getProtocol(String arg, String val) {
        // return TransferProtocol.fromValue(val).value();
        try {
            return TransferProtocol.valueOf(val.toUpperCase()).value();
        } catch (Exception e) {
            PARSE_ERRORS.add("[" + arg + "]set protocol to UNKNOWN");
            return TransferProtocol.UNKNOWN.value();
        }
    }

    private static int getPort(String arg, String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            PARSE_ERRORS.add("[" + arg + "]set port to " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    private static String getAccount(String val) {
        return isEmpty(val) ? Yade.DEFAULT_ACCOUNT : val;
    }

    private static YadeTransferResultEntry getEntry(TransferOperation operation, String val) {
        String[] arr = Arrays.stream(val.split(DELIMITER, 4)).map(String::trim).toArray(String[]::new);

        YadeTransferResultEntry entry = new YadeTransferResultEntry();
        entry.setSource(arr[0]);
        switch (arr.length) {
        case 1:
            switch (operation) {
            case REMOVE:
            case GETLIST:
                break;
            default:
                entry.setTarget(entry.getSource());
            }

            break;
        case 2:
            entry.setTarget(arr[1]);
            break;
        case 3:
            entry.setTarget(arr[1]);
            try {
                entry.setSize(Long.parseLong(arr[2]));
            } catch (Exception e) {
                entry.setErrorMessage(arr[2]);
            }
            break;
        case 4:
            entry.setTarget(arr[1]);
            try {
                entry.setSize(Long.parseLong(arr[2]));
                entry.setErrorMessage(arr[3]);
            } catch (Exception e) {
                entry.setErrorMessage(arr[2] + DELIMITER + arr[3]);
            }
            break;
        }
        if (isEmpty(entry.getErrorMessage())) {
            switch (operation) {
            case MOVE:
            case RENAME:
                // RENAMED by MOVE because /joc/api/yade/files(com.sos.joc.yade.common.TransferFileUtils.getState) handle MOVED as RENAMED
                entry.setState(TransferEntryState.RENAMED.value());
                break;
            case REMOVE:
                entry.setState(TransferEntryState.DELETED.value());
                break;
            default:
                entry.setState(TransferEntryState.TRANSFERRED.value());
                break;
            }
        } else {
            entry.setState(TransferEntryState.FAILED.value());
        }
        return entry;
    }

    private static YadeTransferResultProtocol complete(String argPrefix, YadeTransferResultProtocol p) {
        if (p == null) {
            return null;
        }

        if (isEmpty(p.getHost())) {
            p.setHost(getHostname());
        }
        if (p.getProtocol() == null || p.getHost().contains("://")) {
            try {
                URL url = new URL(p.getHost());
                if (p.getProtocol() == null) {
                    String pl = url.getProtocol().toLowerCase();
                    if (pl.equals("file")) {
                        p.setProtocol(TransferProtocol.LOCAL.value());
                    } else {
                        p.setProtocol(TransferProtocol.fromValue(pl).value());
                    }
                }
                if (!isEmpty(url.getHost())) {
                    p.setHost(url.getHost());
                }
                if (p.getPort() == null && url.getPort() != -1) {
                    p.setPort(url.getPort());
                }
                if (p.getAccount() == null) {
                    p.setAccount(url.getUserInfo());
                }
            } catch (Exception e) {
            }
        }

        if (p.getProtocol() == null) {
            p.setProtocol(TransferProtocol.LOCAL.value());
        }
        if (p.getPort() == null) {
            String[] arr = p.getHost().split(":");
            if (arr.length > 1) {
                p.setHost(arr[0]);
                p.setPort(getPort(argPrefix + "host=" + p.getHost(), arr[1]));
            } else {
                p.setPort(DEFAULT_PORT);
            }
        }
        if (p.getAccount() == null) {
            p.setAccount(Yade.DEFAULT_ACCOUNT);
        }
        return p;
    }

    private static String getHostname() {
        if (HOSTNAME == null) {
            String env = System.getenv(IS_WINDOWS ? "COMPUTERNAME" : "HOSTNAME");
            try {
                HOSTNAME = isEmpty(env) ? InetAddress.getLocalHost().getHostName() : env;
            } catch (Exception e) {
                HOSTNAME = DEFAULT_HOSTNAME;
            }
        }
        return HOSTNAME;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static void displayUsage() {
        String INDENT = "                                        ";

        System.out.println("Usage: file_transfer_history.sh|.cmd [Options] [Switches] ");

        System.out.println("");
        System.out.println("Options:");
        System.out.print("  --transfer-file=<transfer-spec>    | ");
        System.out.println("optional: the transfer specification can occur any number of times and is made up of the elements:");
        System.out.println(INDENT + "<source-file>,<target-file>[,<file-size>[,<error-message>]]");

        System.out.print("  --operation=<operation-spec>       | ");
        System.out.println("optional: operation to copy, to move, to remove or to get a list of files:");
        System.out.println(INDENT + "copy|move|remove|getlist, default: copy");

        System.out.print("  --source-account=<account>         | ");
        System.out.println("optional: account used for authentication with the system holding source files");
        System.out.print("  --source-protocol=<protocol-spec>  | ");
        System.out.println("optional: one of the protocols for access to source files:");
        System.out.println(INDENT + "local|ftp|ftps|sftp|ssh|http|https|webdav|webdavs|smb, default: local");
        System.out.print("  --source-host=<hostname>           | ");
        System.out.println("optional: hostname, IP address or URL of the host holding source files, default: localhost");
        System.out.print("  --source-port=<number>             | ");
        System.out.println("optional: port used to connect to the system holding source files, default: 0");

        System.out.print("  --target-account=<account>         | ");
        System.out.println("optional: account used for authentication with the system holding target files");
        System.out.print("  --target-protocol=<protocol-spec>  | ");
        System.out.println("optional: one of the protocols for access to target files:");
        System.out.println(INDENT + "local|ftp|ftps|sftp|ssh|http|https|webdav|webdavs|smb, default: local");
        System.out.print("  --target-host=<hostname>           | ");
        System.out.println("optional: hostname, IP address or URL of the host holding target files, default: localhost");
        System.out.print("  --target-port=<number>             | ");
        System.out.println("optional: port used to connect to the system holding target files, default: 0");

        System.out.print("  --start-time=<date-time>           | ");
        System.out.println("optional: ISO date and time for start time of transfer: yyyy-MM-dd hh:mm:ssZ");
        System.out.print("  --end-time=<date-time>             | ");
        System.out.println("optional: ISO date and time for end time of transfer: yyyy-MM-dd hh:mm:ssZ, default: current time");
        System.out.print("  --error=<string>                   | ");
        System.out.println("optional: error message indicating a failed file transfer");
        System.out.print("  --delimiter=<character>            | ");
        System.out.println("optional: delimiter character for entries in the transfer specification, default: comma");

        System.out.println("");
        System.out.println("Switches:");
        System.out.print("  -h | --help                        | ");
        System.out.println("displays usage");
        System.out.print("  --display-args                     | ");
        System.out.println("displays command line arguments");
        System.out.print("  --display-result                   | ");
        System.out.println("displays execution result");
        System.out.print("  --dry-run                          | ");
        System.out.println("sets --display-args and --display-result and does not create entries for the File Transfer History");
    }

    private static void displayArguments(String[] args) {
        System.out.println(String.format("[%s]Arguments(count=%s):", TransferHistory.class.getSimpleName(), args.length));
        int i = 0;
        for (String arg : args) {
            i++;
            System.out.println(String.format("  %s)%s", i, arg));
        }
    }

    private static void displayResult(YadeTransferResultSerializer<YadeTransferResult> serializer, String serialized) {
        try {
            YadeTransferResult r = serializer.deserialize(serialized);

            displayMeta(r);
            displayProtocol("Source:", r.getSource());
            displayProtocol("Target:", r.getTarget());
            displayEntries("Entries:", r.getEntries());

        } catch (Exception e) {
            System.err.println(String.format("[%s][can't deserialize result]%s", TransferHistory.class.getSimpleName(), e.toString()));
        }
    }

    private static void displayMeta(YadeTransferResult r) {
        List<String> l = new ArrayList<>();
        l.add("Operation=" + r.getOperation());
        l.add("Start Time(UTC)=" + r.getStart());
        l.add("End Time(UTC)=" + r.getEnd());
        if (r.getErrorMessage() != null) {
            l.add("Error=" + r.getErrorMessage());
        }
        System.out.println(String.format("[%s]Result: %s", TransferHistory.class.getSimpleName(), String.join(", ", l)));
    }

    private static void displayProtocol(String header, YadeTransferResultProtocol p) {
        if (p != null) {
            System.out.println(String.format("  %s Protocol=%s, Host=%s, Port=%s, Account=%s", header, p.getProtocol(), p.getHost(), p.getPort(), p
                    .getAccount()));
        }
    }

    private static void displayEntries(String header, List<YadeTransferResultEntry> entries) {
        if (entries != null && entries.size() > 0) {
            System.out.println("  " + header);
            int i = 0;
            for (YadeTransferResultEntry entry : entries) {
                i++;

                List<String> l = new ArrayList<>();
                l.add("Source=" + entry.getSource());
                if (entry.getTarget() != null) {
                    l.add("Target=" + entry.getTarget());
                }
                l.add("Size=" + entry.getSize());
                l.add("Status=" + entry.getState());
                if (!isEmpty(entry.getErrorMessage())) {
                    l.add("Error=" + entry.getErrorMessage());
                }
                System.out.println(String.format("    %s)%s", i, String.join(", ", l)));
            }
        }
    }

}
