package main.java.com.group3.payroll;
//import necessary libraries
import java.sql.*;
import java.util.*;
import java.io.*;

//Database connection class
public class DatabaseConnection{
    //define the configuration file
    private static final String PROPERTIES_FILE="config.properties";

    public static Connection getConnection() throws IOException,SQLException{
        Properties props = new Properties();

        //load properties from the configuration file
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)){
            props.load(input);

        }
        String url = props.getProperty("db.url","jdbc:mysql://localhost:3306/company_payroll");
        String user = props.getProperty("db.user","root");
        String password = props.getProperty("db.password","password");

        System.out.println("Connecting to "+url+" with user "+user);

        //return the database connection details
        try{
            return DriverManager.getConnection(url,user,password);
        }catch (SQLException e){
           if (e.getMessage().contains("Unknown database")){
                System.out.println("Database does not exist. Creating database...");
                createDatabase(user,password);
                return DriverManager.getConnection(url,user,password);

           }else{
               throw e;
           }
        }
    }
    //method to create the database if it does not exist
    private static void createDatabase (String user, String password) throws SQLException{
        String url = "jdbc:mysql://localhost:3306/";
        try (Connection conn = DriverManager.getConnection (url,user,password);
             Statement stmt = conn.createStatement()){
                 stmt.execute("CREATE DATABASE IF NOT EXISTS company_payroll");
                 System.out.println("Database 'company_payroll' created successfully.");

                 stmt.execute("USE company_payroll");

                 stmt.execute("USE company_roll");

                 createTables(stmt);
        }
    }
     private static void createTables(Statement stmt) throws SQLException {
        // Employees Table
        String createEmployees = 
            "CREATE TABLE IF NOT EXISTS Employees (" +
            "    employee_id VARCHAR(10) PRIMARY KEY," +
            "    first_name VARCHAR(50) NOT NULL," +
            "    last_name VARCHAR(50) NOT NULL," +
            "    department VARCHAR(50) NOT NULL," +
            "    hire_date DATE NOT NULL," +
            "    INDEX idx_department (department)," +
            "    INDEX idx_hire_date (hire_date)" +
            ")";
        stmt.execute(createEmployees);
        System.out.println(" Employees table created");
        
        // Salaries Table
        String createSalaries =
            "CREATE TABLE IF NOT EXISTS Salaries (" +
            "    salary_id VARCHAR(10) PRIMARY KEY," +
            "    employee_id VARCHAR(10) NOT NULL," +
            "    month VARCHAR(7) NOT NULL," +
            "    amount DECIMAL(10, 2) NOT NULL," +
            "    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id) ON DELETE CASCADE," +
            "    UNIQUE KEY unique_employee_month (employee_id, month)," +
            "    INDEX idx_employee_id (employee_id)," +
            "    INDEX idx_month (month)" +
            ")";
        stmt.execute(createSalaries);
        System.out.println(" Salaries table created");
    }
    
    // Initialize database method
     
    public static void initializeDatabase() throws SQLException, IOException {
        try (Connection conn = getConnection()) {
            System.out.println(" Database initialized and ready");
            
            // Verify tables exist
            verifyTables(conn);
        }
    }
    
    //Verify all tables exist and are accessible

    private static void verifyTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("Tables in database:");
            while (rs.next()) {
                System.out.println(" " + rs.getString(1));
            }
            
            // Check table counts
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Employees");
            if (rs.next()) {
                System.out.println("Employees records: " + rs.getInt(1));
            }
            
            rs = stmt.executeQuery("SELECT COUNT(*) FROM Salaries");
            if (rs.next()) {
                System.out.println("Salaries records: " + rs.getInt(1));
            }
        }
    }
    
   
}