package com.sos.joc.classes.profiles;

import java.util.ArrayList;
import java.util.List;

import com.sos.joc.model.configuration.ConfigurationType;

public class ProfilesDeleteResult {

    private Configuration configuration = new Configuration();
    private List<Account> accounts = new ArrayList<>();

    protected Configuration getConfiguration() {
        return configuration;
    }

    protected Account getAccount(final String account) {
        Account a = accounts.stream().filter(e -> e.account.equals(account)).findAny().orElse(null);
        if (a == null) {
            a = new Account(account);
            accounts.add(a);
        }
        return a;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Configuration=[").append(configuration.toString()).append("]");
        sb.append(",Accounts=[");
        for (Account a : accounts) {
            sb.append("[").append(a.toString()).append("]");
        }
        sb.append("]");
        return sb.toString();
    }

    protected class Configuration {

        private int profile;
        private int git;
        private int setting;
        private int customization;
        private int ignoreList;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(ConfigurationType.PROFILE.name()).append("=").append(profile);
            sb.append(",").append(ConfigurationType.GIT.name()).append("=").append(git);
            sb.append(",").append(ConfigurationType.SETTING.name()).append("=").append(setting);
            sb.append(",").append(ConfigurationType.CUSTOMIZATION.name()).append("=").append(customization);
            sb.append(",").append(ConfigurationType.IGNORELIST.name()).append("=").append(ignoreList);
            return sb.toString();
        }

        protected void setProfile(int val) {
            profile = val;
        }

        protected void setGit(int val) {
            git = val;
        }

        protected void setSetting(int val) {
            setting = val;
        }

        protected void setCustomization(int val) {
            customization = val;
        }

        protected void setIgnoreList(int val) {
            ignoreList = val;
        }
    }

    protected class Account {

        private final String account;

        private int favorite;
        private int key;
        private int cert;

        private Account(String account) {
            this.account = account;
        }

        protected void setFavorite(int val) {
            favorite = val;
        }

        protected void setKey(int val) {
            key = val;
        }

        protected void setCert(int val) {
            cert = val;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("account=").append(account);
            sb.append(",favorite=").append(favorite);
            sb.append(",key=").append(key);
            sb.append(",cert=").append(cert);
            return sb.toString();
        }
    }
}
