package jp.co.tac.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {
    private static final Logger logger = LogManager.getLogger(ConfigManager.class);
    private static final String DEFAULT_CONFIG = "/default-config.json";

    public static List<AwsInstance> loadFromJson(String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(filePath), new TypeReference<List<AwsInstance>>() {
            });
        } catch (Exception e) {
            logger.error("Error loading JSON configuration", e);
            return new ArrayList<>();
        }
    }

    public static List<AwsInstance> loadFromCsv(String filePath) {
        List<AwsInstance> instances = new ArrayList<>();

        try (Reader reader = new FileReader(filePath);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreHeaderCase()
                        .withTrim())) {

            for (CSVRecord record : csvParser) {
                AwsInstance instance = new AwsInstance();
                instance.setInstanceId(record.get("instanceId"));
                instance.setName(record.get("name"));
                instance.setRegion(record.get("region"));
                instance.setEnvironment(record.get("environment"));
                instance.setInstanceType(record.get("instanceType"));
                instance.setPrivateIp(record.get("privateIp"));
                instance.setPublicIp(record.get("publicIp"));
                instance.setKeyName(record.get("keyName"));
                instance.setVpcId(record.get("vpcId"));
                instance.setSubnetId(record.get("subnetId"));
                instance.setOsType(record.get("osType"));
                instance.setDescription(record.get("description"));

                instances.add(instance);
            }
        } catch (Exception e) {
            logger.error("Error loading CSV configuration", e);
        }

        return instances;
    }

    public static List<AwsInstance> loadConfiguration(String filePath) {
        try {
            // 添加文件存在性检查
            if (!Files.exists(Paths.get(filePath))) {
                System.err.println("Config file not found: " + filePath);
                return loadDefaultConfiguration();
            }
            if (filePath.toLowerCase().endsWith(".json")) {
                return loadFromJson(filePath);
            } else if (filePath.toLowerCase().endsWith(".csv")) {
                return loadFromCsv(filePath);
            } else {
                logger.warn("Unsupported file format: {}", filePath);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Error loading config: " + filePath);
            return loadDefaultConfiguration();
        }
    }

    public static List<AwsInstance> loadDefaultConfiguration() {
        try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream("default-config.json")) {
            if (is == null) {
                throw new IllegalStateException("Default config not found in classpath!");
            }
            return new ObjectMapper().readValue(is, new TypeReference<List<AwsInstance>>() {
            });
        } catch (IOException e) {
            System.err.println("Failed to load default config: " + e.getMessage());
            return Collections.emptyList();
        }
    }

}