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
    inp = "raw-testtopic"
    outp = "analyzed-testtopic"
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

    consumer.subscribe(pattern="^"+outp+".*")

    # Push messages to all needed topics to create them if needed
    producer.send(outp,"Create topic".encode())
    producer.send(inp,"Create topic".encode())
    producer.flush()
    time.sleep(1)

    wait_for_assignment(consumer,"Test waiting for assignment")
    consumer.seek_to_end()
    consumer.poll(0)

    stopEvent = threading.Event()
    startEvent = threading.Event()

    analyzerThread = threading.Thread(target=main,args=(inp,outp,"analyzers","end",stopEvent,startEvent,True))
    analyzerThread.start()

    startEvent.wait()
    producer.send(inp+"-1",testvalue)
    producer.send(inp+"-2",testvalue)
    producer.flush()

    resplist=[]
    resp = consumer.poll(20000)
    resplist.extend(list(resp.values())[0])

    # if poll returned only one result, poll again
    if len(resplist) < 2:
        resp = consumer.poll(20000)
        resplist.extend(list(resp.values())[0])

    stopEvent.set()
    assert resplist[0].value == expected
    assert resplist[1].value == expected