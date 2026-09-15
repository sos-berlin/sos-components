package com.sos.auth.records;

import java.util.Map;
import java.util.Set;

import com.sos.auth.common.AuthFolder;

public record PermissionsPerRole (UniqueRole role, Map<String, Set<AuthFolder>> folders, Map<String, Set<String>> permissions) {}
