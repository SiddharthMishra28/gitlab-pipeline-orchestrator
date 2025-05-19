package com.gitlab.orchestrator;

import org.gitlab4j.api.models.PipelineStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Class representing the result of a pipeline execution.
 */
public class PipelineResult {
    private String appName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private PipelineStatus status;
    private Map<String, String> injectedVariables;
    private long pipelineId;

    /**
     * Constructor for PipelineResult.
     *
     * @param appName          Name of the application
     * @param startTime        Start time of the pipeline
     * @param endTime          End time of the pipeline
     * @param status           Final status of the pipeline
     * @param injectedVariables Variables injected into the pipeline
     * @param pipelineId       GitLab pipeline ID
     */
    public PipelineResult(String appName, LocalDateTime startTime, LocalDateTime endTime, 
                          PipelineStatus status, Map<String, String> injectedVariables, long pipelineId) {
        this.appName = appName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.injectedVariables = injectedVariables;
        this.pipelineId = pipelineId;
    }

    /**
     * Get the build time of the pipeline.
     *
     * @return Duration of the pipeline execution
     */
    public Duration getBuildTime() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime);
        }
        return Duration.ZERO;
    }

    // Getters and setters
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public PipelineStatus getStatus() {
        return status;
    }

    public void setStatus(PipelineStatus status) {
        this.status = status;
    }

    public Map<String, String> getInjectedVariables() {
        return injectedVariables;
    }

    public void setInjectedVariables(Map<String, String> injectedVariables) {
        this.injectedVariables = injectedVariables;
    }

    public long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(long pipelineId) {
        this.pipelineId = pipelineId;
    }
}
