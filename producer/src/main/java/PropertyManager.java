import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** Handles all configuration-related stuff.
 *
 */
class PropertyManager {

    private static final String private_key_passphrase = "3gQFJ97htSDFV12irydfasgdf34tmlioOUw";
    private static final String server_public_ip = "18.194.145.94";

    private static final String CONFIG_PATH=Paths.get("../config.properties").toAbsolutePath().normalize().toString();

    private Properties userSettings = new Properties();

    public PropertyManager(String configfile){
        try {
            FileInputStream fi = new FileInputStream(configfile);
            userSettings = new Properties();
            userSettings.load(fi);
            fi.close();

        }catch(FileNotFoundException e){
            System.out.printf("No config-file found in %s. Using defaults.",CONFIG_PATH);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public PropertyManager(){
        this(CONFIG_PATH);
    }

    Properties generalProperties(){
        Properties p = new Properties();
        p.putAll(userSettings);
        return p;
    }

    Properties consumerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", server_public_ip + ":9092");
        props.put("ssl.enabled.protocols", "TLSv1.1,TLSv1.2");
        props.put("ssl.protocol", "TLSv1.2");
        props.put("group.id", "test123");
        appendDeserialiser(props);
        appendClientKeystore(props);
        props.putAll(userSettings);
        return props;
    }

    Properties producerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", server_public_ip + ":9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("ssl.enabled.protocols", "TLSv1.1,TLSv1.2");
        props.put("ssl.protocol", "TLSv1.2");
        appendSerializer(props);
        appendClientKeystore(props);
        props.putAll(userSettings);
        return props;
    }

    private void appendSerializer(Properties props) {
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    private void appendDeserialiser(Properties props) {
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    }

    private void appendClientKeystore(Properties props) {
        Path myKeyHomeDir = Paths.get("../certificates").toAbsolutePath();

        //configure the following three settings for SSL Encryption
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, myKeyHomeDir.resolve("kafka.client.keystore.jks").toString());
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, private_key_passphrase);

        // configure the following three settings for SSL Authentication
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, myKeyHomeDir.resolve("kafka.client.keystore.jks").toString());
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, private_key_passphrase);
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, private_key_passphrase);
    }

}