package main.java.com.group3;


import java.sql.*;
import java.io.*;
import java.util.Properties;

public class DatabaseInitializer {
    
    public static void setupDatabase() {
        System.out.println("Starting database setup...");
        
        try {
            
            createDatabaseIfNotExists();
          
            String username = getDBUsername();
            String password = getDBPassword();
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/company_payroll", username, password);
            
            
            createTablesIfNotExist(conn);
            
           
            verifyDatabaseSetup(conn);
            
            conn.close();
            System.out.println("Database setup completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Database setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createDatabaseIfNotExists() throws SQLException, IOException {
        String baseUrl = "jdbc:mysql://localhost:3306/";
        String username = getDBUsername();
        String password = getDBPassword();
        
        try (Connection conn = DriverManager.getConnection(baseUrl, username, password);
             Statement stmt = conn.createStatement()) {
            
            // Create database
            stmt.execute("CREATE DATABASE IF NOT EXISTS company_payroll");
            System.out.println("Database checked/created: company_payroll");
            
            // Grant privileges to user
            try {
                stmt.execute("GRANT ALL PRIVILEGES ON company_payroll.* TO '" + username + "'@'localhost'");
                System.out.println(" Privileges granted");
            } catch (SQLException e) {
                System.out.println("Note: Privileges already set");
            }
        }
    }
    
    private static void createTablesIfNotExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            
            // Employees table
            String employeesTable = 
                "CREATE TABLE IF NOT EXISTS Employees (" +
                "    employee_id VARCHAR(10) PRIMARY KEY," +
                "    first_name VARCHAR(50) NOT NULL," +
                "    last_name VARCHAR(50) NOT NULL," +
                "    department VARCHAR(50) NOT NULL," +
                "    hire_date DATE NOT NULL" +
                ")";
            stmt.execute(employeesTable);
            System.out.println("Employees table ready");
            
            // Salaries table
            String salariesTable =
                "CREATE TABLE IF NOT EXISTS Salaries (" +
                "    salary_id VARCHAR(10) PRIMARY KEY," +
                "    employee_id VARCHAR(10) NOT NULL," +
                "    month VARCHAR(7) NOT NULL," +
                "    amount DECIMAL(10, 2) NOT NULL," +
                "    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id) ON DELETE CASCADE" +
                ")";
            stmt.execute(salariesTable);
            System.out.println("Salaries table ready");
            
            // Create indexes for better performance
            stmt.execute("CREATE INDEX  idx_emp_dept ON Employees(department)");
            stmt.execute("CREATE INDEX  idx_sal_emp ON Salaries(employee_id)");
            stmt.execute("CREATE INDEX  idx_sal_month ON Salaries(month)");
            System.out.println("Indexes created");
        }
    }
    
    private static void verifyDatabaseSetup(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check if tables exist
            ResultSet rs = stmt.executeQuery(
                "SELECT TABLE_NAME FROM information_schema.tables " +
                "WHERE table_schema = 'company_payroll' " +
                "AND TABLE_NAME IN ('Employees', 'Salaries')"
            );
            
            int tableCount = 0;
            while (rs.next()) {
                tableCount++;
                System.out.println(" Table accessible: " + rs.getString("TABLE_NAME"));
            }
            
            if (tableCount == 2) {
                System.out.println(" All tables are properly set up");
            } else {
                System.out.println("Some tables might be missing");
            }
        }
    }
    
    private static String getDBUsername() throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            return props.getProperty("db.username", "root");
        }
    }
    
    private static String getDBPassword() throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            props.load(input);
            return props.getProperty("db.password", "");
        }
    }
}
