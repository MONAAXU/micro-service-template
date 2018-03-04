#!/usr/bin/env bash
export PW=123456789 # this is not safe
echo $PW > password


export PW=`cat password`

# Create a self signed key pair root CA certificate.
keytool -genkeypair -v \
  -alias localCA \
  -dname "CN=localCA, OU=Example Org, O=Example Company, L=San Francisco, ST=California, C=US" \
  -keystore localCA.jks \
  -keypass:env PW \
  -storepass:env PW \
  -keyalg RSA \
  -keysize 4096 \
  -ext KeyUsage:critical="keyCertSign" \
  -ext BasicConstraints:critical="ca:true" \
  -validity 9999

# Export the localCA public certificate as localCA.crt so that it can be used in trust stores.
keytool -export -v \
  -alias localCA \
  -file localCA.crt \
  -keypass:env PW \
  -storepass:env PW \
  -keystore localCA.jks \
  -rfc