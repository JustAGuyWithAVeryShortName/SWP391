create database swp_test2
use swp_test2

CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(10) NOT NULL DEFAULT 'Guest' CHECK (role IN ('Guest', 'Member', 'Coach', 'Admin'))
    
);
CREATE TABLE PasswordResetToken (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    token NVARCHAR(255) NOT NULL,
    user_id INT NOT NULL UNIQUE,
    expiryDate DATETIME2 NOT NULL,
    CONSTRAINT FK_User_PasswordReset FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

INSERT INTO PasswordResetToken (token, user_id, expiryDate)
VALUES ('9f3c75c2-91a4-4d13-aea4-78d021cd3f90', 1, DATEADD(HOUR, 2, GETDATE()));


INSERT INTO Users (username, password, email, first_name, last_name, role)
VALUES
('admin', '123', 'admin@example.com', 'Admin', 'User', 'Admin');

 drop table Users
 select * from Users