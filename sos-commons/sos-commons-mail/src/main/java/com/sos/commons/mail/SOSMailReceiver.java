package com.sos.commons.mail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSRequiredArgumentMissingException;
import com.sos.commons.util.SOSString;

public class SOSMailReceiver {

	protected SOSMailAuthenticator authenticator = null;
	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailReceiver.class);
	private Store store;
	private Folder folder = null;
	private final Protocol protocol;
	private Integer defaultTimeout = 5000;
    private String host;
	private Integer port;
	private String user;
	private Session session;
	private String password;
	public int READ_ONLY = Folder.READ_ONLY;
	public int READ_WRITE = Folder.READ_WRITE;
	
	public enum Protocol {
	    pop3, imap
	}
	
	/**
	 * 
	 * @see imap properties at https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
	 * @see pop3 properties at https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
     * @param protocol
     * @param mailProperties
	 * @throws SOSRequiredArgumentMissingException 
	 */
    public SOSMailReceiver(final Protocol protocol, final Map<String, Object> mailProperties)
            throws SOSRequiredArgumentMissingException {
        this.protocol = protocol;
        createSession(mailProperties);
    }

    public SOSMailReceiver(final Protocol protocol, final String host, final Integer port, final String user, final String password, boolean ssl,
            int timeout) throws SOSRequiredArgumentMissingException {
        this.host = host;
        this.port = port;
        this.user = user;
        this.protocol = protocol;
        this.password = password;
        createSession(getProperties(ssl, timeout));
    }

	private Session createSession(Map<String, Object> mailReceiverProps) throws SOSRequiredArgumentMissingException {
		Properties props = System.getProperties();
		
		if (mailReceiverProps != null) {
		    props.putAll(mailReceiverProps);
        }
		
		parseUser(props);
		parseHost(props);
		parsePassword(props);
		parsePort(props);
		if (!parseString("timeout", props).isPresent()) {
            props.put("mail." + protocol.name() + ".timeout", defaultTimeout);
        }
		
		authenticator = new SOSMailAuthenticator(user, password);
		session = Session.getInstance(props, authenticator);

		return session;
	}
	
    private void parseUser(Properties props) throws SOSRequiredArgumentMissingException {
        if (user == null) {
            parseString("user", props).ifPresent(val -> user = val);
        }
        if (user == null) {
            throw new SOSRequiredArgumentMissingException("'mail." + protocol.name() + ".user' is missing but required");
        }
    }
	
	private void parseHost(Properties props) throws SOSRequiredArgumentMissingException {
	    if (host == null) {
	        parseString("host", props).ifPresent(val -> host = val);
	    }
	    if (host == null) {
            throw new SOSRequiredArgumentMissingException("'mail." + protocol.name() + ".host' is missing but required");
        }
    }
	
	private void parsePassword(Properties props) throws SOSRequiredArgumentMissingException {
        if (password == null) {
            parseString("password", props).ifPresent(val -> password = val);
        }
        if (password == null) {
            password = "";
        }
    }
	
	private void parsePort(Properties props) {
        if (port == null) {
            parse("port", props).ifPresent(val -> port =  (Integer) val);
        }
    }
	
	private Optional<String> parseString(String key, Properties props) {
        String propPrefix = "mail." + protocol.name() + ".";
        String prop = props.getProperty(propPrefix + key);
        if (!SOSString.isEmpty(prop)) {
            return Optional.of(prop);
        }
        prop = props.getProperty("mail." + key);
        if (!SOSString.isEmpty(prop)) {
            return Optional.of(prop);
        }
        return Optional.empty();
    }
	
	private Optional<Object> parse(String key, Properties props) {
        String propPrefix = "mail." + protocol.name() + ".";
        Object prop = props.get(propPrefix + key);
        if (prop != null) {
            return Optional.of(prop);
        }
        prop = props.get("mail." + key);
        if (prop != null) {
            return Optional.of(prop);
        }
        return Optional.empty();
    }
	
	private Map<String, Object> getProperties(boolean ssl, int timeout) {
	    String propPrefix = "mail." + protocol.name() + ".";
	    Map<String, Object> props = new HashMap<>();
	    props.put("mail.store.protocol", protocol.name());
	    if (host != null) {
	        props.put(propPrefix + "host", host);
        }
	    if (port != null) {
            props.put(propPrefix + "port", port);
        }
	    if (timeout > 0) {
	        props.put(propPrefix + "timeout", timeout);
	    }
        if (ssl) {
            props.put(propPrefix + "ssl.enable", true);
        }
	    return props;
	}

	public void connect() throws MessagingException {
		store = session.getStore(protocol.name());
		store.connect(host, user, password);
		LOGGER.debug("..connection to host [" + host + ":" + port + "] successfully established.");
	}
	
	public String getHostPort() {
	    return host + ":" + port;
	}

	/**
	 * opens the given folder
	 * 
	 * @param folderName
	 * @param mode
	 *            The open mode of this folder. The open mode is Folder.READ_ONLY,
	 *            Folder.READ_WRITE, or -1 if not known.
	 * @return folder
	 * @throws MessagingException
	 */
	public Folder openFolder(final String folderName, final int mode) throws MessagingException {
		folder = store.getFolder(folderName);
		if (folder == null) {
			throw new MessagingException("Could not open [" + folderName + "]");
		}
		folder.open(mode);
        return folder;
	}

	/**
	 * opens the default folder
	 *
	 * @param mode
	 *            The open mode of this folder. The open mode is Folder.READ_ONLY,
	 *            Folder.READ_WRITE, or -1 if not known.
	 * @return folder
	 * @throws MessagingException
	 */
	public Folder openFolder(final int mode) throws MessagingException {
		folder = store.getDefaultFolder();
		if (folder == null) {
			throw new MessagingException("Could not open default folder");
		}
		folder.open(mode);
		return folder;
	}

	/**
	 * @param expunge
	 *            expunges all deleted messages if this flag is true.
	 * @throws MessagingException
	 */
	public void closeFolder(final boolean expunge) throws MessagingException {
		if (folder != null && folder.isOpen()) {
			folder.close(expunge);
			folder = null;
		}
	}

	public void disconnect() throws MessagingException {
		if (store != null) {
			store.close();
			store = null;
		}
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public String getFolderName() {
		return folder == null ? "" : folder.getName();
	}
	
	public String getDefaultFolderName() throws MessagingException {
        return store.getDefaultFolder().getName();
    }

	public Session getSession() {
		return session;
	}

}