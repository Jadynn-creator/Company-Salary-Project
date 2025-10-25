#Entity Relationship Diagram

###Employees
-employee_id(PK) VARCHAR(10)
-first_name VARCHAR(50)
-last_name VARCHAR(50)
-department VARCHAR(50)
-hire_date DATE

###Salaries
-salary_id (PK) VARCHAR(10)
-employee_id(FK) VARCHAR(10)
-month VARCHAR (7)
-amount DECIMAL (10,2)

##Relationship
Employees(1)----<(Many)Salaries
