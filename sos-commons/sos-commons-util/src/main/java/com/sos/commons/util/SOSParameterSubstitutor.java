package com.sos.commons.util;

import java.io.File;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;

public class SOSParameterSubstitutor {

    private Map<String, String> keylist;
    private StringSubstitutor strSubstitutorEnv;
    private StringSubstitutor strSubstitutor;
    private boolean caseSensitive = false;
    private String openTag = "${";
    private String closeTag = "}";

    public void setOpenTag(String openTag) {
        this.openTag = openTag;
    }

    public void setCloseTag(String closeTag) {
        this.closeTag = closeTag;
    }

    public SOSParameterSubstitutor() {
        super();
    }

    public SOSParameterSubstitutor(boolean caseSensitive_) {
        super();
        this.caseSensitive = caseSensitive_;
    }

    public SOSParameterSubstitutor(boolean caseSensitive_, String openTag_, String closeTag_) {
        super();
        this.openTag = openTag_;
        this.closeTag = closeTag_;
        this.caseSensitive = caseSensitive_;
    }

    public SOSParameterSubstitutor(String openTag_, String closeTag_) {
        super();
        this.openTag = openTag_;
        this.closeTag = closeTag_;
    }

    public void addKey(String k, String v) {
        if (keylist == null) {
            keylist = new HashMap<String, String>();
        }
        if (caseSensitive) {
            keylist.put(k, v);
        } else {
            keylist.put(k.toLowerCase(), v);
        }
    }

    public String replaceEnvVars(String source) {
        if (strSubstitutorEnv == null) {
            strSubstitutorEnv = new StringSubstitutor(System.getenv());
        }
        strSubstitutorEnv.setVariablePrefix(openTag);
        strSubstitutorEnv.setVariableSuffix(closeTag);
        return strSubstitutorEnv.replace(source);
    }

    public String replaceSystemProperties(String source) {
        return StringSubstitutor.replaceSystemProperties(source);
    }

    public String replace(final String source) {
        if (strSubstitutor == null) {
            if (caseSensitive) {
                strSubstitutor = new StringSubstitutor(keylist);
            } else {
                strSubstitutor = new StringSubstitutor(new SOSCaseInsensitivStrLookup<String>(keylist));
            }
        }
        strSubstitutor.setVariablePrefix(openTag);
        strSubstitutor.setVariableSuffix(closeTag);
        return strSubstitutor.replace(source);
    }

    public String replace(Path path) throws IOException {
        return replace(new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
    }

    public void replaceInFile(File in, File out) throws IOException {
        Path inPath = Paths.get(in.getAbsolutePath());
        Path outPath = Paths.get(out.getAbsolutePath());
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(inPath), charset);
        content = replace(content);
        Files.write(outPath, content.getBytes(charset));
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public List<String> getParameterNameFromString(String s) {
        List<String> l = new ArrayList<String>();
        String ot = Pattern.quote(openTag);
        String ct = Pattern.quote(closeTag);
        String regEx = ot + "([^" + ct + "]+)" + ct;
        Matcher m = Pattern.compile(regEx).matcher(s);
        while (m.find()) {
            l.add(m.group(1));
        }
        return l;
    }

    private class SOSCaseInsensitivStrLookup<V> implements StringLookup {

        private final Map<String, V> map;

        SOSCaseInsensitivStrLookup(final Map<String, V> map) {
            this.map = map;
        }

        @Override
        public String lookup(final String key) {
            String lowercaseKey = key.toLowerCase();
            if (map == null) {
                return null;
            }
            final V obj = map.get(lowercaseKey);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
    }

}
