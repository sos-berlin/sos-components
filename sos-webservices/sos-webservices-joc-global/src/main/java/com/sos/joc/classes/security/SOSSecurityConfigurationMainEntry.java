package com.sos.joc.classes.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sos.joc.model.security.SecurityConfigurationMainEntry;

public class SOSSecurityConfigurationMainEntry {


	public static String getIniWriteString(SecurityConfigurationMainEntry securityConfigurationMainEntry) {
		if (securityConfigurationMainEntry.getEntryName().contains(".groupRolesMap")) {
			String s = "  ";
			if (securityConfigurationMainEntry.getEntryValue().size() > 1) {
				s = "\\" + "\n" + "  ";
			}

			for (int i = 0; i < securityConfigurationMainEntry.getEntryValue().size(); i++) {
				s = s + securityConfigurationMainEntry.getEntryValue().get(i).trim();
				if (i < securityConfigurationMainEntry.getEntryValue().size() - 1) {
					s = s + ", \\" + "\n" + "  ";
				} else {
					s = s + "\n";
				}
			}
			return s;
		} else {
			String value = securityConfigurationMainEntry.getEntryValue().get(0).trim();
			if (value.isEmpty()) {
				value = ".";
			}
			return value;
		}
	}

//	public static String removeCharInQuotesTest(String s) {
//		return removeCharInQuotes(s, ',', '^');
//	}

	public static List<String> getMultiLineValue(String entryKey, String entryMultiLineValue) {
		List<String> entryValue = new ArrayList<>();
		if (entryKey.contains(".groupRolesMap")) {
			entryMultiLineValue = removeCharInQuotes(entryMultiLineValue, ',', '°');
			String s[] = entryMultiLineValue.split(",");
			for (int i = 0; i < s.length; i++) {
				entryValue.add(removeCharInQuotes(s[i], '°', ','));
			}
		} else {
			entryValue.add(entryMultiLineValue);
		}
		return entryValue;
	}

	public static List<String> getMultiLineComment(String main, Map<String, String> comments) {
		if (comments != null && comments.get(main) != null) {
		    return Arrays.asList(comments.get(main).trim().split("\\r?\\n"));
		}
		return Collections.emptyList();
	}
	
	private static String removeCharInQuotes(String s, char source, char target) {
        boolean inQuote = false;
        StringBuffer str = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ('"' == c) {
                inQuote = !inQuote;
            }
            if (inQuote && source == c) {
                str.setCharAt(i, target);
            }
        }
        return str.toString();

    }

}
