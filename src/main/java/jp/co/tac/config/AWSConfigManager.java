package jp.co.tac.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class AWSConfigManager {
    private static final String CONFIG_FILE = "aws-profiles.json";
    private ProfileConfig config;
    private ConfigWatcherService watcher;
    private final Preferences prefs = Preferences.userNodeForPackage(AWSConfigManager.class);

    public AWSConfigManager() {
        loadConfig();
        startWatcher();
    }

    public synchronized void loadConfig() {
        // 初始化默认配置
        this.config = createDefaultConfig();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Path configPath = getConfigPath();

            // 尝试加载外部配置
            if (Files.exists(configPath)) {
                try (InputStream is = Files.newInputStream(configPath)) {
                    this.config = mapper.readValue(is, ProfileConfig.class);
                    return;
                }
            }

            // 尝试加载内置配置
            try (InputStream is = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
                if (is != null) {
                    this.config = mapper.readValue(is, ProfileConfig.class);
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            // 加载失败时保留默认配置
        }
    }

    private ProfileConfig createDefaultConfig() {
        ProfileConfig defaultConfig = new ProfileConfig();
        defaultConfig.setProfiles(new ArrayList<>());
        defaultConfig.setRefreshInterval(5000);
        return defaultConfig;
    }

    private Path getConfigPath() {
        return Paths.get(System.getProperty("user.home"), ".aws", CONFIG_FILE);
    }

    private void startWatcher() {
        try {
            Path configPath = getConfigPath();
            if (!Files.exists(configPath))
                return;

            this.watcher = new ConfigWatcherService(
                    configPath.toString(),
                    config.getRefreshInterval(),
                    this::handleConfigChange);
            watcher.start();
        } catch (IOException e) {
            System.err.println("Failed to start config watcher: " + e.getMessage());
        }
    }

    private void handleConfigChange(Path changedFile) {
        System.out.println("Config changed: " + changedFile);
        loadConfig();
    }

    public List<ProfileConfig.AwsProfile> getProfiles() {
        return config.getProfiles();
    }

    public ProfileConfig.AwsProfile getDefaultProfile() {
        return config.getDefaultProfile();
    }

    public void shutdown() {
        if (watcher != null) {
            watcher.stop();
        }
    }
}