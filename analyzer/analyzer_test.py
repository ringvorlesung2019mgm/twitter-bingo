from analyzer import *
import socket
import threading

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
    sslctx = create_sslcontext(conf["ssl.keystore.password"])
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout(10)
    host, port = conf["bootstrap.servers"].split(":")
    wrappedSocket = sslctx.wrap_socket(sock,server_hostname=host)
    wrappedSocket.connect((host,int(port)))
    wrappedSocket.do_handshake()
    wrappedSocket.close()

def test_analyzer():
    inp = "analyzer-testtopic"
    inppattern = "^analyzer-testtopic$"
    outp = "analyzer-testtopic-analyzed"
    testvalue = "My-test".encode()
    expected = "My-test : 0.0".encode()

    conf = read_config(open("../config.properties"))
    sslctx = create_sslcontext(conf["ssl.keystore.password"])

    consumer = KafkaConsumer(
    bootstrap_servers=conf["bootstrap.servers"],
    group_id="analyzertest",
    security_protocol="SSL",
    ssl_context=sslctx
    )

    producer = KafkaProducer(
    bootstrap_servers=conf["bootstrap.servers"],
    security_protocol="SSL",
    ssl_context=sslctx
    )

    consumer.subscribe(topics=(outp,))
    producer.send(outp,"Create topic".encode())
    wait_for_assignment(consumer,"Test waiting for assignment")
    consumer.seek_to_end()
    consumer.poll(0)

    stopEvent = threading.Event()
    startEvent = threading.Event()

    analyzerThread = threading.Thread(target=main,args=(inppattern,"-analyzed","analyzers","end",stopEvent,startEvent))
    analyzerThread.start()

    startEvent.wait()
    producer.send(inp,testvalue)
    producer.flush()

    resp = consumer.poll(20000)
    stopEvent.set()
    assert list(resp.values())[0][0].value == expected