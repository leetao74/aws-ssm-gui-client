package jp.co.tac.config;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ConfigWatcherService {
    private final Path configPath;
    private final Consumer<Path> changeHandler;
    private final long interval;
    private ScheduledExecutorService executor;
    private long lastModified;

    public ConfigWatcherService(String configPath, long interval, Consumer<Path> changeHandler) {
        this.configPath = Paths.get(configPath);
        this.changeHandler = changeHandler;
        this.interval = interval;
        this.lastModified = 0;
    }

    public void start() throws IOException {
        if (!Files.exists(configPath)) {
            throw new IOException("Config file not found: " + configPath);
        }

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::checkForChanges, 0, interval, TimeUnit.MILLISECONDS);
    }

    private void checkForChanges() {
        try {
            long currentModified = Files.getLastModifiedTime(configPath).toMillis();
            if (currentModified > lastModified) {
                lastModified = currentModified;
                changeHandler.accept(configPath);
            }
        } catch (IOException e) {
            System.err.println("Error checking config: " + e.getMessage());
        }
    }

    public void stop() {
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}