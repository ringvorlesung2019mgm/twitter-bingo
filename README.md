# twitter-bingo

[![Build Status](https://travis-ci.com/ringvorlesung2019mgm/twitter-bingo.svg?branch=master)](https://travis-ci.com/ringvorlesung2019mgm/twitter-bingo)

## Configuring the software
Before you can run any part of this software (including the integration-tests) you have to create a config file in the root-directory of this repository containing the following values. 
```
twitter.consumer = **********************
twitter.consumerSecret =  ******************
twitter.token = *****************
twitter.tokenSecret = *****************

bootstrap.servers = yourkafkaserver:9092
ssl.keystore.password = thepasswordforyourcertificates
```

You will also need to create a directory named certificates containing a file kafka.client.keystore.jks (with the certificate+key to connect to kafka) and kafka.client.truststore.jks (with the CA-certifikates needed for the server). Both files need to be in the PKCS12-Format. Python can't use pkcs12-certificates and will therefore automatically create a copy of the certificates as PEM-files in the same directory.

```

After this you can run the tests by going to the producer-directory and running:  
```
./gradlew test
```