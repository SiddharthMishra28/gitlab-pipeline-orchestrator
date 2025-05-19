package com.gitlab.orchestrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for generating reports about pipeline executions.
 */
public class PipelineReporter {
    private static final Logger logger = LoggerFactory.getLogger(PipelineReporter.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generates a CLI report for all pipeline results.
     *
     * @param results List of pipeline results
     */
    public void generateCliReport(List<PipelineResult> results) {
        logger.info("Generating pipeline execution report...");
        
        StringBuilder report = new StringBuilder()
                .append("\n\n=======================================================\n")
                .append("                Pipeline Summary Report                 \n")
                .append("=======================================================\n\n");
        
        for (PipelineResult result : results) {
            report.append("App Name: ").append(result.getAppName()).append("\n");
            report.append("- Pipeline ID: ").append(result.getPipelineId()).append("\n");
            report.append("- Start Time: ").append(formatDateTime(result.getStartTime())).append("\n");
            report.append("- End Time: ").append(formatDateTime(result.getEndTime())).append("\n");
            report.append("- Status: ").append(result.getStatus()).append("\n");
            report.append("- Injected Variables: ").append(formatVariables(result.getInjectedVariables())).append("\n");
            report.append("- Build Time: ").append(formatDuration(result.getBuildTime())).append("\n");
            report.append("\n");
        }
        
        report.append("=======================================================\n");
        
        System.out.println(report.toString());
        logger.info("Report generation completed");
    }

    /**
     * Format the duration in a human-readable format.
     *
     * @param duration Duration to format
     * @return Formatted duration string
     */
    private String formatDuration(Duration duration) {
        long totalSeconds = duration.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (hours > 0) {
            return String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }

    /**
     * Format variables into a readable string.
     *
     * @param variables Map of variables
     * @return Formatted variable string
     */
    private String formatVariables(Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) {
            return "None";
        }
        
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        
        return sb.toString();
    }

    /**
     * Format date time for the report.
     *
     * @param dateTime The LocalDateTime to format
     * @return Formatted date time string
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime != null) {
            return dateTime.format(TIME_FORMATTER);
        } else {
            return "N/A";
        }
    }
}
