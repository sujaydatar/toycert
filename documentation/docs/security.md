# Security

## Customer Account Passwords

The customer account passwords are stored in the database using Bcrypt password hashing using the well tested jBcrypt library. 

## Certificate creation

When the service receives a request to create a certificate, it creates a self-signed X509v3 certificate using SHA256 hash and 2048-bit RSA keys.


