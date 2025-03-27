package jp.co.tac.ssm;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SSMConnector {
    private static final Logger logger = LogManager.getLogger(SSMConnector.class);

    private final SsmClient ssmClient;
    private final Region region;
    private final Map<String, String> parameterCache = new HashMap<>();

    public SSMConnector(Region region) {
        this.region = region;
        this.ssmClient = SsmClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public String getParameter(String parameterName) {
        // 检查缓存
        if (parameterCache.containsKey(parameterName)) {
            logger.debug("Returning cached value for parameter: {}", parameterName);
            return parameterCache.get(parameterName);
        }

        try {
            logger.info("Fetching parameter: {}", parameterName);

            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            String value = response.parameter().value();

            // 存入缓存
            parameterCache.put(parameterName, value);

            logger.debug("Parameter {} value: {}", parameterName, value);
            return value;
        } catch (Exception e) {
            logger.error("Error fetching parameter: {}", parameterName, e);
            throw new RuntimeException("Failed to get parameter: " + parameterName, e);
        }
    }

    public Region getRegion() {
        return region;
    }

    public void close() {
        try {
            ssmClient.close();
            logger.info("SSM client closed successfully");
        } catch (Exception e) {
            logger.warn("Error closing SSM client", e);
        }
    }
}