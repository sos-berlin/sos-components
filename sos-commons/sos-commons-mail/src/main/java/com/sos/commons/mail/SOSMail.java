package com.sos.commons.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments.SOSCredentialStoreResolver;
import com.sos.commons.credentialstore.exceptions.SOSCredentialStoreException;
import com.sos.commons.util.SOSString;

public class SOSMail {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSMail.class);

    public static final int PRIORITY_HIGHEST = 1;
    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_NORMAL = 3;
    public static final int PRIORITY_LOW = 4;
    public static final int PRIORITY_LOWEST = 5;

    public static final String PROPERTY_NAME_SMTP_HOST = "mail.smtp.host";
    public static final String PROPERTY_NAME_SMTP_PORT = "mail.smtp.port";
    public static final String PROPERTY_NAME_SMTP_USER = "mail.smtp.user";
    public static final String PROPERTY_NAME_SMTP_PASSWORD = "mail.smtp.password";

    private SOSCredentialStoreArguments credentialStoreArguments;
    protected String subject = "";
    protected String from;
    protected String fromName;
    protected String replyTo;
    protected String queueDir;
    protected String body = "";
    protected String alternativeBody;
    protected String language = "de";
    protected String dateFormat = "dd.MM.yyyy";
    protected String datetimeFormat = "dd.MM.yyyy HH:mm";
    protected HashMap<String, String> dateFormats = new HashMap<String, String>();
    protected HashMap<String, String> datetimeFormats = new HashMap<String, String>();
    protected String attachmentCharset = "iso-8859-1";
    protected String charset = "iso-8859-1";
    protected String alternativeCharset = "iso-8859-1";
    protected String contentType = "text/plain";
    protected String alternativeContentType = "text/html";
    protected String encoding = "7bit";
    protected String alternativeEncoding = "7bit";
    protected String attachmentEncoding = "Base64";
    protected String attachmentContentType = "application/octet-stream";
    protected LinkedList<String> toList = new LinkedList<String>();
    protected LinkedList<String> ccList = new LinkedList<String>();
    protected LinkedList<String> bccList = new LinkedList<String>();
    protected TreeMap<String, SOSMailAttachment> attachmentList = new TreeMap<String, SOSMailAttachment>();
    private boolean sendToOutputStream = false;
    private byte[] messageBytes;
    private MimeMessage message = null;
    private SOSMailAuthenticator authenticator = null;
    private final ArrayList<FileInputStream> fileInputStreams = new ArrayList<FileInputStream>();
    private ByteArrayOutputStream rawEmailByteStream = null;
    private String lastError = "";
    private boolean changed = false;
    private final String queuePattern = "yyyy-MM-dd.HHmmss.S";
    private String queuePraefix = "sos.";
    private String lastGeneratedFileName = "";
    private String loadedMessageId = "";
    private boolean messageReady = false;
    private boolean queueMailOnError = true;
    private int priority = -1;
    private String securityProtocol = "";
    private Session session = null;
    private Properties smtpProperties = null;

    abstract class MydataSource implements DataSource {

        final String name;
        final String contentType;

        public MydataSource(final File dataSourceFile, final String dataSourceContentType) {
            name = dataSourceFile.getName();
            contentType = dataSourceContentType;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public OutputStream getOutputStream() {
            throw new RuntimeException(getClass().getName() + " has no OutputStream");
        }
    }

    class FileDataSource extends MydataSource {

        final File file;

        public FileDataSource(final File dataSourceFile, final String dataSourceContentType) {
            super(dataSourceFile, dataSourceContentType);
            file = dataSourceFile;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            FileInputStream f = new FileInputStream(file);
            fileInputStreams.add(f);
            return f;
        }
    }

    public SOSMail(Properties smtpProperties) throws Exception {
        if (smtpProperties != null) {
            if (smtpProperties.getProperty(PROPERTY_NAME_SMTP_USER) != null && smtpProperties.getProperty(PROPERTY_NAME_SMTP_USER).isEmpty()) {
                smtpProperties.remove(PROPERTY_NAME_SMTP_USER);
                if (smtpProperties.getProperty(PROPERTY_NAME_SMTP_PASSWORD) != null) {
                    smtpProperties.remove(PROPERTY_NAME_SMTP_PASSWORD);
                }
            }
        }
        this.smtpProperties = smtpProperties;
    }

    public SOSMail(final String smtpHost) throws Exception {
        this.smtpProperties = new Properties();
        setHost(smtpHost);
        init();
    }

    public SOSMail(final String smtpHost, final String smtpUser, final String smtpUserPassword) throws Exception {
        this.smtpProperties = new Properties();
        setHost(smtpHost);
        if (!SOSString.isEmpty(smtpUser)) {
            setUser(smtpUser);
            setPassword(smtpUserPassword);
        }
        init();
    }

    public SOSMail(final String smtpHost, final String smtpPort, final String smtpUser, final String smtpUserPassword) throws Exception {
        this.smtpProperties = new Properties();
        setHost(smtpHost);
        setPort(smtpPort);
        if (!SOSString.isEmpty(smtpUser)) {
            setUser(smtpUser);
            setPassword(smtpUserPassword);
        }
        init();
    }
    private void initPriority() throws MessagingException {
        switch (priority) {
        case PRIORITY_HIGHEST:
            this.setPriorityHighest();
            break;
        case PRIORITY_HIGH:
            this.setPriorityHigh();
            break;
        case PRIORITY_LOW:
            this.setPriorityLow();
            break;
        case PRIORITY_LOWEST:
            this.setPriorityLowest();
            break;
        default:
            break;
        }
    }

    public void init() throws Exception {
        dateFormats.put("de", "dd.MM.yyyy");
        dateFormats.put("en", "MM/dd/yyyy");
        datetimeFormats.put("de", "dd.MM.yyyy HH:mm");
        datetimeFormats.put("en", "MM/dd/yyyy HH:mm");
        initLanguage();
        initMessage();
        clearRecipients();
        clearAttachments();
    }

    private void initMessage() throws Exception {
        createMessage(createSession());
        initPriority();
    }

    private Session createSession() throws Exception {
        Properties props = null;
        if (smtpProperties == null) {
            props = System.getProperties();
        } else {
            props = smtpProperties;
        }

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.class", "com.sun.mail.SMTPTransport");

        if (!SOSString.isEmpty(getUser())) {
            props.put("mail.smtp.auth", "true");
        } else {
            props.put("mail.smtp.auth", "false");
        }

        if ("ssl".equalsIgnoreCase(securityProtocol)) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.transport.protocol", "smtps");
        } else if ("starttls".equalsIgnoreCase(securityProtocol)) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.transport.protocol", "smtps");
        }
        authenticator = new SOSMailAuthenticator(getUser(), getPassword());
        session = Session.getInstance(props, authenticator);
        return session;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) throws Exception {
        this.securityProtocol = securityProtocol;
        this.initMessage();
    }

    private void createMessage(final Session session) throws Exception {
        message = new MimeMessage(session);
    }

    private void initLanguage() throws Exception {
        if (dateFormats.containsKey(this.getLanguage()) && datetimeFormats.containsKey(this.getLanguage())) {
            this.setDateFormat(dateFormats.get(this.getLanguage()).toString());
            this.setDatetimeFormat(datetimeFormats.get(this.getLanguage()).toString());
        } else {
            this.setDateFormat(dateFormats.get("de").toString());
            this.setDatetimeFormat(datetimeFormats.get("de").toString());
        }
    }

    public void addRecipient(String recipient) throws Exception {
        String token = "";
        warn("addRecipient", recipient);
        if (recipient == null) {
            throw new Exception("recipient has no value.");
        }
        recipient = recipient.replace(',', ';');
        StringTokenizer t = new StringTokenizer(recipient, ";");
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            if (!toList.contains(token)) {
                toList.add(token);
            }
            LOGGER.debug("TO=" + token);
        }
        changed = true;
    }

    public void addCC(String recipient) throws Exception {
        String token = "";
        warn("addCC", recipient);
        if (recipient == null) {
            throw new Exception("CC recipient has no value.");
        }
        recipient = recipient.replace(',', ';');
        StringTokenizer t = new StringTokenizer(recipient, ";");
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            if (!toList.contains(token) && !ccList.contains(token)) {
                ccList.add(token);
                LOGGER.debug("CC=" + token);
            } else {
                LOGGER.debug("CC=" + token + " ignored");
            }
        }
        changed = true;
    }

    public void addBCC(String recipient) throws Exception {
        String token = "";
        warn("addBCC", recipient);
        if (recipient == null) {
            throw new Exception("BCC recipient has no value.");
        }
        recipient = recipient.replace(',', ';');
        StringTokenizer t = new StringTokenizer(recipient, ";");
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            if (!ccList.contains(token) && !toList.contains(token) && !bccList.contains(token)) {
                bccList.add(token);
                LOGGER.debug("BCC=" + token);
            } else {
                LOGGER.debug("BCC=" + token + " ignored");
            }
        }
        changed = true;
    }

    private void closeAttachments() throws Exception {
        Exception exception = null;
        for (int i = 0; i < fileInputStreams.size(); i++) {
            try {
                ((FileInputStream) fileInputStreams.get(i)).close();
            } catch (Exception x) {
                if (exception == null) {
                    exception = x;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    public void addAttachment(final SOSMailAttachment att) throws Exception {
        warn("addAttachment", att.getFile().getAbsolutePath());
        attachmentList.put(att.getFile().getAbsolutePath(), att);
        changed = true;
    }

    public void addAttachment(final String filename) throws Exception {
        warn("addAttachment", filename);
        File f = new File(filename);
        SOSMailAttachment att = new SOSMailAttachment(this, f);
        att.setCharset(getAttachmentCharset());
        att.setEncoding(getAttachmentEncoding());
        att.setContentType(getAttachmentContentType());
        attachmentList.put(filename, att);
        changed = true;
    }

    public void addAttachment(final String filename, final String params) throws Exception {
        String name = "";
        String value = "";
        String token = "";
        int counter = 0;
        warn("addAttachment", filename + "(" + params + ")");
        StringTokenizer t = new StringTokenizer(params, ",");
        File f = new File(filename);
        SOSMailAttachment att = new SOSMailAttachment(this, f);
        while (t.hasMoreTokens()) {
            token = t.nextToken();
            StringTokenizer vv = new StringTokenizer(token, "=");
            if (vv.countTokens() == 1) {
                name = "content-type";
                value = vv.nextToken();
                counter += 1;
            } else {
                name = vv.nextToken().trim();
                try {
                    value = vv.nextToken().trim();
                } catch (NoSuchElementException e) {
                    value = "";
                }
            }
            if ("content-type".equalsIgnoreCase(name)) {
                att.setContentType(value);
            } else if ("charset".equalsIgnoreCase(name)) {
                att.setCharset(value);
            } else if ("encoding".equalsIgnoreCase(name)) {
                att.setEncoding(value);
            } else {
                throw new Exception("USING of .addAttachment is wrong. ==> " + params
                        + ", rigth using is: [content-type-value],[content-type=<value>],[charset=<value>],[encoding=<value>]");
            }
            if (counter > 1) {
                throw new Exception("USING of .addAttachment is wrong. ==> " + params
                        + ", rigth using is: [content-type-value],[content-type=<value>],[charset=<value>],[encoding=<value>]");
            }
        }
        attachmentList.put(filename, att);
        changed = true;
    }

    private void checkFileCanRead(File file) throws IOException {
        try {
            FileReader fileReader = new FileReader(file.getAbsolutePath());
            fileReader.read();
            fileReader.close();
        } catch (IOException e) {
            throw new IOException("Error attachment check file:" + e.getMessage());
        }
    }

    private void addFile(final SOSMailAttachment att) throws Exception {
        checkFileCanRead(att.getFile());
        MimeBodyPart attachment = new MimeBodyPart();
        DataSource data_source = new FileDataSource(att.getFile(), att.getContentType());
        DataHandler data_handler = new DataHandler(data_source);
        attachment.setDataHandler(data_handler);
        attachment.setFileName(att.getFile().getName());
        if (att.getContentType().startsWith("text/")) {
            String s = "";
            FileReader fr = new FileReader(att.getFile());
            for (int c; (c = fr.read()) != -1;) {
                s += (char) c;
            }
            attachment.setText(s, att.getCharset());
            fr.close();
        }
        Object m = message.getContent();
        if (!(m instanceof MimeMultipart)) {
            throw new RuntimeException(getClass().getName() + "mime_message.getContent() is not MimeMultiPart");
        }
        ((MimeMultipart) m).addBodyPart(attachment);
        attachment.setHeader("Content-Transfer-Encoding", att.getEncoding());
    }

    public void loadFile(final File messageFile) throws Exception {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(messageFile);
            message = new MimeMessage(createSession(), fis);
            loadedMessageId = message.getMessageID();
            rawEmailByteStream = new ByteArrayOutputStream();
            message.writeTo(rawEmailByteStream);
            messageBytes = rawEmailByteStream.toByteArray();
            messageReady = true;
        } catch (Exception x) {
            throw new Exception("Fehler beim Lesen der eMail. " + messageFile);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public void unloadMessage() {
        messageReady = false;
        loadedMessageId = "";
        message = null;
    }

    public boolean send(final boolean send) throws Exception {
        if (send) {
            return send();
        } else {
            return prepareJavaMail();
        }
    }

    public boolean send() throws Exception {
        return sendJavaMail();
    }

    private boolean sendJavaMail() throws Exception {
        useCredentialStoreArguments();

        String host = getHost();
        if (SOSString.isEmpty(host)) {
            throw new Exception("host is empty");
        }
        String port = getPort();
        try {
            prepareJavaMail();

            String portMsg = port == null ? "<java default port>" : port;
            StringBuilder msg = new StringBuilder("sending email: host:port=").append(host).append(":").append(portMsg);
            msg.append(" to=").append(getRecipientsAsString());
            String cc = getCCsAsString();
            if (!"".equals(cc)) {
                msg.append(" CC=").append(cc);
            }
            String bcc = getBCCsAsString();
            if (!"".equals(bcc)) {
                msg.append(" BCC=").append(bcc);
            }
            LOGGER.info(msg.toString());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Subject=" + subject);
                LOGGER.debug(dumpHeaders());
                LOGGER.debug(dumpMessageAsString(false));
            }
            if (!sendToOutputStream) {
                Transport transport;
                if ("ssl".equalsIgnoreCase(securityProtocol) || "starttls".equalsIgnoreCase(securityProtocol)) {
                    transport = session.getTransport("smtps");
                } else {
                    transport = session.getTransport("smtp");
                }
                message.setSentDate(new Date());
                if (smtpProperties == null) {
                    if (port != null) {
                        System.setProperty(PROPERTY_NAME_SMTP_PORT, port);
                    }
                    System.setProperty(PROPERTY_NAME_SMTP_HOST, host);
                } else {
                    // TODO ??? why set smtpProperties ?
                    if (port != null) {
                        smtpProperties.setProperty(PROPERTY_NAME_SMTP_PORT, port);
                    }
                    smtpProperties.setProperty(PROPERTY_NAME_SMTP_HOST, host);
                }
                if (SOSString.isEmpty(getUser())) {
                    transport.connect();
                } else {
                    transport.connect(host, getUser(), getPassword());
                }
                transport.sendMessage(message, message.getAllRecipients());
                transport.close();
                rawEmailByteStream = new ByteArrayOutputStream();
                message.writeTo(rawEmailByteStream);
                messageBytes = rawEmailByteStream.toByteArray();
                changed = true;
            }
            return true;
        } catch (javax.mail.AuthenticationFailedException ee) {
            lastError = String.format("%s while connecting to %s:%s %s /******** --> %s", ee.getClass().getSimpleName(), host, port, getUser(), ee
                    .toString());

            if (queueMailOnError) {
                try {
                    dumpMessageToFile(true);
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
                return false;
            } else {
                throw ee;
            }
        } catch (javax.mail.MessagingException e) {
            if (queueMailOnError) {
                if (!SOSString.isEmpty(queueDir) && e.getMessage().startsWith("Could not connect to SMTP host") || e.getMessage().startsWith(
                        "Unknown SMTP host") || e.getMessage().startsWith("Read timed out") || e.getMessage().startsWith(
                                "Exception reading response")) {
                    lastError = String.format("%s ==> %s:%s %s /********", e.getMessage(), host, port, getUser());
                    try {
                        dumpMessageToFile(true);
                    } catch (Exception ee) {
                        LOGGER.warn(e.getMessage());
                    }
                    return false;

                } else {
                    throw new Exception(String.format("%s occurred on send: %s", e.getClass().getSimpleName(), e.toString()), e);
                }
            } else {
                throw e;
            }
        } catch (SocketTimeoutException e) {
            if (queueMailOnError) {
                if (!SOSString.isEmpty(queueDir)) {
                    lastError = String.format("%s ==> %s:%s %s /********", e.getMessage(), host, port, getUser());
                    try {
                        dumpMessageToFile(true);
                    } catch (Exception ee) {
                        LOGGER.warn(e.getMessage());
                    }
                    return false;
                } else {
                    throw new Exception(String.format("%s occurred on send: %s", e.getClass().getSimpleName(), e.toString()), e);
                }
            } else {
                throw e;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void useCredentialStoreArguments() throws SOSCredentialStoreException {
        if (credentialStoreArguments != null && credentialStoreArguments.getFile().getValue() != null) {
            SOSCredentialStoreResolver r = credentialStoreArguments.newResolver();

            addMailProperty(PROPERTY_NAME_SMTP_HOST, r.resolve(getHost()));
            addMailProperty(PROPERTY_NAME_SMTP_USER, r.resolve(getUser()));
            addMailProperty(PROPERTY_NAME_SMTP_PASSWORD, r.resolve(getPassword()));
        }
    }

    private boolean haveAlternative() {
        return !SOSString.isEmpty(alternativeBody) && attachmentList.isEmpty();
    }

    protected boolean prepareJavaMail() throws Exception {
        try {
            if (messageReady) {
                message.saveChanges();
                return true;
            }
            if (!changed) {
                return true;
            }
            changed = false;
            if ("text/html".equals(getContentType())) {
                body = body.replaceAll("\\\\n", "<br>");
            } else {
                body = body.replaceAll("\\\\n", "\n");
            }
            String t = "";
            if (toList.isEmpty()) {
                throw new Exception("no recipient specified.");
            }
            if (SOSString.isEmpty(from)) {
                throw new Exception("from is empty.");
            }
            if (!SOSString.isEmpty(fromName)) {
                message.setFrom(new InternetAddress(from, fromName));
            } else {
                message.setFrom(new InternetAddress(from));
            }
            message.setSentDate(new Date());
            if (!SOSString.isEmpty(replyTo)) {
                InternetAddress fromAddrs[] = new InternetAddress[1];
                fromAddrs[0] = new InternetAddress(replyTo);
                message.setReplyTo(fromAddrs);
            }
            if (!toList.isEmpty()) {
                InternetAddress toAddrs[] = new InternetAddress[toList.size()];
                int i = 0;
                for (ListIterator<String> e = toList.listIterator(); e.hasNext();) {
                    t = e.next();
                    toAddrs[i++] = new InternetAddress(t);
                }
                message.setRecipients(MimeMessage.RecipientType.TO, toAddrs);
            }
            InternetAddress toAddrs[] = new InternetAddress[ccList.size()];
            int i = 0;
            for (ListIterator<String> e = ccList.listIterator(); e.hasNext();) {
                t = e.next();
                toAddrs[i++] = new InternetAddress(t);
            }
            message.setRecipients(MimeMessage.RecipientType.CC, toAddrs);
            toAddrs = new InternetAddress[bccList.size()];
            i = 0;
            for (ListIterator<String> e = bccList.listIterator(); e.hasNext();) {
                t = e.next();
                toAddrs[i++] = new InternetAddress(t);
            }
            message.setRecipients(MimeMessage.RecipientType.BCC, toAddrs);
            if (subject != null) {
                message.setSubject(subject);
            }
            if (!attachmentList.isEmpty() || !SOSString.isEmpty(alternativeBody)) {
                MimeBodyPart bodypart = null;
                MimeBodyPart alternativeBodypart = null;
                MimeMultipart multipart = null;
                if (this.haveAlternative()) {
                    multipart = new MimeMultipart("alternative");
                } else {
                    multipart = new MimeMultipart();
                }
                bodypart = new MimeBodyPart();
                if (contentType.startsWith("text/")) {
                    bodypart.setContent(body, contentType + ";charset= " + charset);
                } else {
                    bodypart.setContent(body, contentType);
                }
                multipart.addBodyPart(bodypart);
                if (this.haveAlternative()) {
                    alternativeBodypart = new MimeBodyPart();
                    if (contentType.startsWith("text/")) {
                        alternativeBodypart.setContent(alternativeBody, alternativeContentType + ";charset= " + alternativeCharset);
                    } else {
                        alternativeBodypart.setContent(alternativeBody, alternativeContentType);
                    }
                    multipart.addBodyPart(alternativeBodypart);
                }
                message.setContent(multipart);
                bodypart.setHeader("Content-Transfer-Encoding", encoding);
                if (alternativeBodypart != null) {
                    alternativeBodypart.setHeader("Content-Transfer-Encoding", alternativeEncoding);
                }
                for (Iterator<SOSMailAttachment> iter = attachmentList.values().iterator(); iter.hasNext();) {
                    SOSMailAttachment attachment = iter.next();
                    String content_type = attachment.getContentType();
                    if (content_type == null) {
                        throw new Exception("content_type ist null");
                    }
                    LOGGER.debug("Attachment=" + attachment.getFile());
                    addFile(attachment);
                }
            } else {
                message.setHeader("Content-Transfer-Encoding", encoding);
                if (contentType.startsWith("text/")) {
                    message.setContent(body, contentType + "; charset=" + charset);
                } else {
                    message.setContent(body, contentType);
                }
            }
            message.saveChanges();
            closeAttachments();
            return true;
        } catch (Exception e) {
            throw new Exception("error occurred on send: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public String dumpHeaders() throws IOException, MessagingException {
        StringBuilder sb = new StringBuilder();
        for (Enumeration<Header> e = message.getAllHeaders(); e.hasMoreElements();) {
            Header header = (Header) e.nextElement();
            sb.append("\n");
            sb.append(header.getName());
            sb.append(": ");
            sb.append(header.getValue());
        }
        return sb.toString();

    }

    private ByteArrayOutputStream messageRemoveAttachments() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MimeMessage mm = new MimeMessage(message);
        Object mmpo = mm.getContent();
        if (mmpo instanceof MimeMultipart) {
            MimeMultipart mmp = (MimeMultipart) mmpo;
            if (mm.isMimeType("multipart/*")) {
                mmp = (MimeMultipart) mm.getContent();
                for (int i = 1; i < mmp.getCount(); i++) {
                    mmp.removeBodyPart(i);
                    i--;
                }
            }
            mm.setContent(mmp);
            mm.saveChanges();
        }
        mm.writeTo(baos);
        return baos;
    }

    public String dumpMessageAsString() throws Exception {
        return dumpMessageAsString(false);
    }

    private void dumpMessageToFile(final boolean withAttachment) throws Exception {
        Date d = new Date();
        StringBuffer bb = new StringBuffer();
        SimpleDateFormat s = new SimpleDateFormat(queuePattern);
        FieldPosition fp = new FieldPosition(0);
        StringBuffer b = s.format(d, bb, fp);
        lastGeneratedFileName = queueDir + "/" + queuePraefix + b + ".email~";
        File f = new File(lastGeneratedFileName);
        while (f.exists()) {
            b = s.format(d, bb, fp);
            lastGeneratedFileName = queueDir + "/" + queuePraefix + b + ".email~";
            f = new File(lastGeneratedFileName);
        }
        dumpMessageToFile(f, withAttachment);
    }

    public void dumpMessageToFile(final String filename, final boolean withAttachment) throws Exception {
        dumpMessageToFile(new File(filename), withAttachment);
    }

    public void dumpMessageToFile(final File file, final boolean withAttachment) throws Exception {
        try {
            this.prepareJavaMail();
            File myFile = new File(file.getAbsolutePath() + "~");
            FileOutputStream out = new FileOutputStream(myFile, true);
            out.write(dumpMessage(withAttachment));
            out.close();
            String newFilename = myFile.getAbsolutePath().substring(0, myFile.getAbsolutePath().length() - 1);
            File f = new File(newFilename);
            f.delete();
            myFile.renameTo(f);
        } catch (Exception e) {
            throw new Exception("error occurred on dump: " + e.toString());
        }
    }

    public String dumpMessageAsString(final boolean withAttachment) throws Exception {
        byte[] bytes;
        ByteArrayOutputStream baos = null;
        prepareJavaMail();
        if (!withAttachment) {
            baos = messageRemoveAttachments();
        }
        rawEmailByteStream = new ByteArrayOutputStream();
        message.writeTo(rawEmailByteStream);
        if (withAttachment || baos == null) {
            bytes = rawEmailByteStream.toByteArray();
        } else {
            bytes = baos.toByteArray();
        }
        return new String(bytes);
    }

    public byte[] dumpMessage() throws Exception {
        return dumpMessage(true);
    }

    public byte[] dumpMessage(final boolean withAttachment) throws Exception {
        byte[] bytes;
        ByteArrayOutputStream baos = null;
        prepareJavaMail();
        if (!withAttachment) {
            baos = messageRemoveAttachments();
        }
        rawEmailByteStream = new ByteArrayOutputStream();
        message.writeTo(rawEmailByteStream);
        if (withAttachment || baos == null) {
            bytes = rawEmailByteStream.toByteArray();
        } else {
            bytes = baos.toByteArray();
        }
        return bytes;
    }

    public LinkedList<String> getRecipients() {
        return toList;
    }

    public String getRecipientsAsString() throws MessagingException {
        String s = " ";
        if (messageReady) {
            Address[] addresses = message.getRecipients(MimeMessage.RecipientType.TO);
            if (addresses != null) {
                for (Address aktAddress : addresses) {
                    s += aktAddress.toString() + ",";
                }
            }
        } else {
            for (Iterator<String> i = toList.listIterator(); i.hasNext();) {
                s += i.next() + ",";
            }
        }
        return s.substring(0, s.length() - 1).trim();
    }

    public LinkedList<String> getCCs() {
        return ccList;
    }

    public String getCCsAsString() throws MessagingException {
        String s = " ";
        if (messageReady) {
            Address[] addresses = message.getRecipients(MimeMessage.RecipientType.CC);
            if (addresses != null) {
                for (Address aktAddress : addresses) {
                    s += aktAddress.toString() + ",";
                }
            }
        } else {
            for (Iterator<String> i = ccList.listIterator(); i.hasNext();) {
                s += i.next() + ",";
            }
        }
        return s.substring(0, s.length() - 1).trim();
    }

    public LinkedList<String> getBCCs() {
        return bccList;
    }

    public String getBCCsAsString() throws MessagingException {
        String s = " ";
        if (messageReady) {
            Address[] addresses = message.getRecipients(MimeMessage.RecipientType.BCC);
            if (addresses != null) {
                for (Address aktAddress : addresses) {
                    s += aktAddress.toString() + ",";
                }
            }
        } else {
            for (Iterator<String> i = bccList.listIterator(); i.hasNext();) {
                s += i.next() + ",";
            }
        }
        return s.substring(0, s.length() - 1).trim();
    }

    public void clearRecipients() {
        LOGGER.debug("clearRecipients");
        if (toList != null) {
            toList.clear();
        }
        if (ccList != null) {
            ccList.clear();
        }
        if (bccList != null) {
            bccList.clear();
        }
        changed = true;
    }

    public void clearAttachments() {
        attachmentList.clear();
        changed = true;
    }

    public String getQuotedName(final String name) {
        if (name.indexOf('<') > -1 && name.indexOf('>') > -1) {
            return name;
        } else {
            return '<' + name + '>';
        }
    }

    public void setTimeout(final int timeout) throws Exception {
        addMailProperty("mail.smtp.timeout", timeout);
        initMessage();

    }

    public Integer getTimeout() {
        if (getMailPropertyValue("mail.smtp.timeout") == null) {
            return 30000;
        } else {
            return Integer.parseInt(getMailPropertyValue("mail.smtp.timeout"));
        }
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String lang) throws Exception {
        language = lang;
        initLanguage();
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDatetimeFormat() {
        return datetimeFormat;
    }

    public void setDatetimeFormat(final String datetimeFormat) {
        this.datetimeFormat = datetimeFormat;
    }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
        warn("encoding", encoding);
        changed = true;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setCharset(final String charset) {
        this.charset = charset;
        warn("charset", charset);
        changed = true;
    }

    public String getCharset() {
        return charset;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
        warn("contentType", contentType);
        changed = true;
    }

    public String getContentType() {
        return contentType;
    }

    public void setAttachmentContentType(final String attachmentContentType) {
        this.attachmentContentType = attachmentContentType;
        warn("attachmentContentType", attachmentContentType);
        changed = true;
    }

    public String getAttachmentContentType() {
        return attachmentContentType;
    }

    public String getHost() {
        return getMailPropertyValue(PROPERTY_NAME_SMTP_HOST);
    }

    public String getPort() {
        return getMailPropertyValue(PROPERTY_NAME_SMTP_PORT);
    }

    private String getUser() {
        return getMailPropertyValue(PROPERTY_NAME_SMTP_USER);
    }

    private String getPassword() {
        return getMailPropertyValue(PROPERTY_NAME_SMTP_PASSWORD);
    }

    private String getMailPropertyValue(String key) {
        Properties p;
        if (smtpProperties == null) {
            p = System.getProperties();
        } else {
            p = smtpProperties;
        }
        if (p.get(key) == null) {
            return null;
        } else {
            return p.get(key).toString();
        }
    }

    public void setQueueDir(final String queueDir) {
        this.queueDir = queueDir;
    }

    public String getQueueDir() {
        return queueDir;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
        warn("subject", subject);
        changed = true;
    }

    public String getSubject() {
        return subject;
    }

    public void setFrom(final String from) {
        this.from = from;
        warn("from", from);
        changed = true;
    }

    public String getFrom() {
        return from;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(final String fromName) {
        this.fromName = fromName;
        warn("fromName", fromName);
        changed = true;
    }

    public void setReplyTo(final String replyTo) {
        this.replyTo = replyTo;
        warn("replyTo", replyTo);
        changed = true;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setBody(final String body) {
        this.body = body;
        warn("body", body);
        changed = true;
    }

    public String getBody() {
        return body;
    }

    public byte[] getMessageBytes() {
        return messageBytes;
    }

    public void setSendToOutputStream(final boolean sendToOutputStream) {
        this.sendToOutputStream = sendToOutputStream;
    }

    public void setattachmentEncoding(final String attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;
        warn("attachmentEncoding", attachmentEncoding);
        changed = true;
    }

    public MimeMessage getMessage() {
        return message;
    }

    public String getLastError() {
        return lastError;
    }

    public String getAttachmentEncoding() {
        return attachmentEncoding;
    }

    public String getAttachmentCharset() {
        return attachmentCharset;
    }

    public void setAttachmentCharset(final String attachmentCharset) {
        this.attachmentCharset = attachmentCharset;
        warn("attachmentCharset", attachmentCharset);
        changed = true;
    }

    public void setAttachmentEncoding(final String attachmentEncoding) {
        this.attachmentEncoding = attachmentEncoding;
        warn("attachmentEncoding", attachmentEncoding);
        changed = true;
    }

    public void setHost(final String host) throws Exception {
        addMailProperty(PROPERTY_NAME_SMTP_HOST, host);
        this.initMessage();
    }

    public void setPassword(final String password) throws Exception {
        addMailProperty(PROPERTY_NAME_SMTP_PASSWORD, password);
        this.initMessage();
    }

    public void setUser(final String user) throws Exception {
        addMailProperty(PROPERTY_NAME_SMTP_USER, user);
        this.initMessage();
    }

    public void setPort(final String port) throws Exception {
        addMailProperty(PROPERTY_NAME_SMTP_PORT, port);
        this.initMessage();
    }

    public void setPriorityHighest() throws MessagingException {
        message.setHeader("Priority", "urgent");
        message.setHeader("X-Priority", "1 (Highest)");
        message.setHeader("X-MSMail-Priority", "Highest");
        changed = true;
    }

    public void setPriorityHigh() throws MessagingException {
        message.setHeader("Priority", "urgent");
        message.setHeader("X-Priority", "2 (High)");
        message.setHeader("X-MSMail-Priority", "Highest");
        changed = true;
    }

    public void setPriorityNormal() throws MessagingException {
        message.setHeader("Priority", "normal");
        message.setHeader("X-Priority", "3 (Normal)");
        message.setHeader("X-MSMail-Priority", "Normal");
        changed = true;
    }

    public void setPriorityLow() throws MessagingException {
        message.setHeader("Priority", "non-urgent");
        message.setHeader("X-Priority", "4 (Low)");
        message.setHeader("X-MSMail-Priority", "Low");
        changed = true;
    }

    public void setPriorityLowest() throws MessagingException {
        message.setHeader("Priority", "non-urgent");
        message.setHeader("X-Priority", "5 (Lowest)");
        message.setHeader("X-MSMail-Priority", "Low");
        changed = true;
    }

    public void setAlternativeBody(final String alternativeBody) {
        this.alternativeBody = alternativeBody;
    }

    public void setAlternativeCharset(final String alternativeCharset) {
        this.alternativeCharset = alternativeCharset;
    }

    public void setAlternativeContentType(final String alternativeContentType) {
        this.alternativeContentType = alternativeContentType;
    }

    public String getQueuePraefix() {
        return queuePraefix;
    }

    public String getLastGeneratedFileName() {
        return lastGeneratedFileName;
    }

    public void setQueuePraefix(final String queuePraefix) {
        this.queuePraefix = queuePraefix;
    }

    public String getLoadedMessageId() {
        return loadedMessageId;
    }

    private void warn(final String n, final String v) {
        if (messageReady) {
            try {
                LOGGER.warn("...setting of " + n + "=" + v + " will not be used. Loaded Message will be sent unchanged.");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void addMailProperty(String key, Object value) {
        if (value != null && key != null) {
            if (this.smtpProperties == null) {
                System.getProperties().put(key, value);
            } else {
                this.smtpProperties.put(key, value);
            }
        }
    }

    public void setQueueMailOnError(boolean val) {
        this.queueMailOnError = val;
    }

    public void setCredentialStoreArguments(SOSCredentialStoreArguments val) {
        credentialStoreArguments = val;
    }

    public SOSCredentialStoreArguments getCredentialStoreArguments() {
        return credentialStoreArguments;
    }

    public static void main(final String[] args) throws Exception {
        SOSMail sosMail = new SOSMail("smtp.sos");
        sosMail.setPriorityLowest();
        sosMail.setQueueDir("c:/");
        sosMail.setFrom("xyz@sos-berlin.com");
        sosMail.setEncoding("8bit");
        sosMail.setattachmentEncoding("Base64");
        sosMail.setSubject("Betreff");
        sosMail.setReplyTo("xyz@sos-berlin.com");
        String s = "Hello\\nWorld";
        sosMail.setBody(s);
        sosMail.addRecipient("xyz@sos-berlin.com");
        sosMail.setPriorityLowest();
        if (!sosMail.send()) {
            LOGGER.warn(sosMail.getLastError());
        }
        sosMail.clearRecipients();
    }
}