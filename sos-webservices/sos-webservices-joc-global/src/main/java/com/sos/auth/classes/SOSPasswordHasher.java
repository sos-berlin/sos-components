package com.sos.auth.classes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSPasswordHasher {

	private static final Logger LOGGER = LoggerFactory.getLogger(SOSPasswordHasher.class);

	public static final String HASH_PREFIX = "$JS7-1.0";
	private static final String DEFAULT_PRIVATE_SALT = "Open Source JobScheduler";
	private static final int SALT_SIZE = 16;
	private static final int HASH_GAP = 300;
	private static final int HASH_ITERATIONS = 65536;

	private static byte[] getRandomSalt() throws NoSuchAlgorithmException {
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		byte[] salt = new byte[SALT_SIZE];
		sr.nextBytes(salt);
		return salt;
	}

	private static String byte2EncodedString(byte[] randomSalt) throws NoSuchAlgorithmException {
		String saltString = new String(org.apache.commons.codec.binary.Base64.encodeBase64(randomSalt));
		return saltString;
	}

	private static boolean isHashed(String hash) {
		return ((hash.startsWith(HASH_PREFIX)));
	}

	private static String hash(String pwd, int iterations, byte[] randomSalt)
			throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {

		final long timeStart = System.currentTimeMillis();

		if (isHashed(pwd)) {
			return pwd;
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(randomSalt);
		outputStream.write(DEFAULT_PRIVATE_SALT.getBytes());

		byte salt[] = outputStream.toByteArray();

		KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, iterations, 512);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
		byte[] hash = factory.generateSecret(spec).getEncoded();

		String saltString = byte2EncodedString(randomSalt);
		String hashString = byte2EncodedString(hash);

		String hashedPwd = String.format("%s$%s$%s$%s", HASH_PREFIX, iterations, saltString, hashString);

		final long timeEnd = System.currentTimeMillis();
		Long gap = HASH_GAP - (timeEnd - timeStart);
		if (gap > 0) {
			try {
				java.lang.Thread.sleep(gap);
			} catch (InterruptedException e) {
			}
		}
		return hashedPwd;

	}

	public static String hash(String pwd, int iterations) throws InvalidKeySpecException, NoSuchAlgorithmException {

		byte[] salt = getRandomSalt();
		String hashedPwd;
		try {
			hashedPwd = hash(pwd, iterations, salt);
		} catch (IOException e) {
			hashedPwd = "";
		}

		return hashedPwd;

	}

	public static String hash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return hash(password, HASH_ITERATIONS);
	}

	public static boolean isHashSupported(String hashString) {
		return hashString.contains("HASH|V1$");
	}

	public static boolean verify(String pwd, String hashedPassword) {

		String s = hashedPassword;
		boolean verified = false;
		
		if (pwd == null) {
		    return false;
		}

		String[] hashParts = s.split("\\$");
		if (hashParts.length > 3 && HASH_PREFIX.equals("$" + hashParts[1])) {
			String iterationsFromHash = hashParts[2];
			int interations = Integer.valueOf(iterationsFromHash);
			String publicSaltFromHash = hashParts[3];
			byte[] publicSalt = Base64.getDecoder().decode(publicSaltFromHash.getBytes());
			try {
				verified = hashedPassword.equals(hash(pwd, interations, publicSalt));
			} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
				LOGGER.warn(e.getMessage());
			}
		}

		return verified;

	}
}