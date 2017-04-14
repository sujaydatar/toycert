CREATE TABLE certificates (
    id INT NOT NULL AUTO_INCREMENT,
    common_name VARCHAR(255) NOT NULL,
    private_key BLOB(65535) NOT NULL, 
    customer_id INT NOT NULL, 
    content_link TEXT(1024) NOT NULL,	
    active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (ID),
    FOREIGN KEY fk_customer_id(customer_id) REFERENCES customers(id),
    UNIQUE common_name_customer_id(customer_id, common_name)
)ENGINE=INNODB;
