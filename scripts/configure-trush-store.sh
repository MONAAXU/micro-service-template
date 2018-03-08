#!/usr/bin/env bash
export PW=`cat password`

storePassword=123456789

# Create a JKS keystore that trusts the example CA, with the default password.
keytool -import -v \
  -alias localCA \
  -file localCA.crt \
  -keypass:env PW \
  -storepass $storePassword \
  -keystore serverStore.jks << EOF
yes
EOF

# List out the details of the store password.
keytool -list -v \
  -keystore serverStore.jks \
  -storepass $storePassword