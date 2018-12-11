from kafka import KafkaConsumer, errors
from kafka import KafkaProducer
import ssl
import time
import sys
from sentimentanalysis import get_tweet_sentiment
from convertcerts import pkcs12_to_pem
import argparse
import json
from pymongo import MongoClient
from pymongo.errors import DuplicateKeyError, NotMasterError
from pymongo import IndexModel, ASCENDING
import datetime

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

    # convert pkcs12 certs to pem
    pkcs12_to_pem("../certificates/kafka.client.keystore.jks", password)
    pkcs12_to_pem("../certificates/kafka.client.truststore.jks", password)

    ctx = ssl.create_default_context()
    ctx.load_verify_locations(
        cafile="../certificates/kafka.client.truststore.pem")
    ctx.load_cert_chain(
        certfile="../certificates/kafka.client.keystore.pem", password=pwfunc)
    return ctx


"""
Subscribe a list of topics and wait until the assignment of partitions to the consumer is completed.
If message is not None it will be printed every second while waiting
After some unsuccessfull waits for assignments the subscription is retired
"""


def subscribe_and_wait_for_assignment(consumer, topics, message=None):
    count = 0
    consumer.subscribe(topics=topics)
    consumer.poll(0)
    while not consumer.assignment():
        if message:
            print(message)
        time.sleep(1)
        count += 1
        if count == 10:
            if message:
                print("Assignment still not received. Renewing subscription")
            consumer.subscribe(topics=topics)
            consumer.poll(0)
            count = 0


""""
Given a mongodb collection this function creates indexes that will be usefull for later queries
"""


def create_indexes(collection):
    while True:
        try:
            id = IndexModel([("id", ASCENDING)], unique=True)
            hashtags = IndexModel([("hashtags", ASCENDING)])
            created = IndexModel([("created", ASCENDING)])
            collection.create_indexes([id, hashtags, created])
        except NotMasterError:
            print("Waiting for mongodb-server to become primary...")
            time.sleep(1)
            continue
        break


def main(input_topic, db, group_id=group_id, seek=False, stop_event=None, start_event=None, debug=False):
    conf = read_config(open("../config.properties"))

    security_protocol = conf.get("security.protocol")

    if security_protocol == "SSL":
        sslctx = create_sslcontext(conf["ssl.keystore.password"])
    else:
        sslctx = None

    consumer = None
    retrycount = 0
    while consumer == None:
        try:
            consumer = KafkaConsumer(
                bootstrap_servers=conf["bootstrap.servers"],
                group_id=group_id,
                security_protocol=security_protocol,
                ssl_context=sslctx,
                consumer_timeout_ms=1000
            )
        except errors.NoBrokersAvailable:
            print("Kafka unreachable. Retrying...")
            retrycount += 1
            if retrycount > 10:
                print("Kafka unreachable. Giving up. \r\nMake sure you have set the correct address in the config-file AND the compose-file!")
                sys.exit(1)
            time.sleep(3)
    print("Connected to kafka!")

    db_name, collection_name = db.split("/")

    dbclient = MongoClient(conf["mongodb"])

    tweetcollection = dbclient[db_name][collection_name]

    create_indexes(tweetcollection)

    subscribe_and_wait_for_assignment(
        consumer, (input_topic,), "Analyzer waiting for assignments")

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
                    print("Warning:", msg.value.decode(), "is no valid json")
                continue

            tweet["isRated"] = True
            tweet["createdAt"] = datetime.datetime.strptime(
                tweet["createdAt"], "%b %d, %Y %I:%M:%S %p")

            sentiment = get_tweet_sentiment(tweet["text"])

            if sentiment != None:
                tweet["rating"] = sentiment
                tweet["isRated"] = True
            else:
                tweet["rating"] = 0.0
                tweet["isRated"] = False
                if debug:
                    print("Warning: Could not detect language of:", tweet[id])

            try:
                tweetcollection.insert_one(tweet)
            except DuplicateKeyError:
                # There is already a tweet with this id in the db. Nothing left to do, go on.
                if debug:
                    print("Ignoring duplicate:", tweet["id"])
                pass

            if debug:
                print("From:", msg.topic, " : ", tweet)
            if stop_event:
                if stop_event.isSet():
                    break
        if stop_event:
            if stop_event.isSet():
                break


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Analyze messages from kafka')
    parser.add_argument('--input_topic', type=str,
                        help='The kafka topic to load raw tweets from', default="tweets")
    parser.add_argument(
        '--db', type=str, help='Name of the database and collection in which to store analyzed tweets', default="twitter/tweets")
    parser.add_argument('--group_id', type=str,
                        help='The if of the consumer-group this consumer belongs to', default=group_id)
    parser.add_argument(
        '--seek', type=str, help='Seek to "begin", "end" or "none" when starting up.', default="none")
    parser.add_argument(
        '--debug', type=bool, help='Print debug infos (like analyzed messages)', default=False)

    args = parser.parse_args()
    print(args)

    print("Starting analyser...")
    main(args.input_topic,
         args.db,
         group_id=args.group_id,
         seek=args.seek,
         debug=args.debug)
