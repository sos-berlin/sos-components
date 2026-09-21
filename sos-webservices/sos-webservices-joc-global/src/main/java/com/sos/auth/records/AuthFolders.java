package com.sos.auth.records;

import java.util.Optional;
import java.util.Set;

import com.sos.auth.common.AuthFolder;

public record AuthFolders (Optional<Set<AuthFolder>> allow, Optional<Set<AuthFolder>> deny) {}
