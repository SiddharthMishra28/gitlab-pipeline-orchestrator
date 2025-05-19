package com.gitlab.orchestrator;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.PipelineStatus;
import org.gitlab4j.api.models.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main class for the GitLab Pipeline Executor application.
 * This application reads pipeline configurations from a CSV file,
 * triggers GitLab pipelines sequentially, and generates a report.
 */
public class GitLabPipelineExecutor {
    private static final Logger logger = LoggerFactory.getLogger(GitLabPipelineExecutor.class);
    private static final String GITLAB_URL = "https://gitlab.com";
    private static final String DEFAULT_CSV_PATH = "pipelines.csv";
    private static final long POLLING_INTERVAL_MS = 10000; // 10 seconds

    /**
     * Main method that executes the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        logger.info("Starting GitLab Pipeline Executor");
        
        // Determine CSV file path from arguments or use default
        String csvFilePath = DEFAULT_CSV_PATH;
        if (args.length > 0) {
            csvFilePath = args[0];
        }
        
        try {
            // Parse CSV file
            CsvParser csvParser = new CsvParser();
            List<PipelineConfig> pipelineConfigs = csvParser.parse(csvFilePath);
            
            if (pipelineConfigs.isEmpty()) {
                logger.warn("No valid pipeline configurations found in the CSV file");
                return;
            }
            
            // Execute pipelines sequentially and collect results
            List<PipelineResult> results = executePipelinesSequentially(pipelineConfigs);
            
            // Generate CLI report
            PipelineReporter reporter = new PipelineReporter();
            reporter.generateCliReport(results);
            
            // Generate HTML report
            HtmlReportGenerator htmlReporter = new HtmlReportGenerator();
            htmlReporter.generateHtmlReport(results);
            
        } catch (IOException e) {
            logger.error("Error reading CSV file: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
        }
        
        logger.info("GitLab Pipeline Executor completed");
    }

    /**
     * Executes the pipelines sequentially, waiting for each to complete before starting the next.
     *
     * @param pipelineConfigs List of pipeline configurations
     * @return List of pipeline results
     */
    private static List<PipelineResult> executePipelinesSequentially(List<PipelineConfig> pipelineConfigs) {
        List<PipelineResult> results = new ArrayList<>();
        
        for (PipelineConfig config : pipelineConfigs) {
            logger.info("Processing pipeline for app: {}", config.getAppName());
            
            try {
                PipelineResult result = executePipeline(config);
                results.add(result);
                
                // Only continue to the next pipeline if this one was successful
                if (result.getStatus() != PipelineStatus.SUCCESS) {
                    logger.warn("Pipeline for app '{}' did not succeed (status: {}). Stopping sequential execution.", 
                            config.getAppName(), result.getStatus());
                    break;
                }
            } catch (Exception e) {
                logger.error("Error executing pipeline for app '{}': {}", config.getAppName(), e.getMessage(), e);
                break;
            }
        }
        
        return results;
    }

    /**
     * Executes a single pipeline according to the provided configuration.
     *
     * @param config Pipeline configuration
     * @return Result of the pipeline execution
     * @throws GitLabApiException If there is an error with the GitLab API
     */
    private static PipelineResult executePipeline(PipelineConfig config) throws GitLabApiException {
        logger.info("Initializing GitLab API client for project ID: {}", config.getProjectId());
        
        // Create GitLab API client
        GitLabApi gitLabApi = new GitLabApi(GITLAB_URL, config.getAccessToken());
        
        // Use project ID as a string since GitLabApi expects String, Long, or Project instance
        String projectId = config.getProjectId();
        
        // Prepare variables
        List<Variable> pipelineVariables = convertVariables(config.getVariables());
        
        // Record start time
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("Triggering pipeline for app '{}' on branch '{}'", config.getAppName(), config.getBranchName());
        
        // Trigger pipeline
        Pipeline pipeline = gitLabApi.getPipelineApi().createPipeline(projectId, config.getBranchName(), pipelineVariables);
        logger.info("Pipeline triggered successfully. Pipeline ID: {}", pipeline.getId());
        
        // Poll until pipeline is complete
        PipelineStatus finalStatus = pollPipelineStatus(gitLabApi, projectId, pipeline.getId());
        
        // Record end time
        LocalDateTime endTime = LocalDateTime.now();
        
        // Create and return result
        PipelineResult result = new PipelineResult(
                config.getAppName(),
                startTime,
                endTime,
                finalStatus,
                config.getVariables(),
                pipeline.getId()
        );
        
        logger.info("Pipeline for app '{}' completed with status: {}", config.getAppName(), finalStatus);
        return result;
    }

    /**
     * Converts a map of variables to a list of GitLab Variables.
     *
     * @param variables Map of variable key-value pairs
     * @return List of GitLab Variables
     */
    private static List<Variable> convertVariables(Map<String, String> variables) {
        List<Variable> pipelineVariables = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            pipelineVariables.add(new Variable(entry.getKey(), entry.getValue()));
        }
        
        return pipelineVariables;
    }

    /**
     * Polls the pipeline status until it reaches a final state.
     *
     * @param gitLabApi GitLab API client
     * @param projectId Project ID
     * @param pipelineId Pipeline ID
     * @return Final status of the pipeline
     * @throws GitLabApiException If there is an error with the GitLab API
     */
    private static PipelineStatus pollPipelineStatus(GitLabApi gitLabApi, String projectId, Long pipelineId) 
            throws GitLabApiException {
        logger.info("Starting to poll pipeline status for pipeline ID: {}", pipelineId);
        
        PipelineStatus status;
        boolean isComplete = false;
        
        do {
            try {
                // Sleep before polling again
                Thread.sleep(POLLING_INTERVAL_MS);
                
                // Get pipeline status
                Pipeline pipeline = gitLabApi.getPipelineApi().getPipeline(projectId, pipelineId);
                status = pipeline.getStatus();
                
                logger.info("Current status of pipeline {}: {}", pipelineId, status);
                
                // Check if the pipeline has reached a terminal state
                isComplete = isTerminalStatus(status);
                
            } catch (InterruptedException e) {
                logger.error("Polling interrupted", e);
                Thread.currentThread().interrupt();
                throw new GitLabApiException("Polling was interrupted");
            }
        } while (!isComplete);
        
        logger.info("Pipeline {} has reached terminal status: {}", pipelineId, status);
        return status;
    }

    /**
     * Determines if a pipeline status is a terminal status.
     *
     * @param status Pipeline status
     * @return True if the status is terminal
     */
    private static boolean isTerminalStatus(PipelineStatus status) {
        return status == PipelineStatus.SUCCESS || 
               status == PipelineStatus.FAILED || 
               status == PipelineStatus.CANCELED || 
               status == PipelineStatus.SKIPPED;
    }
}
