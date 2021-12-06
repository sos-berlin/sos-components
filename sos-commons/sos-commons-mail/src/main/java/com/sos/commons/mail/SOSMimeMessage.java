package com.sos.commons.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSDate;


public class SOSMimeMessage {

	private static final Logger LOGGER = LoggerFactory.getLogger(SOSMimeMessage.class);
	protected ByteArrayOutputStream raw_email_byte_stream = null;
	protected String queueDir = "";
	private MimeMessage mimeMessage = null;
	private String localDateFormat = "yyyy-MM-dd HH:mm:ss";
	private String sentDateAsString;
	private String from;
	private String fromName;
	private String fromAddress;
	private final String queuePattern = "yyyy-MM-dd.HHmmss.S";
	private final String queuePrefix = "sos.";
	private String dumpedFileName = "";
	private Vector<SOSMailAttachment> sosMailAttachmentList = new Vector<SOSMailAttachment>();

	public SOSMimeMessage(final Message message) throws SOSMissingDataException, MessagingException, IOException, SOSInvalidDataException {
		this((MimeMessage) message);
	}

	public SOSMimeMessage(final Message message, Boolean harmless) {
		this.mimeMessage = (MimeMessage) message;
	}

	public SOSMimeMessage(final MimeMessage mimeMessage) throws SOSMissingDataException, MessagingException, IOException, SOSInvalidDataException {
		this.mimeMessage = mimeMessage;
		init();
	}

	public MimeMessage getMessage() {
		return mimeMessage;
	}

	public void init() throws MessagingException, IOException, SOSMissingDataException, SOSInvalidDataException {
		if (this.mimeMessage != null) {
			setSentDateAsString();
			setFrom();
			setFromName();
			setFromAddress();
			boolean isUnseen = !this.mimeMessage.getFlags().contains(Flags.Flag.SEEN);
			processAttachment(this.mimeMessage);
			if (isUnseen) this.mimeMessage.setFlag(Flag.SEEN, false);
		}
	}

	public String getFrom() {
		return from;
	}

	private final void setFrom() throws MessagingException, SOSMissingDataException {
		if (mimeMessage.getFrom() != null && mimeMessage.getFrom().length > 0) {
			from = mimeMessage.getFrom()[0].toString();
		} else {
			throw new SOSMissingDataException("From is empty or null!");
		}
	}

	public void setFromName() throws MessagingException {
		fromName = ((InternetAddress) mimeMessage.getFrom()[0]).getPersonal();
	}

	public String getFromName() {
		return fromName;
	}

