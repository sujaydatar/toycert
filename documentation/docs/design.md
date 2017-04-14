# Design

The certificate metadata and customer account data are stored in MySQL. The actual certificate pem files are stored using a 
blob store interface. By default this is local file system. The idea is to have pluggable object storage backends for 
storing the certificate files.
