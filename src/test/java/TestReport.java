import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TestReport {
    private static TestReport instance;
    private List<TestResult> results = new ArrayList<>();
    private TestResult currentTest;
    private static final String REPORT_DIR = "test-reports";
    private static final String SCREENSHOT_DIR = "screenshots";
    
    private TestReport() {
        createDirectories();
    }
    
    public static TestReport getInstance() {
        if (instance == null) {
            instance = new TestReport();
        }
        return instance;
    }
    
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(REPORT_DIR));
            Files.createDirectories(Paths.get(REPORT_DIR, SCREENSHOT_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void startTest(String testName) {
        currentTest = new TestResult();
        currentTest.testName = testName;
        currentTest.startTime = System.currentTimeMillis();
        currentTest.timestamp = LocalDateTime.now();
    }
    
    public void endTest(boolean passed) {
        if (currentTest != null) {
            currentTest.endTime = System.currentTimeMillis();
            currentTest.duration = currentTest.endTime - currentTest.startTime;
            currentTest.passed = passed;
            results.add(currentTest);
            currentTest = null;
        }
    }
    
    public void addScreenshot(String screenshotPath) {
        if (currentTest != null) {
            currentTest.screenshotPath = screenshotPath;
        }
    }
    
    public void generateReport() {
        String html = generateHtmlReport();
        String fileName = REPORT_DIR + "/report_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(html);
            System.out.println("HTML отчет сгенерирован: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String generateHtmlReport() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<title>Test Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append("h1 { color: #333; }");
        html.append(".summary { margin: 20px 0; padding: 10px; background: #f5f5f5; }");
        html.append(".passed { color: green; }");
        html.append(".failed { color: red; }");
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("th { background-color: #4CAF50; color: white; }");
        html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
        html.append(".screenshot-link { color: blue; text-decoration: underline; }");
        html.append("</style>");
        html.append("</head><body>");
        
        // Заголовок
        html.append("<h1>Test Execution Report</h1>");
        
        // Сводка
        long totalTests = results.size();
        long passedTests = results.stream().filter(r -> r.passed).count();
        long failedTests = totalTests - passedTests;
        long totalDuration = results.stream().mapToLong(r -> r.duration).sum();
        
        html.append("<div class='summary'>");
        html.append("<h2>Summary</h2>");
        html.append("<p>Total Tests: ").append(totalTests).append("</p>");
        html.append("<p class='passed'>Passed: ").append(passedTests).append("</p>");
        html.append("<p class='failed'>Failed: ").append(failedTests).append("</p>");
        html.append("<p>Total Duration: ").append(totalDuration).append(" ms</p>");
        html.append("</div>");
        
        // Таблица с деталями
        html.append("<h2>Test Details</h2>");
        html.append("<table>");
        html.append("<tr><th>Test Name</th><th>Timestamp</th><th>Duration (ms)</th><th>Status</th><th>Screenshot</th></tr>");
        
        for (TestResult result : results) {
            html.append("<tr>");
            html.append("<td>").append(result.testName).append("</td>");
            html.append("<td>").append(result.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td>");
            html.append("<td>").append(result.duration).append("</td>");
            html.append("<td class='").append(result.passed ? "passed" : "failed").append("'>")
                .append(result.passed ? "PASSED" : "FAILED").append("</td>");
            
            if (result.screenshotPath != null && !result.passed) {
                html.append("<td><a class='screenshot-link' href='").append(result.screenshotPath)
                    .append("' target='_blank'>View Screenshot</a></td>");
            } else {
                html.append("<td>-</td>");
            }
            html.append("</tr>");
        }
        
        html.append("</table>");
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private static class TestResult {
        String testName;
        LocalDateTime timestamp;
        long startTime;
        long endTime;
        long duration;
        boolean passed;
        String screenshotPath;
    }
}