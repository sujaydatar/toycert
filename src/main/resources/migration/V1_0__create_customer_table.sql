CREATE TABLE customers (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password CHAR(60) NOT NULL, 
    PRIMARY KEY (ID),
    UNIQUE(email)
) ENGINE=INNODB;
