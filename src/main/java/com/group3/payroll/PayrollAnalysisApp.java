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

/**
 * Main application window for the Employee Salary Analysis System.
 * It sets up the Swing GUI, manages the layout, initializes database connections,
 * and handles user interactions to trigger payroll analysis and visualization.
 */
public class PayrollAnalysisApp extends JFrame {
    // Database connection object
    private Connection connection;
    // Core  component for fetching and processing payroll data
    private DataAnalyzer dataAnalyzer;
    // Component for displaying textual analysis results
    private JTextArea resultArea;
    // Combo box for selecting individual employees for trend analysis
    private JComboBox<EmployeeItem> employeeSelector;
    // Component for generating and displaying data visualizations (charts)
    private ChartGenerator chartGenerator;
    
    //  UI/Styling Constants
    // Primary brand color (Dark Blue)
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    // Secondary action color (Brighter Blue)
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    // Light background color
    private final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    
    /**
     * Constructor: Initializes the UI, sets up event handlers, and connects to the database.
     */
    public PayrollAnalysisApp() {
        initializeUI();
        setupEventHandlers();
        initializeDatabase();
    }
    
    /**
     * Sets up the main structure and components of the graphical user interface (GUI).
     */
    private void initializeUI() {
        setTitle("Employee Salary Analysis System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750); // Set initial window size
        setLocationRelativeTo(null); // Center the window on the screen
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Use a main panel with BorderLayout to structure the application into sections (Header, Controls, Results)
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10)); // 10-pixel gap
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Padding around the content
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Create and add the header panel (Title) 
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create the control panel (Buttons and selector)
        JPanel controlPanel = createControlPanel();

        // Wrap the control panel in a JScrollPane to allow scrolling if controls overflow vertically
        JScrollPane scrollableControlPanel = new JScrollPane(controlPanel);
        scrollableControlPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollableControlPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // No horizontal scroll
        scrollableControlPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollableControlPanel.getVerticalScrollBar().setUnitIncrement(16); // Faster scrolling
        
        
        // Set a preferred size for the control panel area (WEST)
        scrollableControlPanel.setPreferredSize(new Dimension(300, 650));
        scrollableControlPanel.setMinimumSize(new Dimension(220, 200));
        
        // Add the scrollable control panel to the WEST (left side)
        mainPanel.add(scrollableControlPanel, BorderLayout.WEST);
        
