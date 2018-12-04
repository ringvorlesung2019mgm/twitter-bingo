# twitter-bingo

[![Build Status](https://travis-ci.com/ringvorlesung2019mgm/twitter-bingo.svg?branch=master)](https://travis-ci.com/ringvorlesung2019mgm/twitter-bingo)

## Configuring the software
Before you can run any part of this software (including the integration-tests) you have to create a config file in the root-directory of this repository containing the following values. 
```
twitter.consumer = **********************
twitter.consumerSecret =  ******************
twitter.token = *****************
twitter.tokenSecret = *****************

mongodb = mongodb://localhost:27017/

bootstrap.servers = localhost:9092
# The two lines below should only be there if your Kafka is using SSL. Otherwise DELETE THEM!!!
security.protocoll = SSL
ssl.keystore.password = thepasswordforyourcertificates
```

You will also need to create a directory named certificates containing a file kafka.client.keystore.jks (with the certificate+key to connect to kafka) and kafka.client.truststore.jks (with the CA-certifikates needed for the server). Both files need to be in the PKCS12-Format. Python can't use pkcs12-certificates and will therefore automatically create a copy of the certificates as PEM-files in the same directory.

## Required Services
To use this software you will need to hav a running kafka-server and a running mongodb-server. You can get both using the docker-compose file in this repo:

```
sudo docker-comppose build
sudo docker-compose up kafka mongodb
```

## Running tests

```
cd producer && ./gradlew test
cd ..
cd analyzer && pytest
cd ..
```

## Running the software
Analyzer:
```
cd analyzer && python3 analyzer.py
```

Producer+Webapp:
```
cd producer && ./gradlew appRun
```

## Docker
You can also build and run the whole software-stack using docker and docker-compose.
First you need to build the docker-images from the project sources:
```
docker-compose build
```
After this place certificates and config files like you would do when running normally. Then just run:
```
docker-compose up
```
This will start one producer/webserver and one analyzer. You can now visit the webapp on http://localhost:8080/producer