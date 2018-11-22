from kafka import KafkaConsumer
from kafka import KafkaProducer
import ssl
import time
from sentimentanalysis import *
from convertcerts import pkcs12_to_pem

outout_topic_sfx = "-analyzed"
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

def main(input_topic_pattern,outout_topic_sfx=outout_topic_sfx,group_id=group_id,seek=False,stop_event=None):
    conf = read_config(open("../config.properties"))

    sslctx = create_sslcontext(conf["ssl.keystore.password"])

    consumer = KafkaConsumer(
    bootstrap_servers=conf["bootstrap.servers"],
    group_id=group_id,
    security_protocol="SSL",
    ssl_context=sslctx,
    consumer_timeout_ms= 1000
    )

    producer = KafkaProducer(
    bootstrap_servers=conf["bootstrap.servers"],
    security_protocol="SSL",
    ssl_context=sslctx
    )

    consumer.subscribe(pattern=input_topic_pattern)
    wait_for_assignment(consumer,"Analyzer waiting for assignments")

    if seek == "begin":
        consumer.seek_to_beginning()
    if seek == "end":
        consumer.seek_to_end()
        consumer.poll(0)

    while True:
        for msg in consumer:
            tweet = msg.value.decode()
            sentiment = str(get_tweet_sentiment(tweet))
            producer.send(msg.topic+outout_topic_sfx,value=(tweet+" : "+sentiment).encode("UTF-8"))
            print (msg.topic, " : ",tweet," : ",sentiment)
            if stop_event:
                if stop_event.isSet():
                    break
        if stop_event:
            if stop_event.isSet():
                break