create database swp_test2
use swp_test2

CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
	gender VARCHAR(10) CHECK (gender IN ('Male', 'Female')),
    date_of_birth DATE,
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

CREATE TABLE user_answer (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL FOREIGN KEY REFERENCES Users(user_id),
    question_id INT NOT NULL FOREIGN KEY REFERENCES question(id),
    option_id INT NOT NULL FOREIGN KEY REFERENCES question_option(id),
    created_at DATETIME DEFAULT GETDATE()
);

CREATE TABLE analysis_result (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL FOREIGN KEY REFERENCES users(user_id),
    analysis NVARCHAR(255) NOT NULL,
    recommendation NVARCHAR(MAX) NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);

-- Bảng chính lưu kế hoạch bỏ thuốc
CREATE TABLE quit_plan (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    start_date DATE,
    target_date DATE,
    stages NVARCHAR(255),
    custom_plan NVARCHAR(2000),
    user_id INT,
    CONSTRAINT FK_quit_plan_user FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- Bảng phụ lưu danh sách lý do (1 kế hoạch có nhiều lý do)
CREATE TABLE quit_plan_reasons (
    plan_id BIGINT NOT NULL,
    reason NVARCHAR(255) NOT NULL,
    CONSTRAINT FK_quit_plan_reasons_plan FOREIGN KEY (plan_id) REFERENCES quit_plan(id) ON DELETE CASCADE
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








INSERT INTO Users (username, password, email, first_name, last_name, role)
VALUES
('admin', '123', 'admin@example.com', 'Admin', 'User', 'Admin');

 drop table Users
 select * from password_reset_token
 select * from user_answer
  select * from Users

DELETE FROM user_answer WHERE question_id IN (SELECT id FROM question);
DELETE FROM question_option WHERE question_id IN (SELECT id FROM question);
DELETE FROM question;



-- xem question
 SELECT q.id AS question_id, q.question_text, o.id AS option_id, o.option_text
FROM question q
JOIN question_option o ON q.id = o.question_id
ORDER BY q.id, o.id;


-- Xem quit plan
DELETE FROM quit_plan WHERE user_id IS NULL;

SELECT 
    qp.id AS plan_id,
    qp.start_date,
    qp.target_date,
    qp.stages,
    qp.custom_plan,
    qp.user_id,
    qpr.reason
FROM 
    quit_plan qp
LEFT JOIN 
    quit_plan_reasons qpr ON qp.id = qpr.plan_id
ORDER BY 
    qp.id;
	