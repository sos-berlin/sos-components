package com.sos.commons.util.common;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOSEnvTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(SOSEnvTest.class);
	
	@Test
	public void testMerge() {
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("1", "(a)");
		map1.put("2", "(b)");
		map1.put("3", "(map1)");

		Map<String, String> map2 = new HashMap<String, String>();
		map2.put("3", "(map2)");
		map2.put("4", "(c)");
		map2.put("5", "(d)");

		SOSEnv env1 = new SOSEnv(map1);
		SOSEnv env2 = new SOSEnv(map2);
		
		SOSEnv mergedWithoutOverwrite = env1.merge(env2);
		SOSEnv mergedWithOverwrite = env1.merge(env2, true);
		
		LOGGER.info("SOSEnv 1:");
		env1.getEnvVars().entrySet().forEach(entry -> {
			LOGGER.info(String.format("key: %1$s - value: %2$s", entry.getKey(), entry.getValue()));
		});
		LOGGER.info("SOSEnv 2:");
		env2.getEnvVars().entrySet().forEach(entry -> {
			LOGGER.info(String.format("key: %1$s - value: %2$s", entry.getKey(), entry.getValue()));
		});
		LOGGER.info("new SOSEnv - env1.merge(env2) - merged without overwrite(default):");
		mergedWithoutOverwrite.getEnvVars().entrySet().forEach(entry -> {
			LOGGER.info(String.format("merged key: %1$s - merged value: %2$s", entry.getKey(), entry.getValue()));
		});
		assertEquals("(map1)", mergedWithoutOverwrite.getEnvVars().get("3"));
		LOGGER.info("new SOSEnv - env1.merge(env2, true) - merged with overwrite:");
		mergedWithOverwrite.getEnvVars().entrySet().forEach(entry -> {
			LOGGER.info(String.format("merged key: %1$s - merged value: %2$s", entry.getKey(), entry.getValue()));
		});
		assertEquals("(map2)", mergedWithOverwrite.getEnvVars().get("3"));
	}
	
}
