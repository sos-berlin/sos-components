package com.sos.jitl.jobs.rest;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.keystore.KeyStoreContainer;
import com.sos.commons.util.keystore.KeyStoreType;
import com.sos.commons.util.ssl.SslArguments;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.OrderProcessStep;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class JobResourceUtils {

	
	//extracting the truststore details
	private static <T extends JobArguments> List<KeyStoreContainer>  getTrustStoreContainersFromOrder (OrderProcessStep<T> step) throws Exception {
	
		String enc_path = Optional
				.ofNullable(step.getAllArguments().get("encipherment_private_key_path"))
				.map(JobArgument::getValue).map(Object::toString).orElse("").trim();
		
		String trustStores = Optional.ofNullable(step.getAllArguments().get("truststore_file"))
				.map(JobArgument::getValue).filter(Objects::nonNull).map(Object::toString).orElse("");

		if (SOSString.isEmpty(trustStores)) {
			return null;
		}
		
		if (!trustStores.isBlank()&& trustStores.startsWith("enc:")) {
			trustStores = EnciphermentDecryptor.decryptValue(trustStores, Paths.get(enc_path));
		}
		String[] arr = trustStores.split("\\|");
		String password = Optional.ofNullable(step.getAllArguments().get("truststore_password"))
				.map(JobArgument::getValue).filter(Objects::nonNull).map(Object::toString).orElse("");
		
		if (!password.isBlank()&& password.startsWith("enc:")) {
			password = EnciphermentDecryptor.decryptValue(password, Paths.get(enc_path));
		}
		
		String pass = SOSString.isEmpty(password) ? null : password;
		return Arrays.asList(arr).stream().peek(String::trim).map(path -> {
			if (SOSString.isEmpty(path)) {
				return null;
			}
			KeyStoreContainer c = new KeyStoreContainer(KeyStoreType.PKCS12, SOSPath.toAbsolutePath(path));
			if (!Files.exists(c.getPath())) {
				step.getLogger().warn("[order][TrustStore][%s]not found", c.getPath());
				return null;
			}
			c.setPassword(pass);
			return c;
		}).filter(Objects::nonNull).toList();
	}
	

	//extracting the keystore details
	private static <T extends JobArguments> KeyStoreContainer getKeyStoreContainerFromOrder(OrderProcessStep<T> step) throws Exception {
		String enc_path = Optional
				.ofNullable(step.getAllArguments().get("encipherment_private_key_path"))
				.map(JobArgument::getValue).map(Object::toString).orElse("").trim();
		String path = Optional.ofNullable(step.getAllArguments().get("keystore_file"))
				.map(JobArgument::getValue).filter(Objects::nonNull).map(Object::toString).orElse("");

		if (SOSString.isEmpty(path)) {
			return null;
		}

		if (path.startsWith("enc:")) {
			path = EnciphermentDecryptor.decryptValue(path, Paths.get(enc_path));
		}
		
		String alias = Optional.ofNullable(step.getAllArguments().get("keystore_alias"))
				.map(JobArgument::getValue).filter(Objects::nonNull).map(Object::toString).orElse("");

		if (!alias.isBlank()&& alias.startsWith("enc:")) {
			alias = EnciphermentDecryptor.decryptValue(alias, Paths.get(enc_path));
		}
		
		KeyStoreContainer c = new KeyStoreContainer(KeyStoreType.PKCS12, SOSPath.toAbsolutePath(path));
		if (!Files.exists(c.getPath())) {
			step.getLogger().warn("[order][KeyStore][%s]not found", c.getPath());
			return null;
		}
		
		String key_pass = Optional.ofNullable(step.getAllArguments().get("keystore_password"))
				.map(JobArgument::getValue).filter(Objects::nonNull).map(Object::toString).orElse("");
		
		if (!key_pass.isBlank()&& key_pass.startsWith("enc:")) {
			key_pass = EnciphermentDecryptor.decryptValue(key_pass, Paths.get(enc_path));
		}
		
		c.setPassword(key_pass);
		c.setKeyPassword(key_pass);
		c.setAliases(SOSString.isEmpty(alias) ? null : List.of(alias));
		
		return c;
	}

	
	//fetching the ssl arguments( keystore/ truststore) details
	public static <T extends JobArguments> SslArguments getSslArguments(OrderProcessStep<T> step) throws Exception {
		SslArguments args = new SslArguments();
		List<KeyStoreContainer> trustStoreContainers = getTrustStoreContainersFromOrder(step);

		args.getTrustedSsl().setTrustStoreContainers(trustStoreContainers);

		// KeyStore: 0->1
		KeyStoreContainer keyStoreContainer = getKeyStoreContainerFromOrder(step);

		args.getTrustedSsl().setKeyStoreContainer(keyStoreContainer);

		return args;
	}
}
