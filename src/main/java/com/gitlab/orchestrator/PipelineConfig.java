package com.gitlab.orchestrator;

import java.util.HashMap;
import java.util.Map;

/**
 * Class representing a configuration for a GitLab pipeline.
 */
public class PipelineConfig {
    private String appName;
    private String projectId;
    private String accessToken;
    private String branchName;
    private String variablesString;
    private Map<String, String> variables;

    /**
     * Constructor for PipelineConfig.
     *
     * @param appName          Name of the application
     * @param projectId        GitLab project ID
     * @param accessToken      GitLab access token
     * @param branchName       Branch name to trigger pipeline on
     * @param variablesString  String of variables in format "key1=value1,key2=value2"
     */
    public PipelineConfig(String appName, String projectId, String accessToken, String branchName, String variablesString) {
        this.appName = appName;
        this.projectId = projectId;
        this.accessToken = accessToken;
        this.branchName = branchName != null && !branchName.trim().isEmpty() ? branchName : "main";
        this.variablesString = variablesString;
        this.variables = parseVariables(variablesString);
    }

    /**
     * Parses a colon-separated string of key-value pairs into a map.
     *
     * @param variablesStr String in format "key1=value1:key2=value2:key3=value3"
     * @return Map of key-value pairs
     */
    private Map<String, String> parseVariables(String variablesStr) {
        Map<String, String> vars = new HashMap<>();
        
        if (variablesStr != null && !variablesStr.trim().isEmpty()) {
            // Check if format is using colon as delimiter (new format)
            if (variablesStr.contains(":")) {
                String[] pairs = variablesStr.split(":");
                for (String pair : pairs) {
                    if (pair.contains("=")) {
                        String[] keyValue = pair.split("=", 2);
                        if (keyValue.length == 2) {
                            vars.put(keyValue[0].trim(), keyValue[1].trim());
                        }
                    }
                }
            } 
            // For backward compatibility - support comma as delimiter (old format)
            else if (variablesStr.contains(",")) {
                String[] pairs = variablesStr.split(",");
                for (String pair : pairs) {
                    if (pair.contains("=")) {
                        String[] keyValue = pair.split("=", 2);
                        if (keyValue.length == 2) {
                            vars.put(keyValue[0].trim(), keyValue[1].trim());
                        }
                    }
                }
            }
            // Single key-value pair without delimiter
            else if (variablesStr.contains("=")) {
                String[] keyValue = variablesStr.split("=", 2);
                if (keyValue.length == 2) {
                    vars.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        
        return vars;
    }

    // Getters and setters
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getVariablesString() {
        return variablesString;
    }

    public void setVariablesString(String variablesString) {
        this.variablesString = variablesString;
        this.variables = parseVariables(variablesString);
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    @Override
    public String toString() {
        return "PipelineConfig{" +
                "appName='" + appName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", accessToken='" + (accessToken != null ? "[REDACTED]" : "null") + '\'' +
                ", branchName='" + branchName + '\'' +
                ", variablesString='" + variablesString + '\'' +
                ", variables=" + variables +
                '}';
    }
}
