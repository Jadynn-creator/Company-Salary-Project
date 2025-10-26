package main.java.com.group3.payroll;
//import necessary libraries
import java.sql.*;
import java.util.*;
import java.io.*;

//Database connection class
public class DatabaseConnection{
    //define the configuration file path
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

        //establish and return the database connection details
        return DriverManager.getConnection (url,user,password);
    }
    //initialize database schema
    public static void initializeDatabase()throws IOException,SQLException{
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()){

                //read the SQL schema from a file
                String schemaSql =readSQLFile("sql/database_schema.sql");
                String[] queries = schemaSql.split(";");

                for (String query:queries){
                    if (!query.trim().isEmpty()){
                        stmt.execute(query);
                    }
                }
                //print confirmation message
                System.out.println("Database initialized successfully.");

             }
        
        }
    //method to read SQL from a file
    private static String readSQLFile(String filepath)throws IOException{
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader (filepath))){
            String line;
            while ((line = reader.readLine()) !=null){
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
}
