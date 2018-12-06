from kafka import KafkaConsumer
from kafka import KafkaProducer
import ssl
import time
from sentimentanalysis import *
from convertcerts import pkcs12_to_pem
import argparse
import json
from pymongo import MongoClient

group_id = "analyzers" 

"""
Quick and dirty way to read java propertie files as pyjavaproperties seems to be broken.
Will probably break on non-trivial config files.
"""
def read_config(file):
    config = {}
    for line in file:
        if len(line.strip()) > 0 and not line.strip().startswith("#"): 
            key, value = line.split("=")
            key = key.strip()
            value = value.strip()
            config[key] = value

    # Set some default values
    if "security.protocol" not in config:
        config["security.protocol"] = "PLAINTEXT"

    if "mongodb" not in config:
        config["mongodb"] = "mongodb:27017"

    if "bootstrap.servers" not in config:
        config["bootstrap.servers"] = "kafka:9092"


    return config

"""
Assuming the default certificate location and given a password to decrypt them this function sets up and returns an sslcontext.
During this the certificates are converted (copied to the same dir, but with another file-extension) to an appropriate format.
"""
def create_sslcontext(password):
    def pwfunc():
        return password

    #convert pkcs12 certs to pem
    pkcs12_to_pem("../certificates/kafka.client.keystore.jks",password)
    pkcs12_to_pem("../certificates/kafka.client.truststore.jks",password)

    ctx = ssl.create_default_context()
    ctx.load_verify_locations(cafile="../certificates/kafka.client.truststore.pem")
    ctx.load_cert_chain(certfile="../certificates/kafka.client.keystore.pem",password=pwfunc)
    return ctx

"""
Wait until the assignment of partitions to the consumer is completed.
If message is not None it will be printed every second while waiting
"""
def wait_for_assignment(consumer,message=None):
    consumer.poll(0)
    while not consumer.assignment():
        if message:
            print(message)
        time.sleep(1)

def main(input_topic,db,group_id=group_id,seek=False,stop_event=None,start_event=None,debug=False):
    conf = read_config(open("../config.properties"))

    security_protocol = conf.get("security.protocol")

    if security_protocol == "SSL":
        sslctx = create_sslcontext(conf["ssl.keystore.password"])
    else:
        sslctx = None

    consumer = KafkaConsumer(
    bootstrap_servers=conf["bootstrap.servers"],
    group_id=group_id,
    security_protocol=security_protocol,
    ssl_context=sslctx,
    consumer_timeout_ms= 1000
    )


    db_name, collection_name = db.split("/")

    dbclient = MongoClient(conf["mongodb"])
    tweetcollection = dbclient[db_name][collection_name]


    consumer.subscribe(topics=(input_topic,))
    wait_for_assignment(consumer,"Analyzer waiting for assignments")

    if seek == "begin":
        consumer.seek_to_beginning()
    if seek == "end":
        consumer.seek_to_end()
        consumer.poll(0)

    if start_event:
        start_event.set()

    while True:
        for msg in consumer:
            try:
                tweet = json.loads(msg.value.decode())
            except json.JSONDecodeError:
                if debug:
                    print("Warning:",msg.value.decode(),"is no valid json")
                continue
            sentiment = get_tweet_sentiment(tweet["text"])
            tweet["rating"]=sentiment
            tweet["isRated"]=True
            tweetcollection.insert_one(tweet)
            if debug:
                print ("From:",msg.topic," : ",tweet)
            if stop_event:
                if stop_event.isSet():
                    break
        if stop_event:
            if stop_event.isSet():
                break

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Analyze messages from kafka')
    parser.add_argument('--input_topic', type=str, help='The kafka topic to load raw tweets from', default="tweets")
    parser.add_argument('--db', type=str, help='Name of the database and collection in which to store analyzed tweets', default="twitter/tweets")
    parser.add_argument('--group_id', type=str, help='The if of the consumer-group this consumer belongs to', default=group_id)
    parser.add_argument('--seek', type=str, help='Seek to "begin", "end" or "none" when starting up.', default="none")
    parser.add_argument('--debug', type=bool, help='Print debug infos (like analyzed messages)', default=False)
    
    args = parser.parse_args()
    print(args)

    print("Starting analyser...")
    main(args.input_topic,
         args.db,
         group_id=args.group_id,
         seek=args.seek,
         debug=args.debug)
