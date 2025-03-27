package jp.co.tac.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ProfileConfig {
    private List<AwsProfile> profiles;
    private long refreshInterval = 5000; // 默认5秒刷新间隔

    public ProfileConfig() {
    };

    public static class AwsProfile {
        private String name;
        private String type = "credentials"; // 默认类型
        private String description;

        // SSO specific fields
        private String sso_session;
        private String sso_account_id;
        private String sso_role_name;

        // Common fields
        private String region;
        private String output;

        @JsonProperty("isDefault")
        private boolean isDefault;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSsoSession() {
            return sso_session;
        }

        public void setSsoSession(String sso_session) {
            this.sso_session = sso_session;
        }

        public String getSsoAccountId() {
            return sso_account_id;
        }

        public void setSsoAccountId(String sso_account_id) {
            this.sso_account_id = sso_account_id;
        }

        public String getSsoRoleName() {
            return sso_role_name;
        }

        public void setSsoRoleName(String sso_role_name) {
            this.sso_role_name = sso_role_name;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }

        public boolean isSsoProfile() {
            return "sso".equalsIgnoreCase(type);
        }

        public String getProfileName() {
            return name;
        }

    }

    // Getters and Setters
    public List<AwsProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<AwsProfile> profiles) {
        this.profiles = profiles;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public AwsProfile getDefaultProfile() {
        return profiles.stream()
                .filter(AwsProfile::isDefault)
                .findFirst()
                .orElse(profiles.isEmpty() ? null : profiles.get(0));
    }
}