        // Create and add the result area panel to the CENTER (main display)
        JPanel resultPanel = createResultPanel();
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        
        // Add the main content panel to the JFrame
        add(mainPanel);
    }
    
    /**
     * Creates the header panel with the application title.
     * @return The configured header panel.
     */
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
    
    /**
     * Creates the panel containing all analysis and chart buttons, and the employee selector.
     * Uses BoxLayout for vertical stacking.
     * @return The configured control panel.
     */
   private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        // Use BoxLayout for a simple vertical list of components
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        
        // Section title for Analysis Tools
        JLabel sectionLabel = new JLabel("Analysis Tools");
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sectionLabel.setForeground(PRIMARY_COLOR);
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align components to the left
        controlPanel.add(sectionLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Task 1: Average salary per department button
        JButton avgSalaryBtn = createStyledButton("Average Salary by Department");
        avgSalaryBtn.addActionListener(e -> showAverageSalaryByDepartment());
        controlPanel.add(avgSalaryBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        
        // Task 2: Salary trend - Grouped label, selector, and button
        JPanel trendPanel = new JPanel();
        trendPanel.setLayout(new BoxLayout(trendPanel, BoxLayout.Y_AXIS));
        trendPanel.setBackground(Color.WHITE);
        trendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel trendLabel = new JLabel("Employee Salary Trend:");
        trendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        trendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        trendPanel.add(trendLabel);
        trendPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel trendControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); // Horizontal grouping
        trendControls.setBackground(Color.WHITE);
        trendControls.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Employee selector combo box
        employeeSelector = new JComboBox<>();
        employeeSelector.setPrototypeDisplayValue(new EmployeeItem("XXXXXXXX","Firstname Lastname")); 
        employeeSelector.setPreferredSize(new Dimension(190, 28));
        employeeSelector.setMaximumSize(new Dimension(190, 28));
        employeeSelector.setMinimumSize(new Dimension(160, 24));

        JButton trendBtn = createStyledButton("Show");
        trendBtn.setPreferredSize(new Dimension(55, 28));
        trendBtn.setMaximumSize(new Dimension(55, 28));
        trendBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        trendBtn.addActionListener(e -> showSalaryTrend());

        // Add controls to the horizontal panel
        trendControls.add(employeeSelector);
        trendControls.add(trendBtn);
        trendPanel.add(trendControls);
        
        // Add the grouped trend panel to the main control panel
        controlPanel.add(trendPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 80))); // Separator space btween employee selector and other buttons
        
        // Task 3a: Top 5 highest paid button
        JButton top5Btn = createStyledButton("Top 5 Highest Paid");
        top5Btn.addActionListener(e -> showTop5HighestPaid());
        controlPanel.add(top5Btn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Task 3b: Department payroll summary button
        JButton deptPayrollBtn = createStyledButton("Department Payroll");
        deptPayrollBtn.addActionListener(e -> showDepartmentPayroll());
        controlPanel.add(deptPayrollBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Monthly trend button
        JButton monthlyBtn = createStyledButton("Monthly Payroll Trend");
        monthlyBtn.addActionListener(e -> showMonthlyTrend());
        controlPanel.add(monthlyBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Salary distribution button
        JButton distributionBtn = createStyledButton("Salary Distribution");
        distributionBtn.addActionListener(e -> showSalaryDistribution());
        controlPanel.add(distributionBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        // Run all analysis button 
        JButton allAnalysisBtn = createStyledButton("Run All Analysis", new Color(46, 204, 113)); // Green color to make it special
        allAnalysisBtn.addActionListener(e -> runAllAnalysis());
        controlPanel.add(allAnalysisBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        //  Data Visualization Section 
        JLabel chartLabel = new JLabel("Data Visualization");
        chartLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chartLabel.setForeground(new Color(155, 89, 182)); // Purple color for charts
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

        //  Data Management Section 
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel dataLabel = new JLabel("Data Management");
        dataLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dataLabel.setForeground(PRIMARY_COLOR);
        dataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(dataLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Import data tool button
        JButton importDataBtn = createStyledButton("Import Data Tool", new Color(155, 89, 182));
        importDataBtn.addActionListener(e -> openDataImportTool());
        controlPanel.add(importDataBtn);
        
        // Use vertical glue to push all components to the top
        controlPanel.add(Box.createVerticalGlue());
        
        return controlPanel;
    }
    
    //  Chart Display Methods 
    private void showLineChart() {
        if (chartGenerator == null) {
            showError("Chart generator not initialized");
            return;
        }
        // Delegates the call to the ChartGenerator class
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

    /**
     * Creates the panel containing the JTextArea for displaying textual analysis results.
     * @return The configured result panel.
     */
    private JPanel createResultPanel() {
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);
        // Add a compound border for line and padding
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Label for the result area
        JLabel resultLabel = new JLabel("Analysis Results");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setForeground(PRIMARY_COLOR);
        resultPanel.add(resultLabel, BorderLayout.NORTH);
        
        // Text area to hold the analysis results
        resultArea = new JTextArea();
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // Monospace font for tabular data
        resultArea.setEditable(false);
        resultArea.setBackground(new Color(248, 249, 250)); // Slightly off-white background
        
        // Scroll pane for the text area
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        return resultPanel;
    }
    
    /**
     * Helper method to create a default styled button.
     * @param text The button text.
     * @return The styled JButton.
     */
    private JButton createStyledButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR);
    }
    
    /**
     * Helper method to create a custom-colored styled button with hover effects.
     * @param text The button text.
     * @param color The background color of the button.
     * @return The styled JButton.
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(color);
        // Automatically determine if foreground should be WHITE or BLACK for readability
        button.setForeground(getReadableTextColor(color));

        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Set fixed dimensions for consistency
        button.setPreferredSize(new Dimension(200, 30));
        button.setMaximumSize(new Dimension(200, 30));
        button.setMinimumSize(new Dimension(160, 26));
        button.setMargin(new Insets(6, 10, 6, 10));

        // Calculate colors for hover and pressed states
        final Color hover = blend(color, Color.black, 0.12f);
        final Color pressed = blend(color, Color.black, 0.22f);

        // Add mouse listeners for interactive effects
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
                // Returns to hover state if the mouse is still over the button
                if (button.contains(evt.getPoint())) {
                     button.setBackground(hover);
                     button.setForeground(getReadableTextColor(hover));
                } else {
                     button.setBackground(color);
                     button.setForeground(getReadableTextColor(color));
                }
            }
        });

        return button;
    }

    //  Button Helper Methods for Styling 
    /**
     * Determines a readable text color (White or Black) based on the background color's luminance.
     * @param bg The background color.
     * @return Color.WHITE or Color.BLACK.
     */
    private Color getReadableTextColor(Color bg) {
        // Calculate relative luminance
        double r = bg.getRed() / 255.0;
        double g = bg.getGreen() / 255.0;
        double b = bg.getBlue() / 255.0;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        // Use White text for dark backgrounds (luminance < 0.6), Black for light backgrounds
        return luminance < 0.6 ? Color.WHITE : Color.BLACK;
    }

    /**
     * Blends two colors together by a given ratio.
     * @param c1 The base color.
     * @param c2 The color to blend with (e.g., Color.BLACK for darkening).
     * @param ratio The blending ratio (0.0 to 1.0).
     * @return The resulting blended color.
     */
    private Color blend(Color c1, Color c2, float ratio) {
        int rr = Math.max(0, Math.min(255, (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio)));
        int gg = Math.max(0, Math.min(255, (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio)));
        int bb = Math.max(0, Math.min(255, (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio)));
        return new Color(rr, gg, bb);
    }
    
    /**
     * Sets up a window listener to ensure the database connection is closed when the app closes.
     */
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDatabaseConnection();
            }
        });
    }
    
    /**
     * Initializes the database connection and the data processing components.
     */
    private void initializeDatabase() {
        try {
            // Assumes DatabaseConnection.getConnection() handles driver loading and connection pooling
            connection = DatabaseConnection.getConnection(); 
            dataAnalyzer = new DataAnalyzer(connection);
            chartGenerator = new ChartGenerator(connection);
            loadEmployeeList();

            showMessage("Database connected successfully!\nReady to run analysis.");
        } catch (Exception ex) {
            showError("Database connection failed: " + ex.getMessage());
        }
    }
    
    /**
     * Fetches and populates the employee selection combo box.
     */
    private void loadEmployeeList() {
        try {
            java.util.Map<String,String> employees = dataAnalyzer.getAllEmployees();
            employeeSelector.removeAllItems();

            // Populate the selector with EmployeeItem objects (ID + Name)
            for (java.util.Map.Entry<String,String> e : employees.entrySet()) {
                employeeSelector.addItem(new EmployeeItem(e.getKey(), e.getValue()));
            }

            if (!employees.isEmpty()) {
                employeeSelector.setSelectedIndex(0); // Select the first employee by default
            }
        } catch (Exception ex) {
            showError("Error loading employee list: " + ex.getMessage());
        }
    }
    
    //  Analysis Methods (fowards the  work to DataAnalyzer)
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
            // Creates and displays the separate DataImportTool window
            DataImportTool importTool = new DataImportTool();
            importTool.setVisible(true);
        } catch (Exception ex) {
            showError("Error opening data import tool: " + ex.getMessage());
        }
    }
    
    // UI Utility Methods 
    /**
     * Clears the result area and displays the given message.
     * @param message The message to display.
     */
    private void showMessage(String message) {
        resultArea.setText(message);
        resultArea.setCaretPosition(0); // Scroll to the top
    }
    
    /**
     * Displays an error message in the result area and a JOptionPane dialog.
     * @param error The error message.
     */
    private void showError(String error) {
        resultArea.setText("ERROR:\n" + error);
        resultArea.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Closes the database connection safely.
     */
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

    /**
     * Simple inner class to hold an employee's ID and Name, 
     * used as items in the JComboBox.
     * The toString() method ensures the correct display format.
     */
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
    
    /**
     * Main method: Entry point of the application.
     * Sets the system look and feel and runs the application on the Event Dispatch Thread (EDT).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for native UI appearance
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