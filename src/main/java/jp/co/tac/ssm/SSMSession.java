package jp.co.tac.ssm;

import jp.co.tac.config.ProfileConfig.AwsProfile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.StartSessionRequest;
import software.amazon.awssdk.services.ssm.model.StartSessionResponse;
import software.amazon.awssdk.services.ssm.model.TerminateSessionRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jp.co.tac.aws.AWSCredentialManager; // 添加导入

public class SSMSession {
    private static final Logger logger = LogManager.getLogger(SSMSession.class);
    private final SsmClient ssmClient;
    private final AwsProfile profile;

    public SSMSession(AwsProfile profile) {
        this.profile = profile;
        AwsCredentialsProvider credentials = AWSCredentialManager.getCredentials(profile);

        this.ssmClient = SsmClient.builder()
                .region(Region.of(profile.getRegion()))
                .credentialsProvider(credentials)
                .build();
    }

    public SessionInfo startSession(String instanceId) {
        try {
            StartSessionRequest request = StartSessionRequest.builder()
                    .target(instanceId)
                    .build();

            StartSessionResponse response = ssmClient.startSession(request);
            return new SessionInfo(
                    response.sessionId(),
                    response.streamUrl(),
                    response.tokenValue(),
                    instanceId);
        } catch (Exception e) {
            logger.error("Failed to start session", e);
            throw new RuntimeException(e);
        }
    }

    public void terminateSession(String sessionId) {
        try {
            TerminateSessionRequest request = TerminateSessionRequest.builder()
                    .sessionId(sessionId)
                    .build();
            ssmClient.terminateSession(request);
        } catch (Exception e) {
            logger.error("Failed to terminate session", e);
        }
    }

    public void close() {
        ssmClient.close();
    }

    // 添加获取profile名称的方法
    public String getProfileName() {
        return this.profile.getProfileName();
    }

    // 获取完整profile对象的方法
    public AwsProfile getProfile() {
        return this.profile;
    }

    public static class SessionInfo {
        public final String sessionId;
        public final String streamUrl;
        public final String tokenValue;
        public final String instanceId;

        public SessionInfo(String sessionId, String streamUrl, String tokenValue, String instanceId) {
            this.sessionId = sessionId;
            this.streamUrl = streamUrl;
            this.tokenValue = tokenValue;
            this.instanceId = instanceId;
        }
    }
}