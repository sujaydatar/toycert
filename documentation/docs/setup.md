# Setup

Toycert is written in scala and has been tested with scala version 2.12.+ and expects a JVM version 1.8 and above.

---

## Prerequisites

### Install Java 8

To install java with homebrew:
```
brew update

brew cask install java

```

Confirm that java 8 has been installed:
```
#> java -version

java version "1.8.0_112"
Java(TM) SE Runtime Environment (build 1.8.0_112-b16)
Java HotSpot(TM) 64-Bit Server VM (build 25.112-b16, mixed mode)
```

---

### Install MySQL or MariaDB

If you do not have a MySQL or MariaDB installion on your localhost:

```
brew install mariadb
brew services start mariadb
```

---

### Create MySQL user

---
For running toycert you will need to create a MySQL user and grant the appropriate privilege's to be able to create a database

```
mysql -u root -e "GRANT ALL PRIVILEGES ON *.* To 'testuser'@'localhost' IDENTIFIED BY 'testuser';"
```

###Running the application
The following command will run the service with default configuration stored in **config.yml** 

```
sbt run 
```



