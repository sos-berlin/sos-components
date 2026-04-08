package com.sos.jitl.jobs.diagnosis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;

public class CollectLogs extends Job<CollectLogsArguments> {

	public static final ObjectMapper objectMapper;

	public CollectLogs(JobContext jobContext) {
		super(jobContext);
	}

	@Override
	public void processOrder(OrderProcessStep<CollectLogsArguments> step) throws Exception {

		CollectLogsArguments args = step.getDeclaredArguments();
		OrderProcessStepLogger logger = step.getLogger();
		String controllerId = step.getControllerId();

		if (controllerId == null || controllerId.isBlank()) {
			throw new IllegalArgumentException("Unable to get the Controller ID");
		}

		Path logsDir = Paths.get(controllerId);
		if (!Files.exists(logsDir) || !Files.isDirectory(logsDir)) {
			throw new IllegalArgumentException("Invalid agent logs directory: " + logsDir);
		}

		String archiveArg = args.getArchiveLogs().getValue();
		if (archiveArg == null || archiveArg.isBlank()) {
			throw new IllegalArgumentException("archive_logs argument is required.");
		}

		Path archivePath = Paths.get(archiveArg);

		if (!archivePath.isAbsolute()) {
			String workDirEnv = System.getenv("JS7_AGENT_WORK_DIR");
			if (workDirEnv == null) {
				workDirEnv = System.getProperty("JS7_AGENT_WORK_DIR");
			}
			if (workDirEnv == null || workDirEnv.isBlank()) {
				throw new IllegalStateException("JS7_AGENT_WORK_DIR environment variable not set.");
			}
			archivePath = Paths.get(workDirEnv).resolve(archivePath).normalize();
		}

		if (archivePath.getParent() != null) {
			Files.createDirectories(archivePath.getParent());
		}

		logger.debug("Creating archive at: " + archivePath);

		try (ZipOutputStream zos = new ZipOutputStream(
				Files.newOutputStream(archivePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {

			try (Stream<Path> files = Files.list(logsDir)) {
				for (Path file : files.filter(Files::isRegularFile).toList()) {

					String fileName = file.getFileName().toString();

					Path anonymizedFile = null;
					Path tempCopy = null;

					try {
						// Try anonymizing original file
						anonymizedFile = AnonymizationUtils.anonymizePossiblyCompressed(file);

						addFileToZip(anonymizedFile, fileName, zos);
						logger.info("Added anonymized file: " + fileName);

					} catch (AccessDeniedException ade) {
						logger.info("Access denied for file " + fileName + ", creating copy.");

						try {
							// create temporary copy of locked file
							tempCopy = Files.createTempFile("log_copy_", "_" + fileName);
							Files.copy(file, tempCopy, StandardCopyOption.REPLACE_EXISTING);

							// anonymize copied file
							anonymizedFile = AnonymizationUtils.anonymizePossiblyCompressed(tempCopy);

							// zip anonymized copy
							addFileToZip(anonymizedFile, fileName, zos);
							logger.info("Added anonymized copy: " + fileName);

						} catch (Exception ex) {
							throw new RuntimeException("Failed processing locked file: " + fileName, ex);
						}

					} catch (Exception ex) {
						throw new RuntimeException("Failed adding file to zip: " + fileName, ex);

					} finally {
						// cleanup temp files
						try {
							if (anonymizedFile != null) {
								Files.deleteIfExists(anonymizedFile);
								if (anonymizedFile.getParent() != null) {
									Files.deleteIfExists(anonymizedFile.getParent());
								}
							}
							if (tempCopy != null) {
								Files.deleteIfExists(tempCopy);
							}
						} catch (Exception ignored) {
						}
					}
				}
			}
		}

		logger.info("Archive created successfully at: " + archivePath);
	}

	private void addFileToZip(Path file, String entryName, ZipOutputStream zos) throws IOException {

		ZipEntry entry = new ZipEntry(entryName);
		zos.putNextEntry(entry);

		try (InputStream in = Files.newInputStream(file, StandardOpenOption.READ)) {
			in.transferTo(zos);
		}

		zos.closeEntry();
	}

	

	static {
		objectMapper = (new ObjectMapper()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
	}
}
