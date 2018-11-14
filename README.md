# twitter-bingo

## Running (or testing) the producer
To run the producer (or the tests for the producer) you will need to place the certificates for kafka inside a directory called /certificates in the root of this repository.  
Additionally you will need to create a file called config.properties in the root directory of this repository containing you twitter API-keys (and any additional kafka-related configuration) in the following format:  
```
twitter.consumer = **********************
twitter.consumerSecret =  ******************
twitter.token = *****************
twitter.tokenSecret = *****************
```

After this you can run the tests by going to the producer-directory and running:  
```
./gradlew test
```