

# Lightweight DBMS with Java

This project implements a lightweight Database Management System (DBMS) in Java, offering essential database functionalities such as user authentication, database creation, table creation, query execution, and transaction control.

## 📂 Modules

### 1. **User Authentication**

#### User Registration Process:
- **Objective**: Verify user registration by prompting for username and password via the console. A unique UUID is generated for each user, and user details are saved in `users_authentication_information.txt`.

#### User Login Process:
- **Objective**: Verify if the user credentials exist and are correct. If correct, the user is prompted with a CAPTCHA for additional security.

### 2. **Database Creation**

#### Database Creation:
- **Objective**: Ensure users can create a database. Only one database is allowed per user. If a database already exists, the user can either delete it or continue using the existing one.

### 3. **Query Execution**

#### CREATE Table Query:
- **Objective**: Verify the creation of a table using the `CREATE TABLE` command. Upon success, two files are generated: one for the table's data and another for metadata related to the table's columns.

#### INSERT Query:
- **Objective**: Validate that values are correctly inserted into the table using the `INSERT` query.

#### SELECT Query:
- **Objective**: Ensure the `SELECT` query correctly retrieves and displays values stored in the database.

*#### Transaction Control:
- *Objective**: Validate that transactions are correctly implemented. Data should only be committed to the database after a `COMMIT` command.

## ⚙️ Features
- User registration and login with secure password storage.
- Creation of a personal database for each user.
- Support for `CREATE`, `INSERT`, and `SELECT` queries.
- Transaction control with support for `COMMIT` operations.

## 🛠️ Technologies Used
- **Language**: Java
- **Database**: File-based storage for user authentication and table data.
- **UUID**: Used for unique user identification.
- **CAPTCHA**: Added for additional security during login.

## 🏗️ Future Enhancements
- **Advanced Query Support**: Add support for more SQL queries like `UPDATE`, `DELETE`, and JOIN operations.
- **Improved Security**: Add encryption for user data and queries.
- **Multi-User Support**: Extend the system to support multiple concurrent users.

## 📝 How to Run
1. Clone the repository.
2. Compile and run the Java files.
3. Follow the console prompts to register or log in, create databases, and execute queries.
