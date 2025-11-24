package main.java.com.group3.payroll;
//import necessary libraries
import javax.swing.*;
import javax.swing.filechooser.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.io.File;

//main class for the data import tool
public class DataImportTool extends JFrame{
    private JTextArea logArea;
    private JButton importEmployees,importSalaries,initializeDB;
    private CVSImporter cvsImporter;
    private Connection connection;

    public DataImportTool() {
    initializeUI();
    setupEventHandlers();
    }

    //UI setup

    private void initializeUI(){
        setTitle("CSV Data Import Tool");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600,400);
        setLocationRelativeTo(null);

        //main panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        //buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        initializeDB = new JButton("Initialize Database");
        importEmployees = new JButton("Import Employees CSV");
        importSalaries = new JButton("Import Salaries CSV");

        buttonPanel.add(initializeDB);
        buttonPanel.add(importEmployees);
        buttonPanel.add(importSalaries);

        //log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        mainPanel.add(buttonPanel,BorderLayout.NORTH);
        mainPanel.add(scrollPane,BorderLayout.CENTER);

        add(mainPanel);
    }
    //event handlers
    private void setupEventHandlers(){
        initializeDB.addActionListener(e -> initializeDatabase());
        importEmployees.addActionListener(e -> importEmployees());
        importSalaries.addActionListener(e -> importSalaries());

    }
   private void initializeDatabase() {
    try {
        // Use the comprehensive database setup
        DatabaseInitializer.setupDatabase();
        connection = DatabaseConnection.getConnection();
        cvsImporter = new CVSImporter(connection);
        log("Database initialized successfully");
    } catch (Exception ex) {
        log("Error initializing database: " + ex.getMessage());
        ex.printStackTrace();
    }
}
// Method to import employees from a CSV file
    private void importEmployees(){
        if (connection == null){
            log("Please initialize the database first.\n");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Employees CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files","csv"));
        fileChooser.setCurrentDirectory(new File("data"));

        int result = fileChooser.showOpenDialog(this);
        if (result ==JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();
            try {
                int count = cvsImporter.importEmployees(fileChooser.getSelectedFile().getAbsolutePath());
                log("Imported " + count + " employees from " + selectedFile.getName() + "\n");
                cvsImporter.verifyImport();
            } catch (Exception ex){
                log("Error importing employees: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }
    }
    // Method to import salaries from a CSV file
    private void importSalaries(){
        if (connection == null){
            log("Please initialize the database first.\n");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Salaries CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files","csv"));
        fileChooser.setCurrentDirectory(new File("data"));

        int result = fileChooser.showOpenDialog(this);
        if (result ==JFileChooser.APPROVE_OPTION){
            File selectedFile = fileChooser.getSelectedFile();
            try {
                int count = cvsImporter.importSalaries(fileChooser.getSelectedFile().getAbsolutePath());
                log("Imported " + count + " salaries from " + selectedFile.getName() + "\n");
                cvsImporter.verifyImport();
            } catch (Exception ex){
                log("Error importing salaries: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }
    }

    //logging method
    private void log(String message){
        SwingUtilities.invokeLater(() -> {
            logArea.append("["+ new java.util.Date() +"] " + message+"\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } );
    }
    //main method to run the tool
    public static void main (String[] args){
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e){
                e.printStackTrace();
            }
            new DataImportTool().setVisible(true);
        });
    }
}



