package com.sos.jitl.jobs.diagnosis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.loganonymizer.SOSLogAnonymizerExecuter;

public class AnonymizationUtils {
	
	public static Path anonymizeLogFile(Path originalFile) throws Exception {

	    Path tempDir = Files.createTempDirectory("anon_logs_");

	    SOSLogAnonymizerExecuter anonymizer = new SOSLogAnonymizerExecuter();
	    anonymizer.setLogfiles(originalFile.toString());
	    anonymizer.setOutputdir(tempDir.toString());
	    anonymizer.executeSubstitution();

	    Path result = tempDir.resolve("anonymized-" + originalFile.getFileName());

	    if (!Files.exists(result)) {
	        throw new IOException("Anonymized file not created: " + result);
	    }

	    return result;
	}
	
	public static Path decompressGzip(Path gzipFile) throws IOException {
	    Path tempFile = Files.createTempFile("decompressed_", ".log");

	    try (
	        GZIPInputStream gis = new GZIPInputStream(Files.newInputStream(gzipFile));
	        OutputStream out = Files.newOutputStream(tempFile)
	    ) {
	        gis.transferTo(out);
	    }

	    return tempFile;
	}
	
	public static Path compressGzip(Path file) throws IOException {
	    Path gzipFile = Files.createTempFile("anonymized_", ".gz");

	    try (
	        InputStream in = Files.newInputStream(file);
	        GZIPOutputStream gos = new GZIPOutputStream(Files.newOutputStream(gzipFile))
	    ) {
	        in.transferTo(gos);
	    }

	    return gzipFile;
	}
	
	public static Path anonymizePossiblyCompressed(Path file) throws Exception {

	    try (InputStream in = Files.newInputStream(file)) {

	        if (SOSLogAnonymizerExecuter.isGZipped(in)) {

	            Path decompressed = decompressGzip(file);
	            Path anonymized = anonymizeLogFile(decompressed);
	            Path recompressed = compressGzip(anonymized);

	            Files.deleteIfExists(decompressed);
	            Files.deleteIfExists(anonymized);
	            Files.deleteIfExists(anonymized.getParent());

	            return recompressed;

	        } else {
	            return anonymizeLogFile(file);
	        }
	    }
	}
}
