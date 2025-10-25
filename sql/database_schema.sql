--Database schema for Company Salary Project
CREATE DATABASE IF NOT EXISTS company_payroll;
USE company_payroll;

--Table to store employee information
CREATE Table Employees(
    employee_id VARCHAR(10) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(50)NOT NULL,
    hire_date DATE NOT NULL,
    
);
--Table to store salary information
CREATE TABLE Salaries(
    salary_id VARCHAR(10) PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    month VARCHAR(7) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id)
);