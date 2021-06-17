package com.sos.commons.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateSQLCommandExtractorException;
import com.sos.commons.util.SOSString;

public class SOSSQLCommandExtractor {

    private final static Logger LOGGER = LoggerFactory.getLogger(SOSSQLCommandExtractor.class);

    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final String REPLACE_BACKSLASH = "\\\\'";
    private static final String REPLACE_DOUBLE_APOSTROPHE = "''";
    private static final String REPLACEMENT_BACKSLASH = "XxxxX";
    private static final String REPLACEMENT_DOUBLE_APOSTROPHE = "YyyyY";

    private String beginProcedure = "";
    private final Enum<SOSHibernateFactory.Dbms> dbms;
    private int majorVersion = -1;
    private int minorVersion = -1;

    public SOSSQLCommandExtractor(Enum<SOSHibernateFactory.Dbms> dbms) {
        this.dbms = dbms;
    }

    public List<String> extractCommands(String content) throws SOSHibernateSQLCommandExtractorException {
        String method = "extractCommands";
        if (SOSString.isEmpty(content)) {
            throw new SOSHibernateSQLCommandExtractorException("content is empty");
        }
        if (isTraceEnabled) {
            LOGGER.trace(String.format("[%s][content]%s", method, content));
        }
        if (SOSHibernateFactory.Dbms.H2.equals(dbms) && content.startsWith("DROP ALIAS")) {// TODO
            return extractH2Aliases(content);
        }

        List<String> commands = new ArrayList<String>();
        Preparer preparer = new Preparer(dbms, majorVersion, minorVersion, content);
        preparer.prepare();

        for (String cmd : preparer.getCommands()) {
            if (cmd == null || cmd.trim().isEmpty()) {
                continue;
            }
            String command = cmd.trim();

            if (endsWithEnd(command)) {
                if (isProcedureSyntax(command)) {
                    commands.add(command + preparer.getCommandCloser());
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("[%s][command]%s%s", method, command, preparer.getCommandCloser()));
                    }
                } else {
                    split(commands, replace(command), null, preparer.getCommandCloser(), true, 0);
                    if (!"".equals(beginProcedure)) {
                        int posBeginProcedure = command.indexOf(beginProcedure);
                        String subCommand = command.substring(posBeginProcedure);
                        commands.add(subCommand + preparer.getCommandCloser());
                        if (isTraceEnabled) {
                            LOGGER.trace(String.format("[%s][command]%s%s", method, subCommand, preparer.getCommandCloser()));
                        }
                    }
                }
            } else {
                String end = preparer.addCommandCloser() ? preparer.getCommandCloser() : "";
                split(commands, replace(command), null, end, false, 0);
            }

        }
        return commands;
    }

    // DROP ALIAS IF EXISTS SOS_XXX;
    // CREATE ALIAS SOS_XXX AS $$
    // ...
    private List<String> extractH2Aliases(String content) {
        Scanner s = new Scanner(content);
        List<String> commands = new ArrayList<String>();
        StringBuilder sb = null;
        String newLine = "\n";
        try {
            s.useDelimiter("(\r\n|\n)");
            while (s.hasNext()) {
                String line = s.next().trim();
                if (line.startsWith("DROP ALIAS")) { // TODO
                    handleH2Command(commands, sb);
                    sb = null;

                    commands.add(line.trim().substring(0, line.length() - 1));
                } else if (line.startsWith("CREATE ALIAS")) { // TODO
                    handleH2Command(commands, sb);

                    sb = new StringBuilder(line).append(newLine);
                } else if (line.startsWith("$$;")) {
                    if (sb != null) {
                        commands.add(sb.append("$$").toString());
                        sb = null;
                    }
                } else {
                    if (sb != null) {
                        sb.append(line).append(newLine);
                    }
                }
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (s != null) {
                s.close();
            }
        }
        handleH2Command(commands, sb);
        return commands;
    }

    private void handleH2Command(List<String> commands, StringBuilder sb) {
        if (sb != null && sb.length() > 0) {
            String command = sb.toString().trim();
            commands.add(command.endsWith(";") ? command.substring(0, command.length() - 1) : command);
        }
    }

    private boolean endsWithEnd(String statement) {
        // END END; END$$; END MY_PROCEDURE;
        String patterns = "end[\\s]*[\\S]*[;]*$";
        Pattern p = Pattern.compile(patterns, Pattern.CASE_INSENSITIVE);

        Matcher matcher = p.matcher(statement);
        return matcher.find();
    }

    private boolean isProcedureSyntax(String command) throws SOSHibernateSQLCommandExtractorException {
        if (command == null) {
            throw new SOSHibernateSQLCommandExtractorException("command is empty");
        }
        command = command.toLowerCase().trim();
        if (command.startsWith("procedure") || command.startsWith("function") || command.startsWith("declare") || command.startsWith("begin")) {
            return true;
        }
        StringBuilder patterns = new StringBuilder();
        patterns.append("^(re)?create+[\\s]*procedure");
        patterns.append("|^create+[\\s]*function");
        patterns.append("|^create+[\\s]*operator");
        patterns.append("|^create+[\\s]*package");
        patterns.append("|^create+[\\s]*trigger");
        patterns.append("|^drop+[\\s]*function");
        patterns.append("|^drop+[\\s]*operator");
        patterns.append("|^drop+[\\s]*package");
        patterns.append("|^drop+[\\s]*procedure");
        patterns.append("|^drop+[\\s]*trigger");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*procedure");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*function");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*package");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*operator");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*trigger");
        Pattern p = Pattern.compile(patterns.toString());
        Matcher matcher = p.matcher(command);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    private StringBuilder replace(String value) {
        String s = value.replaceAll(REPLACE_BACKSLASH, REPLACEMENT_BACKSLASH);
        s = s.replaceAll(REPLACE_DOUBLE_APOSTROPHE, REPLACEMENT_DOUBLE_APOSTROPHE);
        return new StringBuilder(s.trim());
    }

    private void split(final List<String> commands, final StringBuilder st, final Integer position, final String procedurEnd,
            final boolean returnProcedureBegin, int count) throws SOSHibernateSQLCommandExtractorException {
        String method = "split";

        beginProcedure = "";
        count += 1;
        StringBuilder sub;
        int semicolon = -1;
        int apostropheFirst = -1;
        if (position == null) {
            semicolon = st.indexOf(";");
            apostropheFirst = st.indexOf("'");
        } else {
            semicolon = st.indexOf(";", position.intValue());
            apostropheFirst = st.indexOf("'", position.intValue());
        }
        if (apostropheFirst > semicolon || apostropheFirst == -1) {
            String value = "";
            if (semicolon == -1) {
                value = st.toString().trim();
            } else {
                value = st.toString().substring(0, semicolon).trim();
            }
            value = value.replaceAll(REPLACEMENT_BACKSLASH, REPLACE_BACKSLASH);
            value = value.replaceAll(REPLACEMENT_DOUBLE_APOSTROPHE, REPLACE_DOUBLE_APOSTROPHE);
            if (isProcedureSyntax(value)) {
                if (returnProcedureBegin) {
                    beginProcedure = value;
                    return;
                } else if (!"".equals(procedurEnd)) {
                    value += procedurEnd;
                }
            }
            if (!"".equals(value)) {
                commands.add(value);
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("[%s][command]%s", method, value));
                }
            }
            if (semicolon != -1) {
                sub = new StringBuilder(st.substring(semicolon + 1));
                if (sub != null && sub.length() != 0) {
                    split(commands, sub, null, procedurEnd, returnProcedureBegin, count);
                }
            }
        } else {
            int apostropheSecond = st.indexOf("'", apostropheFirst + 1);
            if (apostropheSecond != -1) {
                split(commands, st, Integer.valueOf(apostropheSecond + 1), procedurEnd, returnProcedureBegin, count);
            } else {
                throw new SOSHibernateSQLCommandExtractorException(String.format("closing apostrophe not found = %s = %s ", apostropheFirst, st));
            }
        }
    }

    public class Preparer {

        private boolean addCommandCloser;
        private String commandCloser;
        private String[] commands;
        private String commandSpltter;

        private final String content;
        private final Enum<SOSHibernateFactory.Dbms> dbms;
        private final int majorVersion;
        private final int minorVersion;

        public Preparer(Enum<SOSHibernateFactory.Dbms> dbms, int majorVersion, int minorVersion, String content) {
            this.dbms = dbms;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.content = content;

        }

        public boolean addCommandCloser() {
            return addCommandCloser;
        }

        public String getCommandCloser() {
            return commandCloser;
        }

        public String[] getCommands() {
            return commands;
        }

        public String getCommandSplitter() {
            return commandSpltter;
        }

        public void prepare() throws SOSHibernateSQLCommandExtractorException {
            String content = init();
            content = stripComments(content);
            commands = content.split(commandSpltter);
        }

        private String init() throws SOSHibernateSQLCommandExtractorException {
            String method = "init";
            commandCloser = "";
            addCommandCloser = true;

            // sb.replaceAll(":=","\\\\:=")) to avoid hibernate
            // "Space is not allowed after parameter prefix ':'" Exception
            // e.g. Oracle: myVar := SYSDATE;
            StringBuilder sb = new StringBuilder(content.replaceAll("\r\n", "\n").replaceAll("\\;[ \\t]", ";"));
            if (dbms.equals(SOSHibernateFactory.Dbms.MSSQL)) {
                commandSpltter = "(?i)\nGO\\s*\n|\n/\n";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.MYSQL)) {
                commandSpltter = "\n\\\\g\n";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.H2)) {
                commandSpltter = "\n\\\\g\n";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.ORACLE)) {
                commandSpltter = "\n/\n";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.PGSQL)) {
                commandSpltter = "\\$\\${1}[\\s]+(LANGUAGE|language){1}[\\s]+(plpgsql|PLPGSQL){1}[\\s]*;";
                commandCloser = "$$ LANGUAGE plpgsql;";
                addCommandCloser = false;
            } else if (dbms.equals(SOSHibernateFactory.Dbms.DB2)) {
                commandSpltter = "\n@\n";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.SYBASE)) {
                commandSpltter = "\ngo\n";
            } else if (dbms.equals(SOSHibernateFactory.Dbms.FBSQL)) {
                StringBuilder patterns = new StringBuilder("set+[\\s]*term[inator]*[\\s]*(.*);");
                Pattern p = Pattern.compile(patterns.toString());
                Matcher matcher = p.matcher(content.toString().toLowerCase().trim());
                if (matcher.find()) {
                    commandSpltter = "\\" + matcher.group(1);
                    String ct = content.replaceAll("(?i)set+[\\s]*term[inator]*[\\s]*.*\\n", "");
                    sb.delete(0, sb.length());
                    sb.append(ct);
                } else {
                    commandSpltter = "\n/\n";
                }
            } else {
                throw new SOSHibernateSQLCommandExtractorException(String.format("unsupported dbms=%s", dbms));
            }

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s]commandCloser=%s, commandSpltter=; or %s", method, commandCloser, commandSpltter));
            }

            return sb.toString();
        }

        private String stripComments(String content) throws SOSHibernateSQLCommandExtractorException {
            String method = "stripComments";
            StringBuilder sb = new StringBuilder();
            StringTokenizer st = new StringTokenizer(content, "\n");
            boolean addRow = true;
            boolean isVersionComment = false;
            boolean isMySQL = dbms.equals(SOSHibernateFactory.Dbms.MYSQL);
            while (st.hasMoreTokens()) {
                String row = st.nextToken().trim();
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (row.startsWith("--") || row.startsWith("//") || row.startsWith("#")) {
                    continue;
                }
                row = row.replaceAll("^[/][*](?s).*?[*][/][\\s]*;*", "");
                if (row.isEmpty()) {
                    continue;
                }
                if (isMySQL) {
                    String rowUpper = row.toUpperCase();
                    if (rowUpper.startsWith("DELIMITER")) {
                        continue;
                    } else if (rowUpper.startsWith("END$$;")) {
                        row = "END;";
                    }
                }
                if (row.startsWith("/*!")) {
                    String[] rowArr = row.substring(3).trim().split(" ");
                    if (rowArr[0].length() == 5 || rowArr[0].length() == 6) {
                        String version = rowArr[0].length() == 5 ? "0" + rowArr[0] : rowArr[0];
                        try {
                            int major = Integer.parseInt(version.substring(0, 2));
                            if (majorVersion >= major) {
                                if (isTraceEnabled) {
                                    LOGGER.trace(String.format("[%s][use]db major version=%s >= comment major version=%s", method, majorVersion,
                                            major));
                                }
                                int minor = Integer.parseInt(version.substring(2, 4));
                                if (minorVersion >= minor) {
                                    isVersionComment = true;
                                    if (isTraceEnabled) {
                                        LOGGER.trace(String.format("[%s][use]db minor version=%s >= comment minor version=%s", method, minorVersion,
                                                minor));
                                    }
                                } else {
                                    if (isTraceEnabled) {
                                        LOGGER.trace(String.format("[%s][skip]db minor version=%s < comment minor version=%s", method, minorVersion,
                                                minor));
                                    }
                                }
                            } else {
                                if (isTraceEnabled) {
                                    LOGGER.trace(String.format("[%s][skip]db major version=%s < comment major version=%s", method, majorVersion,
                                            major));
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.warn(String.format(
                                    "[%s][skip]no numerical major/minor version in comment=%s (database major version=%s, minor version=%s", method,
                                    version, majorVersion, minorVersion));
                        }
                    } else {
                        LOGGER.warn(String.format("[%s][skip]invalid comment major version length=%s (database major version=%s)", method, rowArr[0],
                                majorVersion));
                    }
                    if (!isVersionComment) {
                        addRow = false;
                    }
                    continue;
                } else if (row.startsWith("/*")) {
                    addRow = false;
                    continue;
                }
                if (row.endsWith("*/") || row.endsWith("*/;")) {
                    if (isVersionComment) {
                        if (!addRow) {
                            addRow = true;
                        } else {
                            isVersionComment = false;
                        }
                        continue;
                    }
                    if (!addRow) {
                        addRow = true;
                        continue;
                    }
                }
                if (!addRow) {
                    continue;
                }
                sb.append(row + "\n");
            }
            return sb.toString();
        }
    }

}
