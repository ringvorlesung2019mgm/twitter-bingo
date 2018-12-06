# twitter-bingo

[![Build Status](https://travis-ci.com/ringvorlesung2019mgm/twitter-bingo.svg?branch=master)](https://travis-ci.com/ringvorlesung2019mgm/twitter-bingo)

## Configuring the software
Before you can run any part of this software (including the integration-tests) you have to create a config file in the root-directory of this repository containing the following values. 
```
twitter.consumer = **********************
twitter.consumerSecret =  ******************
twitter.token = *****************
twitter.tokenSecret = *****************

# optional if you want to use different kafka or mongodb servers
mongodb = yourmongo:27017
bootstrap.servers = yourkafka:9092

# The two lines below should only be there if your Kafka is using SSL. Otherwise DELETE THEM!!!
security.protocol = SSL
ssl.keystore.password = thepasswordforyourcertificates
```

If your kafka uses certificate auth (the kafka in the docker-compose environment does not) you will also need to create a directory named certificates containing a file kafka.client.keystore.jks (with the certificate+key to connect to kafka) and kafka.client.truststore.jks (with the CA-certifikates needed for the server). Both files need to be in the PKCS12-Format. Python can't use pkcs12-certificates and will therefore automatically create a copy of the certificates as PEM-files in the same directory.

## Running
First you need to build the docker-images from the project sources:
```
sudo docker-compose build
```
Now start the software stack:
```
sudo docker-compose up
```
This will start one producer/webserver, one analyzer one kafka and one mongodb-server. You can now visit the webapp on http://localhost:8080/producer


## Running/Testing outside of docker-containers

- In the docker-compose.yml replace ```KAFKA_ADVERTISED_HOST_NAME: kafka``` with ```KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1```

- Start mongodb and kafka

```
sudo docker-comppose build
sudo docker-compose up kafka mongodb
```
- Insert the following lines in your config:
```
mongodb = 127.0.0.1:27017
bootstrap.servers = 127.0.0.1:9092
```

### Running tests

```
cd producer && ./gradlew test
cd ..
cd analyzer && pytest
cd ..
```

### Running the software
Analyzer:
```
cd analyzer && python3 analyzer.py
```

Producer+Webapp:
```
cd producer && ./gradlew appRun
```