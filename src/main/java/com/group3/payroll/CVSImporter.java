package main.java.com.group3.payroll;

import java.io.*;
import java.sql.*;
import java.util.*;

public class CVSImporter{
    private Connection connection;
    public CVSImporter(Connection connection){
        this.connection = connection;
    }
    // Method to import CSV data into the database
    //import employees from a CSV file
    public int importEmployees(String filepath)
     throws SQLException,IOException{
        int importedCount = 0;
        String sql = "INSERT INTO employees (id, name, department, salary) VALUES (?, ?, ?, ?)";
        //using a buffered reader for file reading
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath));
        //usimg a prepared statement to prevent SQL injection
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String line=reader.readLine();
                //skip the header line
                while ((line = reader.readLine()) !=null) {
                    String[] data = parseCVSLine(line);
                    if (data.length == 5){
                        pstmt.setString(1, data[0]);
                        //employee name
                        pstmt.setString(2, data[1]);
                        //first name
                        pstmt.setString(3, data[2]);
                        //last name 
                        pstmt.setString(4, data[3]);
                        //department
                        pstmt.setDate(5, java.sql.Date.valueOf(data[4]));
                        //date of hire

                        pstmt.executeUpdate();
                        importedCount++;
                    }
                
            }
     }
return importedCount;
}
    
    //import salaries from a CSV file
    public int importSalaries(String filepath)
        throws SQLException,IOException{
        int importedCount = 0;
        String sql = "INSERT INTO salaries (employee_id, amount, effective_date) VALUES (?, ?, ?)";
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath));
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String line;
                //skip the header line
                reader.readLine();
                while ((line = reader.readLine()) !=null) {
                    String[] data = parseCVSLine(line);
                    if (data.length == 4){
                        pstmt.setString(1, data[0]);
                        //salary id
                        pstmt.setString(2, data[1]);
                        //employee id
                        pstmt.setString(3,data[2]);
                        //month
                        pstmt.setDouble(4, Double.parseDouble(data[3]));
                        //amount

                        pstmt.executeUpdate();
                        importedCount++;
                    }
            
                }
            }
        return importedCount;
     }
    // Simple CSV line parser to handle commas within quoted fields
    private String[] parseCVSLine(String line){
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        for (char c : line.toCharArray()){
            if (c == '"'){
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes){
                tokens.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        tokens.add(field.toString());
        return tokens.toArray(new String[0]);
    }
//Clear existing data from tables
    public void clearTables() throws SQLException{
        try (Statement stmt = connection.createStatement()){
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            stmt.execute("TRUNCATE TABLE Salaries");
            stmt.execute("TRUNCATE TABLE Employees");
            stmt.execute("SET FOREIGN_KEY_CHECKS =1");
        }
    }

}
