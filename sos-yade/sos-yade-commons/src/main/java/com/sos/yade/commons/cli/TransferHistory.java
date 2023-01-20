package com.sos.yade.commons.cli;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final String ENV_VAR_RETURN_VALUES = "JS7_RETURN_VALUES";
    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final int DEFAULT_PORT = 0;

    private static final int EXIT_STATUS_SUCCESS = 0;
    private static final int EXIT_STATUS_ERROR = 0;

    private static String DELIMITER = ",";
    private static String HOSTNAME;
    private static List<String> PARSE_ERRORS;

    public static void main(String[] args) {
        int exitStatus = EXIT_STATUS_SUCCESS;
        boolean process = true;

        if (args.length == 0 || (args.length == 1 && (args[0].equals("") || args[0].equals("--help")))) {
            showUsage();
            process = false;
        }

        String returnValues = System.getenv(ENV_VAR_RETURN_VALUES);
        if (returnValues == null) {
            System.err.println("Missing environment variable: " + ENV_VAR_RETURN_VALUES);
            exitStatus = EXIT_STATUS_ERROR;
            process = false;
        }

        if (process) {
            exitStatus = process(returnValues, args);
        }

        System.exit(exitStatus);
    }

    private static void showUsage() {
        String INDENT = "                                        ";

        System.out.println("Usage: [Meta options...] [Source options...] [Target options...] [Transfer files options...] ");

        // TRANSFER META
        System.out.println(" Meta options:");
        System.out.println("  --operation=<operation-spec>        Optional, Case insensitive");
        System.out.println(INDENT + "Default: copy");
        System.out.println(INDENT + "Valid values: copy, move, getlist, remove");
        System.out.println("  --start-time=<date-time>            Optional, ISO datetime for start time of transfer");
        System.out.println("  --end-time=<date-time>              Optional, ISO datetime for end time of transfer");
        System.out.println(INDENT + "Default: current datetime");
        System.out.println(INDENT + "Datetime formats: yyyy-MM-dd HH:mm:ss, yyyy-MM-dd'T'HH:mm:ssZ");
        System.out.println(INDENT + "Examples:");
        System.out.println(INDENT + "  2023-01-01 12:00:00");
        System.out.println(INDENT + "  2023-01-01 12:00:00+0100");
        System.out.println(INDENT + "  2023-01-01T12:00:00+0100");
        System.out.println("  --error=<string>                    Optional, Transfer error");
        System.out.println(INDENT + "Specifies the error message and sets the whole transfer as failed");

        // SOURCE/TARGET META
        System.out.println(" Source/Target options:");
        System.out.println("  --[source/target]-protocol=<protocol-spec>    Optional, Case insensitive");
        System.out.println(INDENT + "Default: local");
        System.out.println(INDENT + "Valid values: local, ftp, ftps, sftp, ssh, http, https, webdav, webdavs, smb");
        System.out.println(INDENT + "See the --[source/target]-host option.");
        System.out.print("  --[source/target]-account=<account>");
        System.out.println("  Optional, Account for authentication at one of the systems involved in file transfer");
        System.out.println(INDENT + "Default: .");
        System.out.println(INDENT + "See the --[source/target]-host option.");
        System.out.println("  --[source/target]-port=<number>     Optional, Port on which the connection should be established");
        System.out.println(INDENT + "Default: 0");
        System.out.println(INDENT + "See the --[source/target]-host option.");
        System.out.print("  --[source/target]-host=<hostname-spec>");
        System.out.println("   Optional, Specifies the hostname or IP address of the server to which a connection has to be made");
        System.out.println(INDENT + "Default: Hostname of the machine that the File Transfer History script being run on");
        System.out.println(INDENT + "Possible values:");
        System.out.println(INDENT + "  somehost:80");
        System.out.println(INDENT + "    Specifies the Hostname and the Port (when the corresponding Port option is not set)");
        System.out.println(INDENT + "  ftp://test_user@somehost:21");
        System.out.println(INDENT + "    Specifies the Hostname, Protocol, Account and the Port when the corresponding options are not set");
        System.out.println(INDENT + "    Expects a valid Java URL value");

        // TRANSFER ENTRIES
        System.out.println(" Transfer files options:");
        System.out.println("  --delimiter=<character>             Optional, Specifies the delimiter for entries in the transfer specification");
        System.out.println(INDENT + "Default: comma");
        System.out.println("  --transfer-file=<transfer-spec>     Optional, Specifies a single file transfer");
        System.out.println(INDENT + "Multiple values option: values are separated by --delimiter");
        System.out.println(INDENT + "  <source-file>[--delimiter<target-file>[--delimiter<file-size(in bytes)>][--delimiter<error-message>]]");
        System.out.println(INDENT + "Examples:");
        System.out.println(INDENT + "  /home/sos/source_file.txt");
        System.out.println(INDENT + "  /home/sos/source_file.txt,/home/sos/target_file.txt");
        System.out.println(INDENT + "  /home/sos/source_file.txt,/home/sos/target_file.txt,100");
        System.out.println(INDENT + "  /home/sos/source_file.txt,/home/sos/target_file.txt,100,Permission denied");
        System.out.println(INDENT + "  /home/sos/source_file.txt,/home/sos/target_file.txt,Permission denied");
    }

    private static int process(String returnValues, String[] args) {
        PARSE_ERRORS = new ArrayList<>();

        YadeTransferResult result = new YadeTransferResult();
        YadeTransferResultProtocol source = new YadeTransferResultProtocol();
        YadeTransferResultProtocol target = new YadeTransferResultProtocol();
        List<String> entries = new ArrayList<>();

        boolean hasTarget = false;
        for (String arg : args) {
            String[] arr = arg.split("=");
            String pn = arr[0];
            String pv = arr.length > 1 ? arr[1].trim() : "";

            switch (pn) {
            // TRANSFER META
            case "--operation":
                result.setOperation(getOperation(arg, pv));
                break;
            case "--start-time":
                result.setStart(toInstant(arg, pv));
                break;
            case "--end-time":
                result.setEnd(toInstant(arg, pv));
                break;
            case "--error":
                result.setErrorMessage(isEmpty(pv) ? null : pv);
                break;

            // SOURCE META
            case "--source-protocol":
                source.setProtocol(getProtocol(arg, pv));
                break;
            case "--source-account":
                source.setAccount(getAccount(pv));
                break;
            case "--source-host":
                source.setHost(pv);
                break;
            case "--source-port":
                source.setPort(getPort(arg, pv));
                break;

            // TARGET META
            case "--target-protocol":
                target.setProtocol(getProtocol(arg, pv));
                hasTarget = true;
                break;
            case "--target-account":
                target.setAccount(getAccount(pv));
                hasTarget = true;
                break;
            case "--target-host":
                target.setHost(pv);
                hasTarget = true;
                break;
            case "--target-port":
                target.setPort(getPort(arg, pv));
                hasTarget = true;
                break;

            // TRANSFER ENTRIES
            case "--delimiter":
                if (!isEmpty(pv)) {
                    DELIMITER = pv;
                }
                break;

            case "--transfer-file": // multiple times
                if (!isEmpty(pv)) {
                    entries.add(pv);
                }
                break;
            }
        }

        if (PARSE_ERRORS.size() > 0) {
            System.out.println(String.format("[%s][invalid values]%s", TransferHistory.class.getSimpleName(), String.join(", ", PARSE_ERRORS)));
        }

        try {
            serialize2File(Paths.get(returnValues), complete(result, source, hasTarget ? target : null, entries));
            return EXIT_STATUS_SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EXIT_STATUS_ERROR;
        }
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
        } catch (Throwable e) {
            PARSE_ERRORS.add("[" + arg + "]set datetime to now");
            return null;
        }
    }

    private static String getOperation(String arg, String val) {
        // return TransferOperation.fromValue(val).value();
        try {
            return TransferOperation.valueOf(val.toUpperCase()).value();
        } catch (Throwable e) {
            PARSE_ERRORS.add("[" + arg + "]set operation to UNKNOWN");
            return TransferOperation.UNKNOWN.value();
        }
    }

    private static String getProtocol(String arg, String val) {
        // return TransferProtocol.fromValue(val).value();
        try {
            return TransferProtocol.valueOf(val.toUpperCase()).value();
        } catch (Throwable e) {
            PARSE_ERRORS.add("[" + arg + "]set protocol to UNKNOWN");
            return TransferProtocol.UNKNOWN.value();
        }
    }

    private static int getPort(String arg, String val) {
        try {
            return Integer.parseInt(val);
        } catch (Throwable e) {
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
            } catch (Throwable e) {
                entry.setErrorMessage(arr[2]);
            }
            break;
        case 4:
            entry.setTarget(arr[1]);
            try {
                entry.setSize(Long.parseLong(arr[2]));
                entry.setErrorMessage(arr[3]);
            } catch (Throwable e) {
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
            } catch (Throwable e) {
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
            } catch (UnknownHostException e) {
                HOSTNAME = DEFAULT_HOSTNAME;
            }
        }
        return HOSTNAME;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static void serialize2File(Path file, YadeTransferResult result) throws Exception {
        YadeTransferResultSerializer<YadeTransferResult> serializer = new YadeTransferResultSerializer<>();
        Files.write(file, new StringBuilder(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES).append("=").append(serializer.serialize(result)).toString()
                .getBytes());
    }
}
