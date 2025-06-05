create database swp_test2
use swp_test2

CREATE TABLE Users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(10) NOT NULL DEFAULT 'Guest' CHECK (role IN ('Guest', 'Member', 'Coach', 'Admin')),
    
);
 drop table Users