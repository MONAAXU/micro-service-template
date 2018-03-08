#!/usr/bin/env bash
export PW=`cat password`

# Export localhost's public certificate for use with nginx.
keytool -export -v \
  -alias localhost \
  -file localhost.crt \
  -keypass:env PW \
  -storepass:env PW \
  -keystore localhost.jks \
  -rfc

# Create a PKCS#12 keystore containing the public and private keys.
keytool -importkeystore -v \
  -srcalias localhost \
  -srckeystore localhost.jks \
  -srcstoretype jks \
  -srcstorepass:env PW \
  -destkeystore localhost.p12 \
  -destkeypass:env PW \
  -deststorepass:env PW \
  -deststoretype PKCS12

# Export the localhost private key for use in nginx.  Note this requires the use of OpenSSL.
openssl pkcs12 \
  -nocerts \
  -nodes \
  -passout env:PW \
  -passin env:PW \
  -in localhost.p12 \
  -out localhost.key