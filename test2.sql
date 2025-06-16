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
CREATE TABLE password_reset_token (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    token NVARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    expiry_date DATETIME2 NOT NULL,
    CONSTRAINT FK_User_PasswordReset FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE question (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_text NVARCHAR(255) NOT NULL
);
CREATE TABLE question_option (
    id INT IDENTITY(1,1) PRIMARY KEY,
    question_id INT FOREIGN KEY REFERENCES question(id),
    option_text NVARCHAR(255) NOT NULL
);

-- Bảng kế hoạch bỏ thuốc
CREATE TABLE quit_plan (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    start_date DATE,
    target_date DATE,
    stages NVARCHAR(100),
    custom_plan NVARCHAR(MAX),
    CONSTRAINT FK_QuitPlan_User FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng lý do bỏ thuốc, liên kết với quit_plan
CREATE TABLE quit_reason (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    plan_id BIGINT NOT NULL,
    reason_text NVARCHAR(255) NOT NULL,
    CONSTRAINT FK_QuitReason_QuitPlan FOREIGN KEY (plan_id) REFERENCES quit_plan(id)
);




-- Question 1
INSERT INTO question (question_text) VALUES ('How old are you?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Under 18'),
    (SCOPE_IDENTITY(), '18-24'),
    (SCOPE_IDENTITY(), '25-34'),
    (SCOPE_IDENTITY(), '35-44'),
    (SCOPE_IDENTITY(), '45+');

-- Question 2
INSERT INTO question (question_text) VALUES ('How many cigarettes do you smoke per day?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'None, I don''t smoke daily'),
    (SCOPE_IDENTITY(), '1-5'),
    (SCOPE_IDENTITY(), '6-10'),
    (SCOPE_IDENTITY(), '11-20'),
    (SCOPE_IDENTITY(), 'More than 20');

-- Question 3
INSERT INTO question (question_text) VALUES ('At what age did you start smoking?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Under 16'),
    (SCOPE_IDENTITY(), '16-18'),
    (SCOPE_IDENTITY(), '19-25'),
    (SCOPE_IDENTITY(), 'Over 25');

-- Question 4
INSERT INTO question (question_text) VALUES ('How much money do you spend on cigarettes per week?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Less than 50,000 VND'),
    (SCOPE_IDENTITY(), '50,000 - 150,000 VND'),
    (SCOPE_IDENTITY(), '150,000 - 300,000 VND'),
    (SCOPE_IDENTITY(), 'More than 300,000 VND');

-- Question 5
INSERT INTO question (question_text) VALUES ('Have you tried to quit smoking before?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Yes, multiple times'),
    (SCOPE_IDENTITY(), 'Yes, once'),
    (SCOPE_IDENTITY(), 'No, I have never tried');

-- Question 6
INSERT INTO question (question_text) VALUES ('Do you smoke more when you''re stressed?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Yes, significantly more'),
    (SCOPE_IDENTITY(), 'Yes, slightly more'),
    (SCOPE_IDENTITY(), 'No, about the same'),
    (SCOPE_IDENTITY(), 'I smoke less when stressed');

-- Question 7
INSERT INTO question (question_text) VALUES ('Do you smoke indoors or outdoors?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Primarily indoors'),
    (SCOPE_IDENTITY(), 'Primarily outdoors'),
    (SCOPE_IDENTITY(), 'Both equally');

-- Question 8
INSERT INTO question (question_text) VALUES ('Do you live with someone who also smokes?');
INSERT INTO question_option (question_id, option_text) VALUES
    (SCOPE_IDENTITY(), 'Yes'),
    (SCOPE_IDENTITY(), 'No');






INSERT INTO password_reset_token (token, user_id, expiry_date)
VALUES ('9f3c75c2-91a4-4d13-aea4-78d021cd3f90', 1, DATEADD(HOUR, 2, GETDATE()));


INSERT INTO Users (username, password, email, first_name, last_name, role)
VALUES
('admin', '123', 'admin@example.com', 'Admin', 'User', 'Admin');

 drop table password_reset_token
 select * from password_reset_token
 select * from Users