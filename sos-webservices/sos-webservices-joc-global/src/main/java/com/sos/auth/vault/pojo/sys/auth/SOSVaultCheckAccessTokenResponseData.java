package com.sos.auth.vault.pojo.sys.auth;

import java.util.List;

public class SOSVaultCheckAccessTokenResponseData{
 public String accessor;
 public int creation_time;
 public int creation_ttl;
 public String display_name;
 public String entity_id;
 public String expire_time;
 public int explicit_max_ttl;
 public String id;
 public String issue_time;
 public SOSVaultCheckAccessTokenMeta meta;
 public int num_uses;
 public boolean orphan;
 public String path;
 public List<String> policies;
 public boolean renewable;
 public int ttl;
 public String type;
}
