from analyzer import *
import socket
import threading
import json
import pytest

def test_read_config():
    teststring = """
    #comment
    abc = 123
    foo = bar

    """
    conf = read_config(teststring.splitlines())
    assert conf["abc"] == "123"
    assert conf["foo"] == "bar"

def test_sslcontext():
    conf = read_config(open("../config.properties"))
    if conf.get("security.protocol") != "SSL":
        pytest.skip("Cannot test SSL-connection because of non-SSL Broker")
    sslctx = create_sslcontext(conf["ssl.keystore.password"])
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout(10)
    host, port = conf["bootstrap.servers"].split(":")
    wrappedSocket = sslctx.wrap_socket(sock,server_hostname=host)
    wrappedSocket.connect((host,int(port)))
    wrappedSocket.do_handshake()
    wrappedSocket.close()

def test_analyzer():
    inp = "test-tweets"
    testvalue = json.dumps({"text": "My-test"}).encode()

    conf = read_config(open("../config.properties"))

    security_protocol = conf.get("security.protocol")

    if security_protocol == "SSL":
        sslctx = create_sslcontext(conf["ssl.keystore.password"])
    else:
        sslctx = None


    producer = KafkaProducer(
    bootstrap_servers=conf["bootstrap.servers"],
    security_protocol=security_protocol,
    ssl_context=sslctx
    )

    dbclient = MongoClient(conf["mongodb"])
    db = dbclient.twitter
    collection = db["test-tweets"]


    stopEvent = threading.Event()
    startEvent = threading.Event()

    analyzerThread = threading.Thread(target=main,args=(inp,"twitter/test-tweets","analyzers","end",stopEvent,startEvent,True))
    analyzerThread.daemon = True
    analyzerThread.start()

    startEvent.wait()

    changestream = collection.watch()

    producer.send(inp,testvalue)
    producer.flush()

    results = []
    def waitfor():
        results.append(changestream.next()["fullDocument"])

    waitingThread = threading.Thread(target=waitfor)
    waitingThread.daemon = True
    waitingThread.start()

    for _ in range(1,10):
        if len(results) > 0:
            break
        time.sleep(1)

    stopEvent.set()

    assert results[0]["text"] == "My-test"
    assert results[0]["rating"] == 0.0