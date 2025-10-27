--NO LONGER NEEDED
CREATE Table Employees(
    employee_id VARCHAR(10) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    department VARCHAR(50)NOT NULL,
    hire_date DATE NOT NULL,

--Indexes for faster queries due to frequent searches    
    INDEX idx_department (department)
    INDEX idx_hire_date (hire_date)
);

--Table to store salary information
CREATE TABLE Salaries(
    salary_id VARCHAR(10) PRIMARY KEY,
    employee_id VARCHAR(10) NOT NULL,
    --Salary month in YYYY-MM format
    month VARCHAR(7) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES Employees(employee_id) ON DELETE CASCADE,
    --unique constraint to prevent duplicate salary entries for the same employee in the same month as suggested by Samuel Gitabi
    UNIQUE KEY unique_employee_month(employee_id, month),

    --index for faster queries
    INDEX idx_employee_id (employee_id),
    INDEX idx_month (month),
    INDEX idx_amount (amount)
);