package com.gitlab.orchestrator;

import org.gitlab4j.api.models.PipelineStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Class responsible for generating HTML reports about pipeline executions.
 */
public class HtmlReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HtmlReportGenerator.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String REPORT_FILE = "flow-forge-report.html";

    /**
     * Generates an HTML report for all pipeline results.
     *
     * @param results List of pipeline results
     */
    public void generateHtmlReport(List<PipelineResult> results) {
        logger.info("Generating HTML pipeline execution report...");
        
        try (FileWriter writer = new FileWriter(REPORT_FILE)) {
            StringBuilder html = new StringBuilder();
            
            // Start HTML document
            html.append("<!DOCTYPE html>\n")
                .append("<html lang=\"en\">\n")
                .append("<head>\n")
                .append("    <meta charset=\"UTF-8\">\n")
                .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("    <title>Flow Forge Execution Report</title>\n")
                .append("    <style>\n")
                .append("        body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }\n")
                .append("        h1 { color: #2e86de; text-align: center; margin-bottom: 30px; }\n")
                .append("        .report-container { max-width: 900px; margin: 0 auto; }\n")
                .append("        .pipeline-card { background-color: #f5f6fa; border-radius: 8px; padding: 20px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }\n")
                .append("        .pipeline-header { display: flex; justify-content: space-between; margin-bottom: 15px; }\n")
                .append("        .pipeline-title { font-size: 1.4em; font-weight: bold; color: #2d3436; margin: 0; }\n")
                .append("        .pipeline-id { color: #636e72; font-size: 1em; }\n")
                .append("        .pipeline-detail { display: flex; margin-bottom: 8px; }\n")
                .append("        .detail-label { font-weight: bold; min-width: 140px; color: #636e72; }\n")
                .append("        .detail-value { flex-grow: 1; }\n")
                .append("        .status-success { color: #27ae60; font-weight: bold; }\n")
                .append("        .status-failed { color: #e74c3c; font-weight: bold; }\n")
                .append("        .status-pending { color: #f39c12; font-weight: bold; }\n")
                .append("        .status-other { color: #7f8c8d; font-weight: bold; }\n")
                .append("        .variables-container { background-color: #ecf0f1; border-radius: 4px; padding: 10px; margin-top: 10px; }\n")
                .append("        .variable-item { margin-bottom: 5px; }\n")
                .append("        .build-time { font-weight: bold; margin-top: 15px; text-align: right; color: #2c3e50; }\n")
                .append("        .timestamp { color: #7f8c8d; font-size: 0.9em; }\n")
                .append("        .chart-container { margin-top: 40px; text-align: center; }\n")
                .append("    </style>\n")
                .append("</head>\n")
                .append("<body>\n")
                .append("    <div class=\"report-container\">\n")
                .append("        <h1>Flow Forge Execution Report</h1>\n");
            
            // Add each pipeline result
            for (PipelineResult result : results) {
                html.append(generatePipelineCard(result));
            }
            
            // Add summary chart
            html.append(generateSummaryChart(results));
            
            // Close HTML document
            html.append("    </div>\n")
                .append("</body>\n")
                .append("</html>");
            
            writer.write(html.toString());
            logger.info("HTML report generated successfully: {}", REPORT_FILE);
            
        } catch (IOException e) {
            logger.error("Error generating HTML report: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generate HTML for a single pipeline card
     * 
     * @param result Pipeline result
     * @return HTML string for pipeline card
     */
    private String generatePipelineCard(PipelineResult result) {
        StringBuilder card = new StringBuilder();
        
        card.append("        <div class=\"pipeline-card\">\n")
            .append("            <div class=\"pipeline-header\">\n")
            .append("                <h2 class=\"pipeline-title\">").append(result.getAppName()).append("</h2>\n")
            .append("                <span class=\"pipeline-id\">Pipeline ID: ").append(result.getPipelineId()).append("</span>\n")
            .append("            </div>\n");
        
        // Status with colored styling
        card.append("            <div class=\"pipeline-detail\">\n")
            .append("                <div class=\"detail-label\">Status:</div>\n")
            .append("                <div class=\"detail-value ").append(getStatusClass(result.getStatus())).append("\">\n")
            .append("                    ").append(result.getStatus()).append("\n")
            .append("                </div>\n")
            .append("            </div>\n");
        
        // Project details
        card.append("            <div class=\"pipeline-detail\">\n")
            .append("                <div class=\"detail-label\">Start Time:</div>\n")
            .append("                <div class=\"detail-value timestamp\">").append(formatDateTime(result.getStartTime())).append("</div>\n")
            .append("            </div>\n");
        
        card.append("            <div class=\"pipeline-detail\">\n")
            .append("                <div class=\"detail-label\">End Time:</div>\n")
            .append("                <div class=\"detail-value timestamp\">").append(formatDateTime(result.getEndTime())).append("</div>\n")
            .append("            </div>\n");
        
        // Variables
        card.append("            <div class=\"pipeline-detail\">\n")
            .append("                <div class=\"detail-label\">Injected Variables:</div>\n")
            .append("                <div class=\"detail-value\">\n")
            .append("                    <div class=\"variables-container\">\n");
        
        for (Map.Entry<String, String> variable : result.getInjectedVariables().entrySet()) {
            card.append("                        <div class=\"variable-item\">")
                .append(variable.getKey()).append(" = ").append(variable.getValue())
                .append("</div>\n");
        }
        
        card.append("                    </div>\n")
            .append("                </div>\n")
            .append("            </div>\n");
        
        // Build time
        card.append("            <div class=\"build-time\">Build Time: ").append(formatDuration(result.getBuildTime())).append("</div>\n")
            .append("        </div>\n");
        
        return card.toString();
    }
    
    /**
     * Generate a summary chart HTML for all pipelines
     * 
     * @param results List of pipeline results
     * @return HTML for the summary chart
     */
    private String generateSummaryChart(List<PipelineResult> results) {
        StringBuilder chart = new StringBuilder();
        
        chart.append("        <div class=\"chart-container\">\n")
            .append("            <h2>Pipeline Status Summary</h2>\n");
        
        // Add Chart.js
        chart.append("            <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n")
            .append("            <canvas id=\"pipelineChart\" width=\"400\" height=\"200\"></canvas>\n")
            .append("            <h4>Developed By : <a href=\"mailto:connectwithsiddharthm@gmail.com\">Siddharth Mishra</a></h4>\n")
            .append("            <script>\n")
            .append("                const ctx = document.getElementById('pipelineChart').getContext('2d');\n")
            .append("                const pipelineLabels = [");
        
        // Pipeline names for labels
        boolean first = true;
        for (PipelineResult result : results) {
            if (!first) {
                chart.append(", ");
            }
            chart.append("'").append(result.getAppName()).append("'");
            first = false;
        }
        
        chart.append("];\n")
            .append("                const buildTimes = [");
        
        // Build times for data
        first = true;
        for (PipelineResult result : results) {
            if (!first) {
                chart.append(", ");
            }
            chart.append(result.getBuildTime().getSeconds());
            first = false;
        }
        
        chart.append("];\n")
            .append("                const statusColors = [");
        
        // Colors based on status
        first = true;
        for (PipelineResult result : results) {
            if (!first) {
                chart.append(", ");
            }
            
            if (result.getStatus() == PipelineStatus.SUCCESS) {
                chart.append("'#27ae60'"); // Green for success
            } else if (result.getStatus() == PipelineStatus.FAILED) {
                chart.append("'#e74c3c'"); // Red for failed
            } else if (result.getStatus() == PipelineStatus.PENDING) {
                chart.append("'#f39c12'"); // Orange for pending
            } else {
                chart.append("'#7f8c8d'"); // Gray for other statuses
            }
            
            first = false;
        }
        
        chart.append("];\n")
            .append("                const pipelineChart = new Chart(ctx, {\n")
            .append("                    type: 'bar',\n")
            .append("                    data: {\n")
            .append("                        labels: pipelineLabels,\n")
            .append("                        datasets: [{\n")
            .append("                            label: 'Build Time (seconds)',\n")
            .append("                            data: buildTimes,\n")
            .append("                            backgroundColor: statusColors,\n")
            .append("                            borderColor: statusColors,\n")
            .append("                            borderWidth: 1\n")
            .append("                        }]\n")
            .append("                    },\n")
            .append("                    options: {\n")
            .append("                        responsive: true,\n")
            .append("                        scales: {\n")
            .append("                            y: {\n")
            .append("                                beginAtZero: true,\n")
            .append("                                title: {\n")
            .append("                                    display: true,\n")
            .append("                                    text: 'Build Time (seconds)'\n")
            .append("                                }\n")
            .append("                            },\n")
            .append("                            x: {\n")
            .append("                                title: {\n")
            .append("                                    display: true,\n")
            .append("                                    text: 'Pipelines'\n")
            .append("                                }\n")
            .append("                            }\n")
            .append("                        },\n")
            .append("                        plugins: {\n")
            .append("                            title: {\n")
            .append("                                display: true,\n")
            .append("                                text: 'Pipeline Build Times'\n")
            .append("                            },\n")
            .append("                            tooltip: {\n")
            .append("                                callbacks: {\n")
            .append("                                    afterLabel: function(context) {\n")
            .append("                                        return 'Status: ' + ['SUCCESS', 'FAILED', 'PENDING', 'OTHER'][Math.floor(Math.random() * 4)];\n")
            .append("                                    }\n")
            .append("                                }\n")
            .append("                            }\n")
            .append("                        }\n")
            .append("                    }\n")
            .append("                });\n")
            .append("            </script>\n")
            .append("        </div>\n");
        
        return chart.toString();
    }
    
    /**
     * Get the CSS class for a status
     * 
     * @param status Pipeline status
     * @return CSS class name
     */
    private String getStatusClass(PipelineStatus status) {
        if (status == null) {
            return "status-other";
        }
        
        switch (status) {
            case SUCCESS:
                return "status-success";
            case FAILED:
                return "status-failed";
            case PENDING:
                return "status-pending";
            default:
                return "status-other";
        }
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
}