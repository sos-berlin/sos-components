package com.sos.joc.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSParameterSubstitutor;

public class JocCockpitProperties {
	private static final Logger LOGGER = LoggerFactory.getLogger(JocCockpitProperties.class);
	private Properties properties = new Properties();
	private String propertiesFile = "/joc/joc.properties";
	private Path propertiesPath;
	private SOSParameterSubstitutor parameterSubstitutor = new SOSParameterSubstitutor();
	private volatile long modTime = 0L;
	private volatile long log4jModTime = 0L;
	public static AtomicBoolean auditObjectsLoggerIsDefined = new AtomicBoolean(false);
	public static AtomicBoolean auditTrailLoggerIsDefined = new AtomicBoolean(false);

	public JocCockpitProperties() {
		readProperties();
		setLog4JConfiguration();
	}

	public JocCockpitProperties(String propertiesFile) {
		this.propertiesFile = propertiesFile;
		readProperties();
		setLog4JConfiguration();
	}

	public JocCockpitProperties(Path propertiesPath) {
		this.propertiesPath = propertiesPath;
		readPropertiesFromPath();
		setLog4JConfiguration();
	}

	public Properties getProperties() {
		return properties;
	}

	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	public String getPropertiesFile() {
		return propertiesFile;
	}

	public String getPropertiesFileClassPathParent() {
		Path p = Paths.get(propertiesFile).getParent();
		String parent = "";
		if (p != null) {
			parent = p.toString().replace('\\', '/') + "/";
		}
		return parent.replaceFirst("^/", "");
	}

	public String getProperty(String property) {
		String s = properties.getProperty(property);
		if (s != null) {
			s = parameterSubstitutor.replaceEnvVars(s);
			s = parameterSubstitutor.replace(s);
		}
		return s;
	}

	public String getProperty(String property, String defaultValue) {
		String s = getProperty(property);
		if (s == null) {
			return defaultValue;
		} else {
			return s;
		}
	}
	
	public long getProperty(String property, long defaultValue) {
        String s = getProperty(property);
        if (s == null) {
            return defaultValue;
        } else {
            try{
                return Long.parseLong(s);
            } catch (NumberFormatException e){
                LOGGER.warn(String.format("Property value for %1$s is not a Long. Returning default %2$s: %3$s", property, defaultValue, e.getMessage()));
                return defaultValue;
            }
        }
    }

