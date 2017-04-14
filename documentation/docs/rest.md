# API Endpoints

## Customer management

---
### Create customer

Request Body:
```
cat << EOF  > create_customer.json
{
    "name"  	: "John Doe",
    "email" 	: "john@domain.com",
    "password"	: "foobar"
}
EOF
```

cURL:
```
curl -X POST -H "Content-Type: application/json"  http://localhost:8080/api/v1/customers -d @create_customer.json
```

Successful Response:
```
HTTP/1.1 201 Created

{
	"id" : 100
}
```

---

### Delete customer
cURL:
```
curl -X DELETE -H "Content-Type: application/json"  http://localhost:8080/api/v1/customers/:customer_id
```

Successful Response:
```
HTTP/1.1 200 OK
```

## Certificate management

---
## Create certificate

Request Body:
```
cat <<EOF > create_certificate.json
{
    "commonName"  : "blog.domain.com",
    "organization" : "Acme, Inc",
    "country" : "USA",
    "state"   : "CA",
    "location" : "San Francisco"
}
EOF
```

cURL:
```
curl -X POST  -H "Content-Type: application/json" http://localhost:8080/api/v1/customers/:customer_id/certificates -d @create_certificate.json
```

Successful response:
```
HTTP/1.1 201 Created
{
	"id" : 43
}
```

## List active certificates:

cURL
```
curl -X GET -H "Content-Type: application/json" http://localhost:8080/api/v1/customers/:customer_id/certificates?active=true
```

Response body:
```
HTTP/1.1 200 OK
{
	certificates : [
        	"blog.domain.com",
        	"shopping.domain.com"
    ]
}
```

## De-activate/Revoke a certificate

Request body:
```
cat <<EOF > deactivate_certificate.json
{
    "active" : false,
    "callbackUrl" : "http://requestb.in/1hz517q1"
}
EOF
```

cURL:
```
curl -X POST -H "Content-Type: application/json" http://localhost:8080/api/v1/customers/:customer_id/certificates/:certificate_id -d @activate_certificate.json
```

Response:
```
HTTP/1.1 200 OK
```

## Activate a certificate

Request body:
```
cat <<EOF > activate_certificate.json
{
    "active" : true,
    "callbackUrl" : "http://requestb.in/1hz517q1"
}
EOF
```

cURL:
```
curl -X POST -H "Content-Type: application/json" http://localhost:8080/api/v1/customers/:customer_id/certificates/:certificate_id -d @activate_certificate.json
```

Response:
```
HTTP/1.1 200 OK
```

## Download a certificate

cURL:
```
curl -X GET -H "Accept: application/octet-stream" -O -J -L http://localhost:8080/api/v1/downloads/certificates/:certificate_id 
```



