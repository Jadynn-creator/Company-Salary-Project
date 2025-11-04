package main.java.com.group3.payroll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DataAnalyzer {
    private Connection connection;
    private DecimalFormat currencyFormatter = new DecimalFormat("$###,##0.00");

    // This CTE (Common Table Expression) is a reusable SQL snippet.
    // It finds the single *most recent* salary for every employee.
    // We use this for all "current" salary calculations.
    private final String LATEST_SALARY_CTE = 
        "WITH LatestSalary AS ( " +
        "    SELECT employee_id, amount, " +
        "           ROW_NUMBER() OVER(PARTITION BY employee_id ORDER BY month DESC) as rn " +
        "    FROM salaries " +
        ") ";

    public DataAnalyzer(Connection connection) {
        this.connection = connection;
    }

    /**
     * Gets a list of all employee IDs to populate the dropdown.
     * (Matches your 'employees' table)
     */
    public List<String> getAllEmployeeIds() throws SQLException {
        List<String> employeeIds = new ArrayList<>();
        String sql = "SELECT employee_id FROM employees ORDER BY employee_id";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                employeeIds.add(rs.getString("employee_id"));
            }
        }
        return employeeIds;
    }

    /**
     * Task 1: Get average "current" salary per department.
     * (Uses your 'employees.department' and 'salaries.amount')
     */
    public String getAverageSalaryByDepartmentFormatted() throws SQLException {
        StringBuilder sb = new StringBuilder();
        // This query joins employees with their *latest* salary, 
        // then calculates the average of those salaries by department.
        String sql = LATEST_SALARY_CTE +
                     "SELECT e.department, AVG(ls.amount) as avg_salary " +
                     "FROM employees e " +
                     "JOIN LatestSalary ls ON e.employee_id = ls.employee_id " +
                     "WHERE ls.rn = 1 " + // Filter for only the latest salary
                     "GROUP BY e.department " +
                     "ORDER BY avg_salary DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append(String.format("- %-15s: %s\n",
                        rs.getString("department"),
                        currencyFormatter.format(rs.getDouble("avg_salary"))));
            }
        }
        return sb.toString();
    }

    /**
     * Task 2: Get salary trend for a specific employee.
     * (Uses your 'salaries' table)
     */
    public String getSalaryTrendFormatted(String employeeId) throws SQLException {
        StringBuilder sb = new StringBuilder();
        // Assumes 'month' is a text or date field that can be ordered
        String sql = "SELECT month, amount FROM salaries " +
                     "WHERE employee_id = ? " +
                     "ORDER BY month ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    return "No payroll history found for this employee.";
                }
                while (rs.next()) {
                    sb.append(String.format("- %s: %s\n",
                            rs.getString("month"), // Use getString, as 'month' type is flexible
                            currencyFormatter.format(rs.getDouble("amount"))));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Task 3a: Get top 5 highest-paid employees based on their "current" salary.
     * (Uses 'first_name', 'last_name', and latest 'amount')
     */
    public String getTop5HighestPaidFormatted() throws SQLException {
        StringBuilder sb = new StringBuilder();
        // This query joins employees with their *latest* salary,
        // then orders by that salary amount.
        String sql = LATEST_SALARY_CTE +
                     "SELECT e.first_name, e.last_name, ls.amount " +
                     "FROM employees e " +
                     "JOIN LatestSalary ls ON e.employee_id = ls.employee_id " +
                     "WHERE ls.rn = 1 " + // Filter for only the latest salary
                     "ORDER BY ls.amount DESC " +
                     "LIMIT 5";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                sb.append(String.format("%d. %s %s: %s\n",
                        rank++,
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        currencyFormatter.format(rs.getDouble("amount"))));
            }
        }
        return sb.toString();
    }

    /**
     * Task 3b: Get total "current" payroll cost per department.
     * (Uses 'department' and sums latest 'amount' for all employees in it)
     */
    public String getDepartmentPayrollFormatted() throws SQLException {
        StringBuilder sb = new StringBuilder();
        // This query sums the *latest* salaries for all employees, grouped by department.
        String sql = LATEST_SALARY_CTE +
                     "SELECT e.department, SUM(ls.amount) as total_payroll " +
                     "FROM employees e " +
                     "JOIN LatestSalary ls ON e.employee_id = ls.employee_id " +
                     "WHERE ls.rn = 1 " + // Filter for only the latest salary
                     "GROUP BY e.department " +
                     "ORDER BY total_payroll DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append(String.format("- %-15s: %s\n",
                        rs.getString("department"),
                        currencyFormatter.format(rs.getDouble("total_payroll"))));
            }
        }
        return sb.toString();
    }

    /**
     * Get company-wide payroll spending trend by month.
     * (Uses your 'salaries' table 'month' and 'amount')
     */
    public String getMonthlyPayrollTrendFormatted() throws SQLException {
        StringBuilder sb = new StringBuilder();
        // This sums all salary records for each month.
        String sql = "SELECT month, SUM(amount) as total_payroll " +
                     "FROM salaries " +
                     "GROUP BY month " +
                     "ORDER BY month ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append(String.format("- %s: %s\n",
                        rs.getString("month"),
                        currencyFormatter.format(rs.getDouble("total_payroll"))));
            }
        }
        return sb.toString();
    }

    /**
     * Get the distribution of "current" salaries in buckets.
     * (Uses latest 'amount' for all employees)
     */
    public String getSalaryDistributionFormatted() throws SQLException {
        StringBuilder sb = new StringBuilder();
        // This query uses the *latest* salary for each employee
        // and buckets them into ranges.
        String sql = LATEST_SALARY_CTE +
                     "SELECT " +
                     "  CASE " +
                     "    WHEN ls.amount < 50000 THEN '< $50,000' " +
                     "    WHEN ls.amount BETWEEN 50000 AND 74999 THEN '$50,000 - $74,999' " +
                     "    WHEN ls.amount BETWEEN 75000 AND 99999 THEN '$75,000 - $99,999' " +
                     "    WHEN ls.amount BETWEEN 100000 AND 149999 THEN '$100,000 - $149,999' " +
                     "    ELSE '$150,000+' " +
                     "  END as salary_range, " +
                     "  COUNT(*) as employee_count " +
                     "FROM LatestSalary ls " +
                     "WHERE ls.rn = 1 " + // Filter for only the latest salary
                     "GROUP BY salary_range " +
                     "ORDER BY " + // Custom order to get ranges to display logically
                     "  CASE " +
                     "    WHEN salary_range = '< $50,000' THEN 1 " +
                     "    WHEN salary_range = '$50,000 - $74,999' THEN 2 " +
                     "    WHEN salary_range = '$75,000 - $99,999' THEN 3 " +
                     "    WHEN salary_range = '$100,000 - $149,999' THEN 4 " +
                     "    ELSE 5 " +
                     "  END";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                sb.append(String.format("- %-20s: %d employees\n",
                        rs.getString("salary_range"),
                        rs.getInt("employee_count")));
            }
        }
        return sb.toString();
    }

    /**
     * Runs all major analyses and combines them into one report.
     * (This just calls the other methods in this file)
     */
    public String getAllAnalysisFormatted() throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("AVERAGE SALARY BY DEPARTMENT\n");
        sb.append("============================\n");
        sb.append(getAverageSalaryByDepartmentFormatted()).append("\n\n");

        sb.append("TOP 5 HIGHEST PAID EMPLOYEES\n");
        sb.append("============================\n");
        sb.append(getTop5HighestPaidFormatted()).append("\n\n");

        sb.append("DEPARTMENT PAYROLL SUMMARY\n");
        sb.append("==========================\n");
        sb.append(getDepartmentPayrollFormatted()).append("\n\n");

        sb.append("MONTHLY PAYROLL TREND\n");
        sb.append("=====================\n");
        sb.append(getMonthlyPayrollTrendFormatted()).append("\n\n");

        sb.append("SALARY DISTRIBUTION ANALYSIS\n");
        sb.append("============================\n");
        sb.append(getSalaryDistributionFormatted()).append("\n");

        return sb.toString();
    }
}