package com.sos.yade.engine.commons.arguments.loaders.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSComparisonOperator;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.commons.xml.SOSXML;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.commons.arguments.YADESourcePollingArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;

public class YADEXMLProfileHelper {

    public static final String ELEMENT_NAME_NOTIFICATION_TRIGGERS = "NotificationTriggers";

    protected static void parse(YADEXMLArgumentsLoader argsLoader, Node profile) throws Exception {
        NodeList nl = profile.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Operation":
                    parseOperation(argsLoader, n);
                    break;
                case "SystemPropertyFiles":
                    YADEXMLGeneralHelper.parseSystemPropertyFiles(argsLoader, n);
                    break;
                case ELEMENT_NAME_NOTIFICATION_TRIGGERS:
                    parseNotificationTriggers(argsLoader, n, "Profile=" + argsLoader.getArgs().getProfile().getValue());
                    break;
                case "Notifications":
                    YADEXMLGeneralHelper.parseNotifications(argsLoader, n, "Profile=" + argsLoader.getArgs().getProfile().getValue());
                    break;
                case "Client":
                case "JobScheduler":
                default:
                    break;
                }
            }
        }
    }

    private static void parseOperation(YADEXMLArgumentsLoader argsLoader, Node operation) throws Exception {
        Node node = argsLoader.getXPath().selectNode(operation, "*[1]"); // first child
        if (node == null) {
            throw new SOSMissingDataException("Profiles/Profile profile_id=" + argsLoader.getArgs().getProfile().getValue()
                    + "/Operation/<Child Node>");
        }
        String operationIdentifier = node.getNodeName();
        switch (operationIdentifier) {
        case "Copy":
            argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
            parseOperationOnSourceTarget(argsLoader, node, operationIdentifier);
            break;
        case "Move":
            argsLoader.getArgs().getOperation().setValue(TransferOperation.MOVE);
            parseOperationOnSourceTarget(argsLoader, node, operationIdentifier);
            break;
        case "Remove":
            argsLoader.getArgs().getOperation().setValue(TransferOperation.REMOVE);
            argsLoader.nullifyTargetArgs();
            parseOperationOnSource(argsLoader, node, operationIdentifier);
            break;
        case "GetList":
            argsLoader.getArgs().getOperation().setValue(TransferOperation.GETLIST);
            argsLoader.nullifyTargetArgs();
            parseOperationOnSource(argsLoader, node, operationIdentifier);
            break;
        default:
            throw new Exception("[" + node.getNodeName() + "]Unknown Operation");
        }
    }

    private static void parseNotificationTriggers(YADEXMLArgumentsLoader argsLoader, Node notificationTriggers, String parentInfo) throws Exception {
        argsLoader.initializeNotificationArgsIfNull();

        NodeList nl = notificationTriggers.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "OnSuccess":
                    parseNotificationTrigger(argsLoader, n, parentInfo);
                    argsLoader.getNotificationArgs().getOnSuccess().setValue(true);
                    break;
                case "OnError":
                    parseNotificationTrigger(argsLoader, n, parentInfo);
                    argsLoader.getNotificationArgs().getOnError().setValue(true);
                    break;
                case "OnEmptyFiles":
                    parseNotificationTrigger(argsLoader, n, parentInfo);
                    argsLoader.getNotificationArgs().getOnEmptyFiles().setValue(true);
                    break;
                }
            }
        }
    }

    private static void parseNotificationTrigger(YADEXMLArgumentsLoader argsLoader, Node notificationTrigger, String parentInfo) throws Exception {
        Node ref = SOSXML.getChildNode(notificationTrigger, "MailFragmentRef");
        if (ref == null) {
            return;
        }

        String exp = "Fragments/NotificationFragments/MailFragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node fragment = argsLoader.getXPath().selectNode(argsLoader.getRoot(), exp);
        if (fragment == null) {
            throw new SOSMissingDataException("[" + parentInfo + "][" + exp + "]referenced MailFragment not found");
        }
        NodeList nl = fragment.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "Header":
                        parseMailFragmentHeader(argsLoader, n);
                        break;
                    case "Attachment":
                        argsLoader.setListPathArgumentValue(argsLoader.getNotificationArgs().getMail().getAttachment(), n);
                        break;
                    case "Body":
                        argsLoader.setStringArgumentValue(argsLoader.getNotificationArgs().getMail().getBody(), n);
                        break;
                    case "ContentType":
                        argsLoader.setStringArgumentValue(argsLoader.getNotificationArgs().getMail().getContentType(), n);
                        break;
                    case "Encoding":
                        argsLoader.setStringArgumentValue(argsLoader.getNotificationArgs().getMail().getEncoding(), n);
                        break;
                    }
                }
            }
        }
    }

    private static void parseMailFragmentHeader(YADEXMLArgumentsLoader argsLoader, Node header) {
        NodeList nl = header.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "From":
                        argsLoader.setStringArgumentValue(argsLoader.getNotificationArgs().getMail().getHeaderFrom(), n);
                        break;
                    case "To":
                        argsLoader.setListStringArgumentValue(argsLoader.getNotificationArgs().getMail().getHeaderTo(), n);
                        break;
                    case "CC":
                        argsLoader.setListStringArgumentValue(argsLoader.getNotificationArgs().getMail().getHeaderCC(), n);
                        break;
                    case "BCC":
                        argsLoader.setListStringArgumentValue(argsLoader.getNotificationArgs().getMail().getHeaderBCC(), n);
                        break;
                    case "Subject":
                        argsLoader.setStringArgumentValue(argsLoader.getNotificationArgs().getMail().getHeaderSubject(), n);
                        break;
                    }
                }
            }
        }
    }

    private static void parseOperationOnSourceTarget(YADEXMLArgumentsLoader argsLoader, Node operation, String operationIdentifier) throws Exception {
        argsLoader.initializeTargetArgsIfNull();

        NodeList nl = operation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                if (nodeName.equals(operationIdentifier + "Source")) {
                    parseSource(argsLoader, n, operationIdentifier);
                } else if (nodeName.equals(operationIdentifier + "Target")) {
                    parseTarget(argsLoader, n, operationIdentifier);
                } else if (nodeName.equals("TransferOptions")) {
                    parseTransferOptions(argsLoader, n);
                }
            }
        }
    }

    private static void parseOperationOnSource(YADEXMLArgumentsLoader argsLoader, Node operation, String operationIdentifier) throws Exception {
        NodeList nl = operation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals(operationIdentifier + "Source")) {
                    parseSource(argsLoader, n, operationIdentifier);
                }
            }
        }
    }

    private static void parseSource(YADEXMLArgumentsLoader argsLoader, Node sourceOperation, String operationIdentifier) throws Exception {
        NodeList nl = sourceOperation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                // e.g. CopySourceFragmentRef
                if (nodeName.equals(operationIdentifier + "SourceFragmentRef")) {
                    parseFragmentRef(argsLoader, n, true);
                } else if (nodeName.equals("Alternative" + operationIdentifier + "SourceFragmentRef")) {
                    parseAlternativeFragmentRef(argsLoader, n, true);
                } else if (nodeName.equals("SourceFileOptions")) {
                    parseSourceOptions(argsLoader, n);
                }
            }
        }
    }

    private static void parseTarget(YADEXMLArgumentsLoader argsLoader, Node targetOperation, String operationIdentifier) throws Exception {
        NodeList nl = targetOperation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                // e.g. CopyTargetFragmentRef
                if (nodeName.equals(operationIdentifier + "TargetFragmentRef")) {
                    parseFragmentRef(argsLoader, n, false);
                } else if (nodeName.equals("Alternative" + operationIdentifier + "TargetFragmentRef")) {
                    parseAlternativeFragmentRef(argsLoader, n, false);
                } else if (nodeName.equals("Directory")) {
                    argsLoader.setStringArgumentValue(argsLoader.getTargetArgs().getDirectory(), n);
                } else if (nodeName.equals("TargetFileOptions")) {
                    parseTargetOptions(argsLoader, n);
                }
            }
        }
    }

    private static void parseTransferOptions(YADEXMLArgumentsLoader argsLoader, Node transferOptions) throws Exception {
        NodeList nl = transferOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BufferSize":
                    argsLoader.setIntegerArgumentValue(argsLoader.getArgs().getBufferSize(), n);
                    break;
                case "Transactional":
                    argsLoader.setBooleanArgumentValue(argsLoader.getArgs().getTransactional(), n);
                    break;
                case "RetryOnConnectionError":
                    YADEXMLGeneralHelper.parseRetryOnConnectionError(argsLoader, n);
                    break;
                }
            }
        }
    }

    private static void parseAlternativeFragmentRef(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        // YADEEngine - not argsLoaderemented
    }

    private static void parseFragmentRef(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        YADESourceTargetArguments sourceTargetArgs;
        if (isSource) {
            sourceTargetArgs = argsLoader.getSourceArgs();
        } else {
            sourceTargetArgs = argsLoader.getTargetArgs();
        }

        AProviderArguments providerArgs = null;
        // e.g. ref=CopySourceFragmentRef, refRef SFTPFragmentRef
        Node refRef = argsLoader.getXPath().selectNode(ref, "*[1]"); // first child
        if (refRef == null) {
            throw new SOSMissingDataException("Profiles/Profile profile_id=" + argsLoader.getArgs().getProfile().getValue() + "/../" + ref
                    .getNodeName() + "/<Child Node>");
        }
        switch (refRef.getNodeName()) {
        case "LocalSource":
            providerArgs = parseFragmentRefLocal(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "LocalTarget":
            providerArgs = parseFragmentRefLocal(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "SFTPFragmentRef":
            providerArgs = parseFragmentRefSFTP(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "FTPFragmentRef":
            providerArgs = parseFragmentRefFTP(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "FTPSFragmentRef":
            providerArgs = parseFragmentRefFTPS(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "HTTPFragmentRef":
            providerArgs = parseFragmentRefHTTP(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "HTTPSFragmentRef":
            providerArgs = parseFragmentRefHTTPS(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "SMBFragmentRef":
            providerArgs = parseFragmentRefSMB(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        case "WebDAVFragmentRef":
            providerArgs = parseFragmentRefWebDAV(argsLoader, refRef, isSource, sourceTargetArgs);
            break;
        }
        if (isSource) {
            argsLoader.getSourceArgs().setProvider(providerArgs);
        } else {
            argsLoader.getTargetArgs().setProvider(providerArgs);
        }
    }

    private static LocalProviderArguments parseFragmentRefLocal(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        LocalProviderArguments args = new LocalProviderArguments();
        args.applyDefaultIfNullQuietly();

        // Label example:
        // – A label has been set internally for a JumpHost YADE execution - see YADEXMLJumpHostSettingsWriter
        // – If set, the JumpHost YADE Client uses this label (e.g., Jump) for the Provider logging instead of Source/Target
        String label = SOSXML.getAttributeValue(ref, YADEXMLArgumentsLoader.INTERNAL_ATTRIBUTE_LABEL);
        if (!SOSString.isEmpty(label)) {
            if (isSource) {
                argsLoader.getSourceArgs().getLabel().setValue(label);
            } else {
                argsLoader.getTargetArgs().getLabel().setValue(label);
            }
        }

        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "LocalPreProcessing":
                    parsePreProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "LocalPostProcessing":
                    parsePostProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "ProcessingCommandDelimiter":
                    argsLoader.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, args);
                    break;
                }
            }
        }
        return args;
    }

    private static FTPProviderArguments parseFragmentRefFTP(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        FTPProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseFTP(argsLoader, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FTPPreProcessing":
                    parsePreProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "FTPPostProcessing":
                    parsePostProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "ProcessingCommandDelimiter":
                    argsLoader.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static FTPSProviderArguments parseFragmentRefFTPS(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        FTPSProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseFTPS(argsLoader, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FTPPreProcessing":
                    parsePreProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "FTPPostProcessing":
                    parsePostProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "ProcessingCommandDelimiter":
                    argsLoader.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static HTTPProviderArguments parseFragmentRefHTTP(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        HTTPProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseHTTP(argsLoader, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                case "HTTPHeaders":
                    YADEXMLFragmentsProtocolFragmentHelper.parseHTTPHeaders(argsLoader, args, n);
                    break;
                }
            }
        }
        return args;
    }

    private static HTTPSProviderArguments parseFragmentRefHTTPS(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        HTTPSProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseHTTPS(argsLoader, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                case "HTTPHeaders":
                    YADEXMLFragmentsProtocolFragmentHelper.parseHTTPHeaders(argsLoader, args, n);
                    break;
                }
            }
        }
        return args;
    }

    private static SSHProviderArguments parseFragmentRefSFTP(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        SSHProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseSFTP(argsLoader, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SFTPPreProcessing":
                    parsePreProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "SFTPPostProcessing":
                    parsePostProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "ProcessingCommandDelimiter":
                    argsLoader.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                case "ZlibCompression":
                    args.getUseZlibCompression().setValue(Boolean.valueOf(true));
                    break;
                }
            }
        }
        return args;
    }

    private static SMBProviderArguments parseFragmentRefSMB(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        SMBProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseSMB(argsLoader, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SMBPreProcessing":
                    parsePreProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "SMBPostProcessing":
                    parsePostProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "ProcessingCommandDelimiter":
                    argsLoader.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static WebDAVProviderArguments parseFragmentRefWebDAV(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        WebDAVProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseWebDAV(argsLoader, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "WebDAVPostProcessing":
                    parsePostProcessing(argsLoader, sourceTargetArgs.getCommands(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsLoader, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static void parseSourceOptions(YADEXMLArgumentsLoader argsLoader, Node sourceFileOptions) throws Exception {
        NodeList nl = sourceFileOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Selection":
                    parseSourceOptionSelection(argsLoader, n);
                    break;
                case "CheckSteadyState":
                    parseSourceOptionCheckSteadyState(argsLoader, n);
                    break;
                case "Directives":
                    parseSourceOptionDirectives(argsLoader, n);
                    break;
                case "Polling":
                    parseSourceOptionPolling(argsLoader, n);
                    break;
                case "ResultSet":
                    parseSourceOptionResultSet(argsLoader, n);
                    break;
                case "MaxFiles":
                    argsLoader.setIntegerArgumentValue(argsLoader.getSourceArgs().getMaxFiles(), n);
                    break;
                case "CheckIntegrityHash":
                    parseSourceOptionCheckIntegrityHash(argsLoader, n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelection(YADEXMLArgumentsLoader argsLoader, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FilePathSelection":
                    parseSourceOptionSelectionFilePath(argsLoader, n);
                    break;
                case "FileSpecSelection":
                    parseSourceOptionSelectionFileSpec(argsLoader, n);
                    break;
                case "FileListSelection":
                    parseSourceOptionSelectionFileList(argsLoader, n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFilePath(YADEXMLArgumentsLoader argsLoader, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FilePath":
                    argsLoader.getSourceArgs().setFilePath(argsLoader.getValue(n));
                    break;
                case "Directory":
                    argsLoader.setStringArgumentValue(argsLoader.getSourceArgs().getDirectory(), n);
                    break;
                case "Recursive":
                    argsLoader.setBooleanArgumentValue(argsLoader.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFileSpec(YADEXMLArgumentsLoader argsLoader, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FileSpec":
                    argsLoader.setStringArgumentValue(argsLoader.getSourceArgs().getFileSpec(), n);
                    break;
                case "Directory":
                    argsLoader.setStringArgumentValue(argsLoader.getSourceArgs().getDirectory(), n);
                    break;
                case "ExcludedDirectories":
                    argsLoader.setStringArgumentValue(argsLoader.getSourceArgs().getExcludedDirectories(), n);
                    break;
                case "Recursive":
                    argsLoader.setBooleanArgumentValue(argsLoader.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFileList(YADEXMLArgumentsLoader argsLoader, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FileList":
                    argsLoader.setPathArgumentValue(argsLoader.getSourceArgs().getFileList(), n);
                    break;
                case "Directory":
                    argsLoader.setStringArgumentValue(argsLoader.getSourceArgs().getDirectory(), n);
                    break;
                case "Recursive":
                    argsLoader.setBooleanArgumentValue(argsLoader.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionCheckSteadyState(YADEXMLArgumentsLoader argsLoader, Node steadyState) throws Exception {
        NodeList nl = steadyState.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CheckSteadyStateInterval":
                    argsLoader.setStringArgumentValue(argsLoader.getSourceArgs().getCheckSteadyStateInterval(), n);
                    break;
                case "CheckSteadyStateCount":
                    argsLoader.setIntegerArgumentValue(argsLoader.getSourceArgs().getCheckSteadyCount(), n);
                    break;
                case "CheckSteadyStateErrorState":// for JS1 job chain
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionDirectives(YADEXMLArgumentsLoader argsLoader, Node directives) throws Exception {
        NodeList nl = directives.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "DisableErrorOnNoFilesFound":
                    argsLoader.setOppositeBooleanArgumentValue(argsLoader.getSourceArgs().getErrorOnNoFilesFound(), n);
                    break;
                case "TransferZeroByteFiles":
                    argsLoader.getSourceArgs().setZeroByteTransfer(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionPolling(YADEXMLArgumentsLoader argsLoader, Node polling) throws Exception {
        NodeList nl = polling.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            YADESourcePollingArguments pollingArgs = new YADESourcePollingArguments();
            pollingArgs.applyDefaultIfNullQuietly();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "PollInterval":
                        argsLoader.setStringArgumentValue(pollingArgs.getPollInterval(), n);
                        break;
                    case "PollTimeout":
                        argsLoader.setIntegerArgumentValue(pollingArgs.getPollTimeout(), n);
                        break;
                    case "MinFiles":
                        argsLoader.setIntegerArgumentValue(pollingArgs.getPollMinFiles(), n);
                        break;
                    case "WaitForSourceFolder":
                        argsLoader.setBooleanArgumentValue(pollingArgs.getWaitingForLateComers(), n);
                        break;
                    case "PollErrorState":
                        // YADE 1 - YADE Job API
                        break;
                    case "PollingServer":
                        argsLoader.setBooleanArgumentValue(pollingArgs.getPollingServer(), n);
                        break;
                    case "PollingServerDuration":
                        argsLoader.setStringArgumentValue(pollingArgs.getPollingServerDuration(), n);
                        break;
                    case "PollForever":
                        argsLoader.setBooleanArgumentValue(pollingArgs.getPollingServerPollForever(), n);
                        break;
                    }
                }
            }
            argsLoader.getSourceArgs().setPolling(pollingArgs);
        }
    }

    private static void parseSourceOptionResultSet(YADEXMLArgumentsLoader argsLoader, Node resultSet) throws Exception {
        NodeList nl = resultSet.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "ResultSetFile":
                        argsLoader.setPathArgumentValue(argsLoader.getClientArgs().getResultSetFile(), n);
                        break;
                    case "CheckResultSetCount":
                        parseSourceOptionResultSetCheckCount(argsLoader, n);
                        break;
                    case "EmptyResultSetState":
                        // YADE 1 - YADE Job API
                        break;
                    }
                }
            }
        }
    }

    private static void parseSourceOptionResultSetCheckCount(YADEXMLArgumentsLoader argsLoader, Node resultSetCheckCount) throws Exception {
        NodeList nl = resultSetCheckCount.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "ExpectedResultSetCount":
                        argsLoader.setIntegerArgumentValue(argsLoader.getClientArgs().getExpectedResultSetCount(), n);
                        break;
                    case "RaiseErrorIfResultSetIs":
                        SOSComparisonOperator comparisonOperator = SOSComparisonOperator.fromString(argsLoader.getValue(n));
                        if (comparisonOperator != null) {
                            argsLoader.getClientArgs().getRaiseErrorIfResultSetIs().setValue(comparisonOperator);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static void parseSourceOptionCheckIntegrityHash(YADEXMLArgumentsLoader argsLoader, Node checkIntegrityHash) throws Exception {
        argsLoader.getSourceArgs().getCheckIntegrityHash().setValue(Boolean.valueOf(true));

        NodeList nl = checkIntegrityHash.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "HashAlgorithm":
                        argsLoader.setStringArgumentValue(argsLoader.getSourceArgs().getIntegrityHashAlgorithm(), n);
                        break;
                    }
                }
            }
        }
    }

    private static void parseTargetOptions(YADEXMLArgumentsLoader argsLoader, Node targetFileOptions) throws Exception {
        NodeList nl = targetFileOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AppendFiles":
                    argsLoader.setBooleanArgumentValue(argsLoader.getTargetArgs().getAppendFiles(), n);
                    break;
                case "Atomicity":
                    parseTargetOptionAtomicity(argsLoader, n);
                    break;
                case "CheckSize":
                    argsLoader.setBooleanArgumentValue(argsLoader.getTargetArgs().getCheckSize(), n);
                    break;
                case "CumulateFiles":
                    parseTargetOptionCumulateFiles(argsLoader, n);
                    break;
                case "CompressFiles":
                    parseTargetOptionCompressFiles(argsLoader, n);
                    break;
                case "CreateIntegrityHashFile":
                    parseTargetOptionCreateIntegrityHashFile(argsLoader, n);
                    break;
                case "KeepModificationDate":
                    argsLoader.setBooleanArgumentValue(argsLoader.getTargetArgs().getKeepModificationDate(), n);
                    break;
                case "DisableMakeDirectories":
                    argsLoader.setOppositeBooleanArgumentValue(argsLoader.getTargetArgs().getCreateDirectories(), n);
                    break;
                case "DisableOverwriteFiles":
                    argsLoader.setOppositeBooleanArgumentValue(argsLoader.getTargetArgs().getOverwriteFiles(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionAtomicity(YADEXMLArgumentsLoader argsLoader, Node atomicity) throws Exception {
        NodeList nl = atomicity.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AtomicPrefix":
                    argsLoader.setStringArgumentValue(argsLoader.getTargetArgs().getAtomicPrefix(), n);
                    break;
                case "AtomicSuffix":
                    argsLoader.setStringArgumentValue(argsLoader.getTargetArgs().getAtomicSuffix(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCumulateFiles(YADEXMLArgumentsLoader argsLoader, Node cumulateFiles) throws Exception {
        NodeList nl = cumulateFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CumulativeFileSeparator":
                    argsLoader.setStringArgumentValue(argsLoader.getTargetArgs().getCumulativeFileSeparator(), n);
                    break;
                case "CumulativeFilename":
                    argsLoader.setStringArgumentValue(argsLoader.getTargetArgs().getCumulativeFileName(), n);
                    break;
                case "CumulativeFileDelete":
                    argsLoader.setBooleanArgumentValue(argsLoader.getTargetArgs().getCumulativeFileDelete(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCompressFiles(YADEXMLArgumentsLoader argsLoader, Node compressFiles) throws Exception {
        NodeList nl = compressFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CompressedFileExtension":
                    argsLoader.setStringArgumentValue(argsLoader.getTargetArgs().getCompressedFileExtension(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCreateIntegrityHashFile(YADEXMLArgumentsLoader argsLoader, Node createIntegrityHashFile) throws Exception {
        argsLoader.getTargetArgs().getCreateIntegrityHashFile().setValue(Boolean.valueOf(true));

        NodeList nl = createIntegrityHashFile.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HashAlgorithm":
                    argsLoader.setStringArgumentValue(argsLoader.getTargetArgs().getIntegrityHashAlgorithm(), n);
                    break;
                }
            }
        }
    }

    private static void parseFragmentRefRename(YADEXMLArgumentsLoader argsLoader, Node rename, YADESourceTargetArguments sourceTargetArgs)
            throws Exception {
        NodeList nl = rename.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "ReplaceWhat":
                    argsLoader.setStringArgumentValue(sourceTargetArgs.getReplacing(), n);
                    break;
                case "ReplaceWith":
                    argsLoader.setStringArgumentValue(sourceTargetArgs.getReplacement(), n);
                    break;
                }
            }
        }
    }

    public static void parsePreProcessing(YADEXMLArgumentsLoader argsLoader, YADEProviderCommandArguments args, Node preProcessing) throws Exception {
        NodeList nl = preProcessing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CommandBeforeFile":
                    String attrVal = SOSXML.getAttributeValue(n, "enable_for_skipped_transfer");
                    if (!SOSString.isEmpty(attrVal)) {
                        args.getCommandsBeforeFileEnableForSkipped().setValue(Boolean.parseBoolean(attrVal));
                    }
                    args.setCommandsBeforeFile(argsLoader.getValue(n));
                    break;
                case "CommandBeforeOperation":
                    args.setCommandsBeforeOperation(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    public static void parsePostProcessing(YADEXMLArgumentsLoader argsLoader, YADEProviderCommandArguments args, Node postProcessing)
            throws Exception {
        NodeList nl = postProcessing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CommandAfterFile":
                    String attrVal = SOSXML.getAttributeValue(n, "disable_for_skipped_transfer");
                    if (!SOSString.isEmpty(attrVal)) {
                        args.getCommandsAfterFileDisableForSkipped().setValue(Boolean.parseBoolean(attrVal));
                    }
                    args.setCommandsAfterFile(argsLoader.getValue(n));
                    break;
                case "CommandAfterOperationOnSuccess":
                    args.setCommandsAfterOperationOnSuccess(argsLoader.getValue(n));
                    break;
                case "CommandAfterOperationOnError":
                    args.setCommandsAfterOperationOnError(argsLoader.getValue(n));
                    break;
                case "CommandAfterOperationFinal":
                    args.setCommandsAfterOperationFinal(argsLoader.getValue(n));
                    break;
                case "CommandBeforeRename":
                    args.setCommandsBeforeRename(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

}
