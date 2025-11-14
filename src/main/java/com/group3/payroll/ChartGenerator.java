package main.java.com.group3.payroll;

//imported libraries
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChartGenerator {

    private Connection connection;

    public ChartGenerator(Connection connection) {
        this.connection = connection;}


    //Monthly payroll line chart    

    public void monthlyPayrollLineChart(){
        try{
            List<String> months = new ArrayList<>();
            List<Double> payrolls = new ArrayList<>();

            String sql ="SELECT month, SUM(amount) as total_payroll FROM Salaries GROUP BY month ORDER BY month";

            try(Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery (sql)){
                while (rs.next()){
                    months.add(rs.getString("month"));
                    payrolls.add(rs.getDouble("total_payroll"));
                }
            }

            //chart
            XYChart chart = new XYChartBuilder()
                    .width(800)
                    .height(500)
                    .title("Monthly Payroll Trend")
                    .xAxisTitle("Month")
                    .yAxisTitle("Total Payroll x 10^6")
                    .build();
            
            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            chart.getStyler().setPlotBackgroundColor(Color.white);
            chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);

            List<Integer> xData = new ArrayList<>();
            for (int i = 0; i < months.size(); i++) {
                xData.add(i + 1);
            }
            List<Double> payrollsInMillions = new ArrayList<>();
            for (Double payroll : payrolls) {
                payrollsInMillions.add(payroll / 1_000_000);
            }
            XYSeries series = chart.addSeries("Total Payroll", xData, payrollsInMillions);

            series.setMarker(SeriesMarkers.CIRCLE);
            series.setLineColor(new Color(41,128,185));
            series.setMarkerColor(new Color(52,152,219));

            showChart(chart, "Monthly Payroll Trend");

        } catch (SQLException e){
            showError("Error generating monthly payroll chart: " + e.getMessage());
        }
    }

    // Average Salary by Department Bar Chart

    public void averageSalaryBarChart(){
        try{
            List<String> departments = new ArrayList<>();
            List<Double> avgSalaries = new ArrayList<>();

            String sql = "SELECT e.department, AVG(s.amount) as avg_salary " +
                    "FROM Employees e " +
                    "JOIN Salaries s ON e.employee_id = s.employee_id " +
                    "GROUP BY e.department";

            try(Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()){
                    departments.add(rs.getString("department"));
                    avgSalaries.add(rs.getDouble("avg_salary"));
                }
            }

            //chart
            CategoryChart chart = new CategoryChartBuilder()
                    .width(800)
                    .height(500)
                    .title("Average Salary by Department")
                    .xAxisTitle("Department")
                    .yAxisTitle("Average Salary")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            chart.getStyler().setPlotBackgroundColor(Color.white);
            chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);

            chart.addSeries("Average Salary", departments, avgSalaries)
                    .setFillColor(new Color(52,152,219));

            showChart(chart, "Average Salary by Department");

        } catch (SQLException e){
            showError("Error generating average salary by department chart: " + e.getMessage());
        }
    }
    //Department Payroll Bar Chart

    public void departmentPayrollBarChart(){
        try{
            List<String> departments = new ArrayList<>();
            List<Double> totalPayrolls = new ArrayList<>();
            List<Double> employeeCounts = new ArrayList<>();

            String sql =  "SELECT e.department, SUM(s.amount) as total_payroll, " +
                        "COUNT(DISTINCT e.employee_id) as emp_count " +
                        "FROM Employees e JOIN Salaries s ON e.employee_id = s.employee_id " +
                        "GROUP BY e.department ORDER BY total_payroll DESC";

            try(Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()){
                    departments.add(rs.getString("department"));
                    totalPayrolls.add(rs.getDouble("total_payroll"));
                    employeeCounts.add(rs.getDouble("emp_count")*1000);
                }
            }

            //chart
            CategoryChart chart = new CategoryChartBuilder()
                    .width(800)
                    .height(500)
                    .title("Department Payroll Comparison")
                    .xAxisTitle("Department")
                    .yAxisTitle("Total Payroll x 10^6")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            chart.getStyler().setPlotBackgroundColor(Color.white);
            chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);

            List<Double> totalPayrollsInMillions = new ArrayList<>();
            for (Double payroll : totalPayrolls) {
                totalPayrollsInMillions.add(payroll / 1_000_000);
            }

            chart.addSeries("Total Payroll", departments, totalPayrollsInMillions)
                    .setFillColor(new Color(41,128,185));

            showChart(chart, "Total Payroll by Department");

        } catch (SQLException e){
            showError("Error generating total payroll by department chart: " + e.getMessage());
        }
    }

    // salart histogram chart

    public void salaryHistogramChart(){
        try{
            List<Double> salaries = new ArrayList<>();

            String sql = "SELECT amount FROM Salaries";

            try(Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()){
                    salaries.add(rs.getDouble("amount"));
                }
            }

            //chart
            Histogram histogram = new Histogram(salaries, 10);

            CategoryChart chart = new CategoryChartBuilder()
                    .width(1150)
                    .height(500)
                    .title("Salary Distribution")
                    .xAxisTitle("Salary Range")
                    .yAxisTitle("Number of Employees")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            chart.getStyler().setPlotBackgroundColor(Color.white);
            chart.getStyler().setPlotGridLinesColor(Color.LIGHT_GRAY);

            //bin creation
            List<Double> binCounts = new ArrayList<>();
            List<String> binLabels = new ArrayList<>();

            List<Double> yData = histogram.getyAxisData();

            List<Double> xAxisData = histogram.getxAxisData();
            double binWidth = 0;
            if (xAxisData.size() > 1) {
                binWidth = xAxisData.get(1) - xAxisData.get(0);
            }

            for (int i = 0; i < yData.size(); i++) {
                double binStart = xAxisData.get(i);
                binLabels.add(String.format("$%.0f - %.0f", binStart, binStart + binWidth));
                binCounts.add(yData.get(i));
            }

            chart.addSeries("Salary Distribution", binLabels, binCounts)
                    .setFillColor(new Color(155,89,182));

            showChart(chart, "Salary Distribution Histogram");

        } catch (SQLException e){
            showError("Error generating salary distribution histogram: " + e.getMessage());
        }
    }
    //Emoloyee distribution Pie Chart

    public void employeeDistributionPieChart(){
        try{
            List<String> departments = new ArrayList<>();
            List<Double> empCounts = new ArrayList<>();

            String sql = "SELECT department, COUNT(*) as emp_count " +
                    "FROM Employees GROUP BY department";

            try(Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)){
                while (rs.next()){
                    departments.add(rs.getString("department"));
                    empCounts.add(rs.getDouble("emp_count"));
                }
            }

            //chart
            PieChart chart = new PieChartBuilder()
                    .width(800)
                    .height(500)
                    .title("Employee Distribution by Department")
                    .build();

            chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);
            chart.getStyler().setChartBackgroundColor(Color.WHITE);

          Color[] colors = new Color[]{
                    new Color(231,76,60),
                    new Color(46,204,113),
                    new Color(52,152,219),
                    new Color(155,89,182),
                    new Color(241,196,15),
                    new Color(230,126,34)
            };

            for (int i = 0; i < departments.size(); i++) {
                Color color = colors[i % colors.length];
                 chart.addSeries(departments.get(i) + " (" + empCounts.get(i).intValue() + ")", 
                              empCounts.get(i))
                     .setFillColor(color);
            }

            showChart(chart, "Employee Distribution by Department");

        } catch (SQLException e){
            showError("Error generating employee distribution pie chart: " + e.getMessage());
        }
    }

     private void showChart(Chart<?, ?> chart, String title) {
        SwingUtilities.invokeLater(() -> {
            JDialog chartDialog = new JDialog();
            chartDialog.setTitle(title);
            chartDialog.setModal(true);
            chartDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            
            XChartPanel chartPanel = new XChartPanel(chart);
            chartDialog.getContentPane().add(chartPanel);
            
            chartDialog.pack();
            chartDialog.setLocationRelativeTo(null);
            chartDialog.setVisible(true);
        });
    }
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }
}