	public int getProperty(String property, int defaultValue) {
		String s = getProperty(property);
		if (s == null) {
			return defaultValue;
		} else {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				LOGGER.warn(String.format("Property value for %1$s is not an Integer. Returning default %2$s: %3$s",
						property, defaultValue, e.getMessage()));
				return defaultValue;
			}
		}
	}

	public boolean getProperty(String property, boolean defaultValue) {
		String s = getProperty(property);
		if (s == null) {
			return defaultValue;
		} else {
			try {
				return Boolean.parseBoolean(s);
			} catch (Exception e) {
				LOGGER.warn(String.format(
						"Property value for %1$s could not be parsed to boolean. Returning default %2$s: %3$s",
						property, defaultValue, e.getMessage()));
				return defaultValue;
			}
		}
	}
	
	public void updateProperty(String property, String value) throws IOException {
	    if (value != null && propertiesPath != null) {
	        String s = getProperty(property);
	        if (s != null && !s.equals(value)) {
	            Predicate<String> p = Pattern.compile("^" + property + "\\s*=\\s*" + s).asPredicate();
	            
	            Files.write(propertiesPath, Files.lines(propertiesPath).map(line -> {
	                if (p.test(line)) {
	                    return line.replaceFirst("^(" + property + "\\s*=).*", "$1 " + value);
	                }
	                return line;
	                
	            }).collect(Collectors.toList()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	        }
	    }
	}

	public Path resolvePath(String path) {
		if (path != null) {
			Path p = getResourceDir();
			if (p != null) {
				p = p.resolve(path).normalize();
				LOGGER.debug(String.format("Resolved path of %1$s = %2$s", path, p.toString().replace('\\', '/')));
				return p;
			}
		}
		return null;
	}
	
	public void touchLog4JConfiguration() {
	    String propKeyLog4J = "log4j.configuration";
        String log4jConf = getProperty(propKeyLog4J);
        if (log4jConf != null) {
            Path p = resolvePath(log4jConf);
            if (p != null) {
                if (Files.exists(p)) {
                    try {
                        if (log4jModTime != Files.getLastModifiedTime(p).toMillis()) {
                            Configurator.reconfigure(p.toUri());
                            log4jModTime = Files.getLastModifiedTime(p).toMillis();
                            setAuditObjectsLoggerIsDefined(p);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                } else {
                    LOGGER.warn(String.format("%1$s=%2$s is set but couldn't find the file (%3$s).", propKeyLog4J, log4jConf, p.toString()));
                }
            }
        }
	}

    private void setLog4JConfiguration() {
        String propKeyLog4J = "log4j.configuration";
        String log4jConf = getProperty(propKeyLog4J);
        if (log4jConf != null) {
            Path p = resolvePath(log4jConf);
            if (p != null) {
                if (Files.exists(p)) {
                    try {
                        Configurator.reconfigure(p.toUri());
                        log4jModTime = Files.getLastModifiedTime(p).toMillis();
                        setAuditObjectsLoggerIsDefined(p);
                    } catch (Exception e) {
                        LOGGER.warn("", e);
                    }
                } else {
                    LOGGER.warn(String.format("%1$s=%2$s is set but couldn't find the file (%3$s).", propKeyLog4J, log4jConf, p.toString()));
                }
            }
        }
    }
    
    private void setAuditObjectsLoggerIsDefined(Path p) {
        Collection<LoggerConfig> lg = Configurator.initialize("joc_config", null, p.toUri()).getConfiguration().getLoggers().values();
        auditObjectsLoggerIsDefined.getAndSet(lg.stream().map(LoggerConfig::getName).anyMatch(l -> l.equals(WebserviceConstants.AUDIT_OBJECTS_LOGGER)));
        auditTrailLoggerIsDefined.getAndSet(lg.stream().map(LoggerConfig::getName).anyMatch(l -> l.equals(WebserviceConstants.AUDIT_TRAIL_LOGGER)));
    }
    
	private void substituteProperties() {
		parameterSubstitutor = new SOSParameterSubstitutor();
		for (Map.Entry<Object, Object> e : properties.entrySet()) {
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			parameterSubstitutor.addKey(key, value);
		}
	}

	public Path getResourceDir() {
		try {
			Path parentDirOfPropFilePath = Paths.get(propertiesFile).getParent();
			String parentDirOfPropFile = "/";
			if (parentDirOfPropFilePath != null && parentDirOfPropFilePath.getNameCount() != 0) {
				parentDirOfPropFile = parentDirOfPropFilePath.toString().replace('\\', '/');
			}
			URL url = this.getClass().getResource(parentDirOfPropFile);
			if (url != null) {
				Path p = Paths.get(url.toURI());
				if (Files.exists(p)) {
					return p;
				} else {
					LOGGER.error("Cannot determine resource path");
				}
			}
		} catch (Exception e) {
			LOGGER.error("Cannot determine resource path", e);
		}
		return null;
	}
	
	public boolean isChanged() {
	    long oldModTime = modTime;
	    setModTime();
	    return modTime > oldModTime;
	}

	private void readProperties() {
		InputStream stream = null;
		InputStreamReader streamReader = null;
		try {
			stream = this.getClass().getResourceAsStream(propertiesFile);
			if (stream != null) {
				streamReader = new InputStreamReader(stream, "UTF-8");
				properties.load(streamReader);
				substituteProperties();
				setPath();
			}
		} catch (Exception e) {
			LOGGER.error(String.format("Error while reading %1$s:", propertiesFile), e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (Exception e) {
			}
			try {
				if (streamReader != null) {
					streamReader.close();
				}
			} catch (Exception e) {
			}
		}
	}

	private void readPropertiesFromPath() {
		InputStream stream = null;
		InputStreamReader streamReader = null;
		try {
			if (propertiesPath != null) {
				stream = Files.newInputStream(propertiesPath);
				if (stream != null) {
					streamReader = new InputStreamReader(stream, "UTF-8");
					properties.load(streamReader);
					substituteProperties();
				}
			}
		} catch (Exception e) {
			LOGGER.error(String.format("Error while reading %1$s:", propertiesPath.toString()), e);
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
			} catch (Exception e) {
			}
			try {
				if (streamReader != null) {
					streamReader.close();
				}
			} catch (Exception e) {
			}
		}
	}
	
    private void setModTime() {
        if (propertiesPath != null) {
            try {
                modTime = Files.getLastModifiedTime(propertiesPath).toMillis();
            } catch (IOException e) {
                //LOGGER.warn("Error while determine modification date of " + propertiesPath.toString());
            }
        }
    }
	
    private void setPath() {
        if (propertiesFile != null) {
            try {
                URL url = this.getClass().getResource(propertiesFile);
                if (url != null) {
                    Path p = Paths.get(url.toURI());
                    if (Files.exists(p)) {
                        propertiesPath = p;
                    }
                }
            } catch (Exception e) {
                //
            }
        }
    }
}
