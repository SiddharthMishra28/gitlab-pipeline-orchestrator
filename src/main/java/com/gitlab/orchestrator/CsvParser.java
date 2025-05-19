package com.gitlab.orchestrator;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for parsing the CSV file containing pipeline configurations.
 */
public class CsvParser {
    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);
    private static final int EXPECTED_COLUMN_COUNT = 5;
    private static final int APP_NAME_INDEX = 0;
    private static final int PROJECT_ID_INDEX = 1;
    private static final int ACCESS_TOKEN_INDEX = 2;
    private static final int BRANCH_NAME_INDEX = 3;
    private static final int VARIABLES_INDEX = 4;

    /**
     * Reads the CSV file and parses it into PipelineConfig objects.
     *
     * @param filePath Path to the CSV file
     * @return List of PipelineConfig objects
     * @throws IOException If there is an error reading the file
     */
    public List<PipelineConfig> parse(String filePath) throws IOException {
        logger.info("Starting to parse CSV file: {}", filePath);
        List<PipelineConfig> configs = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] header = reader.readNext(); // Skip header row
            
            if (header == null || header.length < EXPECTED_COLUMN_COUNT) {
                throw new IOException("CSV file header is missing or has incorrect format");
            }
            
            String[] line;
            int lineNumber = 1; // Start from 1 because we already read the header
            
            while ((line = reader.readNext()) != null) {
                lineNumber++;
                
                if (line.length < EXPECTED_COLUMN_COUNT) {
                    logger.warn("Line {} has fewer columns than expected ({}), skipping", lineNumber, EXPECTED_COLUMN_COUNT);
                    continue;
                }
                
                try {
                    PipelineConfig config = new PipelineConfig(
                            line[APP_NAME_INDEX],
                            line[PROJECT_ID_INDEX],
                            line[ACCESS_TOKEN_INDEX],
                            line[BRANCH_NAME_INDEX],
                            line.length > VARIABLES_INDEX ? line[VARIABLES_INDEX] : ""
                    );
                    
                    validateConfig(config, lineNumber);
                    configs.add(config);
                    logger.debug("Added pipeline config for app: {}", config.getAppName());
                } catch (Exception e) {
                    logger.error("Error parsing line {}: {}", lineNumber, e.getMessage());
                }
            }
        } catch (CsvValidationException e) {
            logger.error("Error validating CSV file", e);
            throw new IOException("Error validating CSV file: " + e.getMessage(), e);
        }
        
        logger.info("CSV parsing completed. Found {} valid pipeline configurations", configs.size());
        return configs;
    }

    /**
     * Validates a pipeline configuration.
     *
     * @param config     PipelineConfig to validate
     * @param lineNumber Line number in the CSV file
     * @throws IllegalArgumentException If the configuration is invalid
     */
    private void validateConfig(PipelineConfig config, int lineNumber) throws IllegalArgumentException {
        if (config.getAppName() == null || config.getAppName().trim().isEmpty()) {
            throw new IllegalArgumentException("App name is required (line " + lineNumber + ")");
        }
        
        if (config.getProjectId() == null || config.getProjectId().trim().isEmpty()) {
            throw new IllegalArgumentException("Project ID is required (line " + lineNumber + ")");
        }
        
        if (config.getAccessToken() == null || config.getAccessToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Access token is required (line " + lineNumber + ")");
        }
        
        // Branch name can be empty, defaults to "main" in the PipelineConfig constructor
        
        try {
            Integer.parseInt(config.getProjectId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Project ID must be a number (line " + lineNumber + ")");
        }
    }
}
