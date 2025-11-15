package main.java.com.group3.payroll;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;

public class PayrollAnalysisApp extends JFrame {
    private Connection connection;
    private DataAnalyzer dataAnalyzer;
    private JTextArea resultArea;
    private JComboBox<EmployeeItem> employeeSelector;
    private ChartGenerator chartGenerator;
    
    // Colors for better UI
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    
    public PayrollAnalysisApp() {
        initializeUI();
        setupEventHandlers();
        initializeDatabase();
    }
    
    private void initializeUI() {
        setTitle("Employee Salary Analysis System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // This is the code that was misplaced:
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create control panel
        JPanel controlPanel = createControlPanel();

    JScrollPane scrollableControlPanel = new JScrollPane(controlPanel);
    scrollableControlPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    scrollableControlPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollableControlPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollableControlPanel.getVerticalScrollBar().setUnitIncrement(16);
    
    
    // Give the scroll pane a sensible preferred height so the left panel is
    // visible and can scroll when its content is taller than the viewport.
    scrollableControlPanel.setPreferredSize(new Dimension(300, 650));
    scrollableControlPanel.setMinimumSize(new Dimension(220, 200));
    
    mainPanel.add(scrollableControlPanel, BorderLayout.WEST);
        
        // Create result area
        JPanel resultPanel = createResultPanel();
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("Employee Salary Analysis System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel);
        return headerPanel;
    }
    
   private JPanel createControlPanel() {
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
    controlPanel.setBackground(Color.WHITE);
    controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    
    
    // Let the control panel's preferred height be determined by its content
    // so the scroll pane will show scroll bars when controls overflow vertically.
    
    // Section title - Analysis Tools
    JLabel sectionLabel = new JLabel("Analysis Tools");
    sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    sectionLabel.setForeground(PRIMARY_COLOR);
    sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    controlPanel.add(sectionLabel);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    
    // Task 1: Average salary per department
    JButton avgSalaryBtn = createStyledButton("Average Salary by Department");
    avgSalaryBtn.addActionListener(e -> showAverageSalaryByDepartment());
    controlPanel.add(avgSalaryBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 16)));
    
    // Task 2: Salary trend
    // Stack the label above the controls so the selector and button are
    // always visible and not pushed to the far right by layout quirks.
    JPanel trendPanel = new JPanel();
    trendPanel.setLayout(new BoxLayout(trendPanel, BoxLayout.Y_AXIS));
    trendPanel.setBackground(Color.WHITE);
    trendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel trendLabel = new JLabel("Employee Salary Trend:");
    trendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    trendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    trendPanel.add(trendLabel);
    trendPanel.add(Box.createRigidArea(new Dimension(0, 6)));

    JPanel trendControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    trendControls.setBackground(Color.WHITE);
    trendControls.setAlignmentX(Component.LEFT_ALIGNMENT);

    employeeSelector = new JComboBox<>();
    employeeSelector.setPrototypeDisplayValue(new EmployeeItem("XXXXXXXX","Firstname Lastname"));
    // Increase selector size for visibility
    employeeSelector.setPreferredSize(new Dimension(190, 28));
    employeeSelector.setMaximumSize(new Dimension(190, 28));
    employeeSelector.setMinimumSize(new Dimension(160, 24));

    JButton trendBtn = createStyledButton("Show");
    // Size the trend button to match selector height
    trendBtn.setPreferredSize(new Dimension(55, 28));
    trendBtn.setMaximumSize(new Dimension(55, 28));
    trendBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    trendBtn.addActionListener(e -> showSalaryTrend());

    // Put selector and button into the trendControls panel so sizes are respected
    trendControls.add(employeeSelector);
    trendControls.add(trendBtn);
    trendPanel.add(trendControls);
    
    controlPanel.add(trendPanel);
    // Add extra vertical space after the trend controls so they remain
    // clearly visible and the rest of the buttons are pushed further down.
    controlPanel.add(Box.createRigidArea(new Dimension(0, 80)));
    
    // Task 3a: Top 5 highest paid
    JButton top5Btn = createStyledButton("Top 5 Highest Paid");
    top5Btn.addActionListener(e -> showTop5HighestPaid());
    controlPanel.add(top5Btn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
    
    // Task 3b: Department payroll
    JButton deptPayrollBtn = createStyledButton("Department Payroll");
    deptPayrollBtn.addActionListener(e -> showDepartmentPayroll());
    controlPanel.add(deptPayrollBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
    
    // Monthly trend
    JButton monthlyBtn = createStyledButton("Monthly Payroll Trend");
    monthlyBtn.addActionListener(e -> showMonthlyTrend());
    controlPanel.add(monthlyBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
    
    // Salary distribution
    JButton distributionBtn = createStyledButton("Salary Distribution");
    distributionBtn.addActionListener(e -> showSalaryDistribution());
    controlPanel.add(distributionBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
    
    // Run all analysis
    JButton allAnalysisBtn = createStyledButton("Run All Analysis", new Color(46, 204, 113));
    allAnalysisBtn.addActionListener(e -> runAllAnalysis());
    controlPanel.add(allAnalysisBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    // Charts section
    JLabel chartLabel = new JLabel("Data Visualization");
    chartLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    chartLabel.setForeground(new Color(155, 89, 182));
    chartLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    controlPanel.add(chartLabel);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    // Chart buttons
    JButton lineChartBtn = createStyledButton("Monthly Payroll Trend", new Color(155, 89, 182));
    lineChartBtn.addActionListener(e -> showLineChart());
    controlPanel.add(lineChartBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));

    JButton barChartBtn = createStyledButton("Avg Salary by Dept", new Color(155, 89, 182));
    barChartBtn.addActionListener(e -> showBarChart());
    controlPanel.add(barChartBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    
    JButton distChartBtn = createStyledButton("Salary Distribution", new Color(155, 89, 182));
    distChartBtn.addActionListener(e -> showDistributionChart());
    controlPanel.add(distChartBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    
    JButton deptPayrollChartBtn = createStyledButton("Dept Payroll Comparison", new Color(155, 89, 182));
    deptPayrollChartBtn.addActionListener(e -> showDepartmentPayrollChart());
    controlPanel.add(deptPayrollChartBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    
    JButton pieChartBtn = createStyledButton("Dept Distribution", new Color(155, 89, 182));
    pieChartBtn.addActionListener(e -> showPieChart());
    controlPanel.add(pieChartBtn);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 8)));

    // Data management section
    controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    JLabel dataLabel = new JLabel("Data Management");
    dataLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
    dataLabel.setForeground(PRIMARY_COLOR);
    dataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    controlPanel.add(dataLabel);
    controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    
    JButton importDataBtn = createStyledButton("Import Data Tool", new Color(155, 89, 182));
    importDataBtn.addActionListener(e -> openDataImportTool());
    controlPanel.add(importDataBtn);
    

    controlPanel.add(Box.createVerticalGlue());
    
    return controlPanel;
}
    //chart methods
    private void showLineChart() {
        if (chartGenerator == null) {
            showError("Chart generator not initialized");
            return;
        }
        chartGenerator.monthlyPayrollLineChart();
    }

    private void showBarChart() {
        if (chartGenerator == null) {
            showError("Chart generator not initialized");
            return;
        }
        chartGenerator.averageSalaryBarChart();
    }
    private void showDistributionChart() {
        if (chartGenerator == null) {
            showError("Chart generator not initialized");
            return;
        }
        chartGenerator.salaryHistogramChart();
    }
    private void showDepartmentPayrollChart() {
        if (chartGenerator == null) {
            showError("Chart generator not initialized");
            return;
        }
        chartGenerator.departmentPayrollBarChart();

    }
    private void showPieChart() {
        if (chartGenerator == null) {
            showError("Chart generator not initialized");
            return;
        }
        chartGenerator.employeeDistributionPieChart();
    }

    private JPanel createResultPanel() {
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel resultLabel = new JLabel("Analysis Results");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setForeground(PRIMARY_COLOR);
        resultPanel.add(resultLabel, BorderLayout.NORTH);
        
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(248, 249, 250));
        
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        return resultPanel;
    }
    
    private JButton createStyledButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR);
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(color);
        // Choose readable foreground automatically
        button.setForeground(getReadableTextColor(color));

        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Reduce default button width so the left pane fits more controls
        // and keeps the employee trend selector visible.
        button.setPreferredSize(new Dimension(200, 30));
        button.setMaximumSize(new Dimension(200, 30));
        button.setMinimumSize(new Dimension(160, 26));
        button.setMargin(new Insets(6, 10, 6, 10));

        // subtle hover/pressed colors
        final Color hover = blend(color, Color.black, 0.12f);
        final Color pressed = blend(color, Color.black, 0.22f);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(hover);
                button.setForeground(getReadableTextColor(hover));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
                button.setForeground(getReadableTextColor(color));
            }

            public void mousePressed(MouseEvent evt) {
                button.setBackground(pressed);
                button.setForeground(getReadableTextColor(pressed));
            }

            public void mouseReleased(MouseEvent evt) {
                // return to hover or default depending on pointer
                button.setBackground(hover);
                button.setForeground(getReadableTextColor(hover));
            }
        });

        return button;
    }

    // ----- Button helpers -----
    private Color getReadableTextColor(Color bg) {
        double r = bg.getRed() / 255.0;
        double g = bg.getGreen() / 255.0;
        double b = bg.getBlue() / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return luminance < 0.6 ? Color.WHITE : Color.BLACK;
    }

    private Color blend(Color c1, Color c2, float ratio) {
        int rr = Math.max(0, Math.min(255, (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio)));
        int gg = Math.max(0, Math.min(255, (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio)));
        int bb = Math.max(0, Math.min(255, (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio)));
        return new Color(rr, gg, bb);
    }
    
    private void setupEventHandlers() {
        // Window listener to close database connection
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDatabaseConnection();
            }
        });
    }
    
    private void initializeDatabase() {
        try {
            connection = DatabaseConnection.getConnection();
            dataAnalyzer = new DataAnalyzer(connection);
            chartGenerator = new ChartGenerator(connection);
            loadEmployeeList();

            showMessage("Database connected successfully!\nReady to run analysis.");
        } catch (Exception ex) {
            showError("Database connection failed: " + ex.getMessage());
        }
    }
    
    private void loadEmployeeList() {
        try {
            java.util.Map<String,String> employees = dataAnalyzer.getAllEmployees();
            employeeSelector.removeAllItems();

            for (java.util.Map.Entry<String,String> e : employees.entrySet()) {
                employeeSelector.addItem(new EmployeeItem(e.getKey(), e.getValue()));
            }

            if (!employees.isEmpty()) {
                employeeSelector.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            showError("Error loading employee list: " + ex.getMessage());
        }
    }
    
    // Analysis Methods
    private void showAverageSalaryByDepartment() {
        try {
            String result = dataAnalyzer.getAverageSalaryByDepartmentFormatted();
            showMessage("AVERAGE SALARY BY DEPARTMENT\n" + 
                       "============================\n" + result);
        } catch (Exception ex) {
            showError("Error analyzing department salaries: " + ex.getMessage());
        }
    }
    
    private void showSalaryTrend() {
        EmployeeItem selected = (EmployeeItem) employeeSelector.getSelectedItem();
        if (selected == null) {
            showError("Please select an employee first");
            return;
        }

        String selectedId = selected.getId();
        String selectedName = selected.getName();

        try {
            String result = dataAnalyzer.getSalaryTrendFormatted(selectedId);
            if (result == null || result.trim().isEmpty() || result.contains("No payroll history")) {
                showMessage("No salary trend found for " + selectedId + " - " + selectedName);
            } else {
                showMessage("SALARY TREND FOR: " + selectedId + " - " + selectedName + "\n" +
                           "===================================\n" + result);
            }
        } catch (Exception ex) {
            showError("Error analyzing salary trend: " + ex.getMessage());
        }
    }
    
    private void showTop5HighestPaid() {
        try {
            String result = dataAnalyzer.getTop5HighestPaidFormatted();
            showMessage("TOP 5 HIGHEST PAID EMPLOYEES\n" +
                       "============================\n" + result);
        } catch (Exception ex) {
            showError("Error analyzing top earners: " + ex.getMessage());
        }
    }
    
    private void showDepartmentPayroll() {
        try {
            String result = dataAnalyzer.getDepartmentPayrollFormatted();
            showMessage("DEPARTMENT PAYROLL SUMMARY\n" +
                       "==========================\n" + result);
        } catch (Exception ex) {
            showError("Error analyzing department payroll: " + ex.getMessage());
        }
    }
    
    private void showMonthlyTrend() {
        try {
            String result = dataAnalyzer.getMonthlyPayrollTrendFormatted();
            showMessage("MONTHLY PAYROLL TREND\n" +
                       "=====================\n" + result);
        } catch (Exception ex) {
            showError("Error analyzing monthly trend: " + ex.getMessage());
        }
    }
    
    private void showSalaryDistribution() {
        try {
            String result = dataAnalyzer.getSalaryDistributionFormatted();
            showMessage("SALARY DISTRIBUTION ANALYSIS\n" +
                       "============================\n" + result);
        } catch (Exception ex) {
            showError("Error analyzing salary distribution: " + ex.getMessage());
        }
    }
    
    private void runAllAnalysis() {
        try {
            String result = dataAnalyzer.getAllAnalysisFormatted();
            showMessage("COMPREHENSIVE PAYROLL ANALYSIS\n" +
                       "===============================\n" + result);
        } catch (Exception ex) {
            showError("Error running complete analysis: " + ex.getMessage());
        }
    }
    
    private void openDataImportTool() {
        try {
            DataImportTool importTool = new DataImportTool();
            importTool.setVisible(true);
        } catch (Exception ex) {
            showError("Error opening data import tool: " + ex.getMessage());
        }
    }
    
    private void showMessage(String message) {
        resultArea.setText(message);
        resultArea.setCaretPosition(0);
    }
    
    private void showError(String error) {
        resultArea.setText("ERROR:\n" + error);
        resultArea.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException ex) {
            System.err.println("Error closing database connection: " + ex.getMessage());
        }
    }

    // Simple holder to display id + name in the combo box while keeping id accessible
    private static class EmployeeItem {
        private final String id;
        private final String name;

        EmployeeItem(String id, String name) {
            this.id = id;
            this.name = name == null ? "" : name;
        }

        String getId() { return id; }
        String getName() { return name; }

        @Override
        public String toString() { return id + " - " + name; }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Create and show main application
                PayrollAnalysisApp app = new PayrollAnalysisApp();
                app.setVisible(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}