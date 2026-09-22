package com.sos.auth.records;

import java.util.Optional;
import java.util.Set;

import com.sos.joc.model.common.Folder;

public record AuthFolders (Optional<Set<Folder>> allow, Optional<Set<Folder>> deny) {}