	private void setFromAddress() throws MessagingException {
		fromAddress = ((InternetAddress) mimeMessage.getFrom()[0]).getAddress();
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public String getSubject() throws UnsupportedEncodingException, MessagingException {
		String subject = "";
		if (mimeMessage.getSubject() != null) {
			subject = MimeUtility.decodeText(mimeMessage.getSubject());
		}
		return subject;
	}

	public String getSentDateAsString(final String dateFormat) throws MessagingException, SOSInvalidDataException {
		return SOSDate.format(mimeMessage.getSentDate(), dateFormat);
	}

	public final void setSentDateAsString() throws MessagingException, SOSInvalidDataException {
		if (mimeMessage.getSentDate() != null) {
			sentDateAsString = SOSDate.format(mimeMessage.getSentDate(), localDateFormat);
		}
	}

	public String getSentDateAsString() {
		return sentDateAsString;
	}

	public void deleteMessage() throws MessagingException {
		mimeMessage.setFlag(Flags.Flag.DELETED, true);
		LOGGER.debug("[" + mimeMessage.getMessageID() + "] marked for delete.");
	}

	public Date getSentDate() throws MessagingException {
		return mimeMessage.getSentDate();
	}

	public int getMessageNumber() {
		return mimeMessage.getMessageNumber();
	}

	public String getMessageId() throws MessagingException {
		return mimeMessage.getMessageID().replaceAll("(^<?)|(>?$)", "");
	}

	public List<String> getRecipientAddress(String recipientType) throws MessagingException, SOSInvalidDataException, UnsupportedEncodingException {
		List<String> recipientAddressList = new Vector<String>();
		InternetAddress[] internetAddress = null;
		recipientType = recipientType.toUpperCase();
		if ("TO".equals(recipientType)) {
			internetAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);
		} else if ("CC".equals(recipientType)) {
			internetAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);
		} else if ("BCC".equals(recipientType)) {
			internetAddress = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);
		} else {
			throw new SOSInvalidDataException("Invalid recipient type!!");
		}
		if (internetAddress != null) {
			for (InternetAddress internetAddres : internetAddress) {
				if (internetAddres.getAddress() != null) {
					recipientAddressList.add(MimeUtility.decodeText(internetAddres.getAddress()));
				}
			}
		}
		return recipientAddressList;
	}

	public String getLocalDateFormat() {
		return localDateFormat;
	}

	public void setLocalDateFormat(final String localDateFormat) throws MessagingException, SOSInvalidDataException {
		this.localDateFormat = localDateFormat;
		setSentDateAsString();
	}

	public List<String> getBodyText() throws MessagingException, IOException {
		return getBodyText(mimeMessage);
	}

	private List<String> getBodyText(final Part part) throws IOException, MessagingException {
		Object content = part.getContent();
		String disposition = part.getDisposition();
		List<String> textContent = new Vector<String>();
		if (content instanceof String && disposition != null && !disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
			textContent.addAll(toList((String) content));
			return toList((String) content);
		} else if (content instanceof Multipart) {
			int count = ((Multipart) content).getCount();
			for (int i = 0; i < count; i++) {
				BodyPart bodyPart = ((Multipart) content).getBodyPart(i);
				textContent.addAll(getBodyText(bodyPart));
			}
			return textContent;
		} else {
			return new Vector<String>();
		}
	}

	public String getPlainTextBody() throws IOException, MessagingException {
		Object content = mimeMessage.getContent();
		if (mimeMessage.isMimeType("text/plain")) {
			return (String) content;
		} else if (mimeMessage.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) mimeMessage.getContent();
			int numParts = mp.getCount();
			for (int i = 0; i < numParts; ++i) {
				if (mp.getBodyPart(i).isMimeType("text/plain")) {
					if (mp.getBodyPart(i).getContent() == null) {
						return "";
					} else {
						return (String) mp.getBodyPart(i).getContent();
					}
				} else if (mp.getBodyPart(i).isMimeType("multipart/*")) {
					MimeMultipart mmp = (MimeMultipart) mp.getBodyPart(i).getContent();
					int numBodyParts = mmp.getCount();
					for (int j = 0; j < numBodyParts; ++j) {
						if (mmp.getBodyPart(j).isMimeType("text/plain")) {
							if (mmp.getBodyPart(j).getContent() == null) {
								return "";
							} else {
								return (String) mmp.getBodyPart(j).getContent();
							}
						}
					}
				}
			}
			return "";
		} else {
			return "";
		}
	}

	/**
	 * bounced message has an empty return-path and multipart/report header
	 *
	 * @param message
	 * @return
	 * @throws MessagingException
	 */
	public boolean isBounce() throws MessagingException {
		boolean emptyReturnPath = false;
		if (mimeMessage.getHeader("Return-Path") != null) {
			emptyReturnPath = mimeMessage.getHeader("Return-Path")[0].matches("(.*)(\\s*\\<\\s*\\>\\s*)(.*)");
		}
		return mimeMessage.isMimeType("multipart/report") && emptyReturnPath;
	}

	/**
	 * returns comma separated string
	 * 
	 * @param array
	 * @return string
	 */
	public static String arrayToString(final String[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			if (i < array.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

    public SOSMimeMessage getAttachedSosMimeMessage(final Session session, final byte[] messageContent) throws SOSInvalidDataException,
            MessagingException, SOSMissingDataException, IOException {
        if (messageContent == null) {
            throw new SOSInvalidDataException("messageContent has null value!!");
        }
        InputStream messageInputStream = new ByteArrayInputStream(messageContent);
        return new SOSMimeMessage(new MimeMessage(session, messageInputStream));
    }

	/**
	 * Returns a list of all sosMailAttachments in the message.
	 *
	 * @return the sosMailAttachment in the message
	 */
	public Vector<SOSMailAttachment> getSosMailAttachments() {
		return sosMailAttachmentList;
	}

	/**
	 * Returns the number of attachments in the message.
	 *
	 * @return the number of attachments in the message
	 */
	public int getSosMailAttachmentsCount() {
		return sosMailAttachmentList.size();
	}

	/**
	 * @param in
	 * @return
	 * @throws IOException
	 */
	private byte[] getByteArray(final InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buffer = new byte[8192];
			int count = 0;
			while ((count = in.read(buffer)) >= 0) {
				bos.write(buffer, 0, count);
			}
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				//
			}
			try {
				if (bos != null) {
					bos.close();
				}
			} catch (Exception e) {
				//
			}
		}
		return bos.toByteArray();
	}

	public void processAttachment(final Part part) throws MessagingException, IOException {
		String extension = "";
		boolean hasAttachment = false;
		String fileName = null;
		if (part.isMimeType("multipart/*")) {
			Multipart multipart = (Multipart) part.getContent();
			int count = multipart.getCount();
			for (int i = 0; i < count; i++) {
				processAttachment(multipart.getBodyPart(i));
			}
		} else if (part.isMimeType("message/rfc822")) {
			hasAttachment = true;
			extension = ".eml";
		} else if (part.isMimeType("text/html")) {
			extension = ".html";
			hasAttachment = true;
		} else {
			hasAttachment = true;
		}
		if (hasAttachment) {
			SOSMailAttachment sosMailAttachment = new SOSMailAttachment();
			String disposition = part.getDisposition();
			if (disposition == null || disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
				fileName = part.getFileName();
			}
			if (fileName != null) {
				sosMailAttachment.setFilename(MimeUtility.decodeText(part.getFileName()));
			} else {
				sosMailAttachment.setFilename(fileName);
			}
			sosMailAttachment.setContentType(part.getContentType());
			sosMailAttachment.setContent(getByteArray(part.getInputStream())); // set implicit Flag.SEEN
			sosMailAttachment.setFileExtension(extension);
			sosMailAttachmentList.add(sosMailAttachment);
		}
	}

	private static List<String> toList(final String string) {
		List<String> list = new Vector<String>();
		list.add(string);
		return list;
	}

	public void saveFile(File file, final byte[] input) throws IOException {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		File saveFile = file;
		try {
			for (int i = 0; file.exists(); i++) {
				file = new File(saveFile.getAbsolutePath() + "_" + i);
			}
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(input);
		} finally {
			if (bos != null) {
				try {
					bos.flush();
				} catch (Exception e) {
					//
				}
			}
			if (bos != null) {
				try {
					bos.close();
					bos = null;
				} catch (Exception e) {
					//
				}
			}
		}
	}

	/**
	 * Saves the specified attachment.
	 *
	 * @param sosMailAttachment
	 *            the attachment to be saved.
	 * @param targetFolderName
	 * @throws SOSInvalidDataException 
	 * @throws IOException
	 */
    public void saveAttachment(final SOSMailAttachment sosMailAttachment, final String targetFolderName) throws SOSInvalidDataException, IOException {
        String attachmentFileName = null;
        LOGGER.debug("Calling saveAttachment");
        if (!new File(targetFolderName).exists()) {
            throw new SOSInvalidDataException("File [" + targetFolderName + "] does not exists!");
        }
        attachmentFileName = sosMailAttachment.getFilename();
        if (attachmentFileName == null) {
            attachmentFileName = "attach";
        }
        attachmentFileName = targetFolderName + File.separator + attachmentFileName;
        saveFile(new File(attachmentFileName), sosMailAttachment.getContent());
        LOGGER.debug(".. saving attachment file [" + sosMailAttachment.getFilename() + "] successfully done.");
    }

	private String normalize(String f) {
		f = f.replace(":", "_");
		f = f.replace("\\", "_");
		return f;
	}

	public void saveAttachments(SOSMimeMessage message, String attachmentFileNamePattern, final String targetFolderName,
			boolean saveFilesWithoutFilename) throws SOSInvalidDataException, IOException, MessagingException {
		String attachmentFileName = null;
		if (attachmentFileNamePattern == null || attachmentFileNamePattern.isEmpty()) {
			attachmentFileNamePattern = "${filename}";
		}
		File fileToSave;
		SOSMailAttachment attachment;
		LOGGER.debug("Calling saveAttachments");
		if (!new File(targetFolderName).exists()) {
			throw new SOSInvalidDataException("File [" + targetFolderName + "] does not exists!!");
		}
		Iterator<SOSMailAttachment> it = sosMailAttachmentList.iterator();

		int count = 0;
		for (; it.hasNext();) {
			attachment = it.next();
			LOGGER.debug(".. attachment count byte [" + attachment.getContent().length + "]");
			attachmentFileName = attachment.getFilename();
			if (attachmentFileName == null && saveFilesWithoutFilename) {
				attachmentFileName = "attach";
			}
			if (attachmentFileName != null) {
				attachmentFileName = attachmentFileNamePattern.replace("${filename}", attachmentFileName);
				attachmentFileName = attachmentFileName.replace("${messageId}", message.getMessageId());
				attachmentFileName = attachmentFileName.replace("${subject}", message.getSubject());
				attachmentFileName = normalize(attachmentFileName);
				fileToSave = new File(targetFolderName, attachmentFileName);
				saveFile(fileToSave, attachment.getContent());
				count++;
				LOGGER.debug(".. attachment file [" + attachment.getFilename() + "] successfully saved.");
			}
		}
		LOGGER.debug(".. [" + count + "] attachment(s) saved to " + targetFolderName);
	}

	public void dumpMessage(final File file, final boolean append) throws IOException, MessagingException {
		dumpFile(file, mimeMessage.getInputStream(), append);
	}

	public void setFlag(final Flags.Flag flag, final boolean set) throws MessagingException {
		mimeMessage.setFlag(flag, set);
	}

	public Object getContent() throws IOException, MessagingException {
		return mimeMessage.getContent();
	}

	private final void dumpFile(final File file, final InputStream input, final boolean append) throws IOException {
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;
		int b;
		try {
			fos = new FileOutputStream(file, append);
			bos = new BufferedOutputStream(fos);
			bis = new BufferedInputStream(input);
			while ((b = bis.read()) != -1) {
				bos.write(b);
			}
		} finally {
			if (bos != null) {
				bos.flush();
				bos.close();
			}
			if (fos != null) {
				fos.close();
			}
			if (bis != null) {
				bis.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Enumeration<Header> getHeaders() throws MessagingException {
		return mimeMessage.getAllHeaders();
	}

	@SuppressWarnings("unchecked")
	public String dumpHeaders() throws IOException, MessagingException {
		StringBuilder sb = new StringBuilder();
		Header header = null;
		for (Enumeration<Header> e = mimeMessage.getAllHeaders(); e.hasMoreElements();) {
			header = e.nextElement();
			sb.append("\n").append(header.getName()).append(": ").append(header.getValue());
		}
		return sb.toString();
	}

	public void dumpMessageToFile(final File file, final boolean withAttachment, final boolean append) throws IOException {
		FileOutputStream out = null;
		File tmpFile;
		File targetFile;
		try {
			tmpFile = new File(file.getAbsolutePath() + "~");
			out = new FileOutputStream(file, append);
			out.write(dumpMessage(withAttachment));
			targetFile = new File(file.getAbsolutePath().replaceFirst("~$", ""));
			tmpFile.delete();
			tmpFile.renameTo(targetFile);
		} catch (Exception e) {
			throw new IOException("dumpMessageToFile: An error occurred on dumping: " + e.toString());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
					//
				}
			}
		}
	}

	public final MimeMessage getMimeMessage() {
		return mimeMessage;
	}

	private ByteArrayOutputStream messageRemoveAttachments() throws MessagingException, IOException {
		ByteArrayOutputStream raw_email_byte_stream_without_attachment = new ByteArrayOutputStream();
		Object mmpo = mimeMessage.getContent();
		if (mmpo instanceof MimeMultipart) {
			MimeMultipart mmp = (MimeMultipart) mmpo;
			if (mimeMessage.isMimeType("multipart/*")) {
				mmp = (MimeMultipart) mimeMessage.getContent();
				for (int i = 1; i < mmp.getCount(); i++) {
					@SuppressWarnings("unused")
					BodyPart part = mmp.getBodyPart(i);
					mmp.removeBodyPart(i);
					i--;
				}
			}
			mimeMessage.setContent(mmp);
		}
		mimeMessage.writeTo(raw_email_byte_stream_without_attachment);
		return raw_email_byte_stream_without_attachment;
	}

	public String dumpMessageAsString() throws IOException, MessagingException {
		return dumpMessageAsString(false);
	}

	public void dumpMessageToFile(final boolean withAttachment, final boolean append) throws IOException {
		Date d = new Date();
		StringBuffer bb = new StringBuffer();
		SimpleDateFormat s = new SimpleDateFormat(queuePattern);
		FieldPosition fp = new FieldPosition(0);
		StringBuffer b = s.format(d, bb, fp);
		dumpedFileName = queueDir + queuePrefix + b + ".email";
		File file = new File(dumpedFileName);
		while (file.exists()) {
			b = s.format(d, bb, fp);
			dumpedFileName = queueDir + "/" + queuePrefix + b + ".email";
			file = new File(dumpedFileName);
		}
		dumpMessageToFile(file, withAttachment, append);
	}

	public void dumpMessageToFile(final String filename, final boolean withAttachment, final boolean append) throws IOException {
		dumpMessageToFile(new File(filename), withAttachment, append);
	}

	public String dumpMessageAsString(final boolean withAttachment) throws IOException, MessagingException {
		byte[] bytes;
		ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;
		if (!withAttachment) {
			raw_email_byte_stream_without_attachment = messageRemoveAttachments();
		}
		raw_email_byte_stream = new ByteArrayOutputStream();
		mimeMessage.writeTo(raw_email_byte_stream);
		if (withAttachment || raw_email_byte_stream_without_attachment == null) {
			bytes = raw_email_byte_stream.toByteArray();
		} else {
			bytes = raw_email_byte_stream_without_attachment.toByteArray();
		}
		String s = new String(bytes);
		return s;
	}

	public byte[] dumpMessage() throws IOException, MessagingException {
		return dumpMessage(true);
	}

	public byte[] dumpMessage(final boolean withAttachment) throws IOException, MessagingException {
		byte[] bytes;
		ByteArrayOutputStream raw_email_byte_stream_without_attachment = null;
		if (!withAttachment) {
			raw_email_byte_stream_without_attachment = messageRemoveAttachments();
		}
		raw_email_byte_stream = new ByteArrayOutputStream();
		mimeMessage.writeTo(raw_email_byte_stream);
		if (withAttachment || raw_email_byte_stream_without_attachment == null) {
			bytes = raw_email_byte_stream.toByteArray();
		} else {
			bytes = raw_email_byte_stream_without_attachment.toByteArray();
		}
		return bytes;
	}

	public String getQueueDir() {
		return queueDir;
	}

	public void setQueueDir(String queueDir) {
		if (!queueDir.endsWith("/")) {
			queueDir += "/";
		}
		this.queueDir = queueDir;
	}

	@SuppressWarnings("unchecked")
	public int incrementHeader(final String headerName) throws MessagingException {
		Header header = null;
		int value = -1;
		for (Enumeration<Header> e = mimeMessage.getAllHeaders(); e.hasMoreElements();) {
			header = e.nextElement();
			if (header.getName().equalsIgnoreCase(headerName) && header.getValue() != null
					&& header.getValue().trim().matches("^[0-9]+$")) {
				value += Integer.parseInt(header.getValue());
				mimeMessage.setHeader(headerName, "" + value);
			}
		}
		return value;
	}

	public void setHeader(final String headerName, final String HeaderValue) throws MessagingException {
		mimeMessage.setHeader(headerName, HeaderValue);
	}

	/**
	 * returns the value of the specified header if header exists otherwise null.
	 *
	 * @param headerName
	 * @return String represents the value of the specified header otherwise null.
	 * @throws MessagingException
	 */
	@SuppressWarnings("unchecked")
	public String getHeaderValue(final String headerName) throws MessagingException {
		Header header = null;
		for (Enumeration<Header> e = mimeMessage.getAllHeaders(); e.hasMoreElements();) {
			header = e.nextElement();
			if (header.getName().equalsIgnoreCase(headerName)) {
				return header.getValue();
			}
		}
		return null;
	}

	public String getDumpedFileName() {
		return dumpedFileName;
	}

	public void setDumpedFileName(final String dumpedFileName) {
		this.dumpedFileName = dumpedFileName;
	}

	public String getFirstToRecipient() throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		return getRecipient("TO", 0);
	}

	public String getToRecipient(int index) throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		return getRecipient("TO", index);
	}

	public String getFirstCCRecipient() throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		return getRecipient("CC", 0);
	}

	public String getCCRecipient(int index) throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		return getRecipient("CC", index);
	}

	public String getFirstBCCRecipient() throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		return getRecipient("BCC", 0);
	}

	public String getBCCRecipient(int index) throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		return getRecipient("BCC", index);
	}

	public String getRecipient(String type, int index) throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		List<String> l = this.getRecipientAddress(type);
		if (l.size() > index) {
			return l.get(index);
		} else {
			return "";
		}
	}

	public String getFirstRecipient(String type) throws SOSInvalidDataException, UnsupportedEncodingException, MessagingException {
		List<String> l = this.getRecipientAddress(type);
		if (l.size() > 0) {
			return l.get(0);
		} else {
			return "";
		}
	}
}