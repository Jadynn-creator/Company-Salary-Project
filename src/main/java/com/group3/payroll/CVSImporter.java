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
        String sql = "INSERT INTO employees (employee_id, first_name,last_name,department,hire_date) VALUES (?, ?, ?, ?, ?)";
        //using a buffered reader for file reading
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath));
        //usimg a prepared statement to prevent SQL injection
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String line=reader.readLine();
                //skip the header line
                while ((line = reader.readLine()) !=null) {
                    if(line.trim().isEmpty()){
                        continue; //skip empty lines
                    }
                    String[] data = parseCVSLine(line);
                    
                    if (data.length >=5){
                        try {
                            pstmt.setString(1,data[0].trim());//employee id
                            pstmt.setString(2,data[1].trim());//first name
                            pstmt.setString(3,data[2].trim());//last name
                            pstmt.setString(4,data[3].trim());//department

                            //to handle errors in importing the date
                            String dateStr= data[4].trim();
                            java.sql.Date hireDate = parseDate(dateStr);
                            pstmt.setDate(5,hireDate);//hire date

                            pstmt.executeUpdate();
                            importedCount++;
                        } catch (SQLException e){
                            System.err.println("Error importing line: " + Arrays.toString(data));
                            System.err.println("SQLException: " + e.getMessage());

                        }
                    } else {
                        System.err.println("Skipping invalid column " + data.length + " in line: " + line);
                    }
            }
     }
return importedCount;
}
    
    //import salaries from a CSV file
    public int importSalaries(String filepath)
        throws SQLException,IOException{
        int importedCount = 0;
        String sql = "INSERT INTO salaries (salary_id,employee_id,month,amount) VALUES (?, ?, ?, ?)";
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath));
            PreparedStatement pstmt = connection.prepareStatement(sql)) {
                String line;
                //skip the header line
                reader.readLine();
                while ((line = reader.readLine()) !=null) {
                    if(line.trim().isEmpty()){
                        continue; //skip empty 
                    }
                    String[] data = parseCVSLine(line);
                    if (data.length == 4){
                        try{
                        pstmt.setString(1, data[0].trim());
                        //salary id
                        pstmt.setString(2, data[1].trim());
                        //employee id
                        pstmt.setString(3,data[2].trim());
                        //month
                        pstmt.setDouble(4, Double.parseDouble(data[3].trim()));
                        //amount

                        pstmt.executeUpdate();
                        importedCount++;} catch (SQLException e){
                            System.err.println("Error importing salary: " + Arrays.toString(data));
                            System.err.println("SQLException: " + e.getMessage());
                        }
                    } else {
                        System.err.println("Skipping invalid column " + data.length + " in line: " + line);
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
    // Parse date from string in format YYYY-MM-DD since that is the format we expect
    private java.sql.Date parseDate(String dateStr){
        try {
           if (dateStr.contains("-")){
            return java.sql.Date.valueOf(dateStr);
           } else {
            System.err.println("Unrecognized date format: " + dateStr + ". Using current date instead.");
            return java.sql.Date.valueOf("2000-01-01");
           }
        } catch (IllegalArgumentException e){
            System.err.println("Invalid date format: " + dateStr + ". Using current date instead.");
            return java.sql.Date.valueOf("2000-01-01");
        }
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
//how to verify if the import was successful?
    public void verifyImport() throws SQLException{
        //uses sql statements to count records and check for orphaned salary records which do not have matching employees
        String countEmployees ="SELECT COUNT(*) AS total FROM Employees";
        String countSalaries ="SELECT COUNT(*) AS total FROM Salaries";
        String orphanCheck = "SELECT COUNT(*) AS orphaned FROM Salaries s LEFT JOIN Employees e ON s.employee_id = e.employee_id WHERE e.employee_id IS NULL";
        //using try to test and display number records imported and any orphaned records
        try (Statement stmt = connection.createStatement()){
            ResultSet rs = stmt.executeQuery(countEmployees);
            if (rs.next()){
                System.out.println("Total Employees Imported: " + rs.getInt("total"));
            }
            rs = stmt.executeQuery(countSalaries);
            if (rs.next()){
                System.out.println("Total Salaries Imported: " + rs.getInt("total"));
            }
            rs = stmt.executeQuery(orphanCheck);
            if (rs.next()){
                int orphans= rs.getInt("orphaned");
                if (orphans> 0){
                    System.out.println("Warning: There are " + orphans + " salary records without matching employees.");
                } else {
                    System.out.println("All salary records have matching employees.");
                }
            }
        }
    }
}
