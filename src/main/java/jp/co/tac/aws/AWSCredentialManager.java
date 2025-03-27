package jp.co.tac.aws;

import jp.co.tac.config.ProfileConfig.AwsProfile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AWSCredentialManager {
    private static final Logger logger = LogManager.getLogger(AWSCredentialManager.class);

    public static boolean isSessionValid(AwsProfile profile) {
        try {
            getCredentials(profile).resolveCredentials();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean loginWithSSO(AwsProfile profile) {
        if (!profile.isSsoProfile()) {
            logger.info("Skipping non-SSO profile: {}", profile.getName());
            return true;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "aws", "sso", "login",
                    "--profile", profile.getName(),
                    "--region", profile.getRegion());

            Process process = pb.start();
            logProcessOutput(process);

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("SSO login successful for profile: {}", profile.getName());
                return true;
            }
            logger.error("SSO login failed for profile: {}", profile.getName());
            return false;
        } catch (IOException | InterruptedException e) {
            logger.error("SSO login error", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static void logProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("AWS CLI: {}", line);
            }
        }
    }

    public static AwsCredentialsProvider getCredentials(AwsProfile profile) {
        return profile.isSsoProfile() ? ProfileCredentialsProvider.builder()
                .profileName(profile.getName())
                .build() : DefaultCredentialsProvider.create();
    }

}