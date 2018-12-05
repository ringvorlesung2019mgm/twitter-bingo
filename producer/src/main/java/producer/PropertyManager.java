package producer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Handles all configuration-related stuff.
 *
 */
public class PropertyManager {

    private static final String CONFIG_PATH=Paths.get("../config.properties").toAbsolutePath().normalize().toString();

    private Properties userSettings = new Properties();

    public PropertyManager(String configfile){
        try {
            FileInputStream fi = new FileInputStream(configfile);
            userSettings = new Properties();
            userSettings.load(fi);
            fillOptionalSettings(userSettings);
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

    /*** Populate some empty optional config-fields with default-values derived from other user-provided options
     *
     * @param p
     */
    private void fillOptionalSettings(Properties p){
        if(p.containsKey("ssl.keystore.password")){
            if(!p.containsKey("ssl.truststore.password")){
                p.put("ssl.truststore.password",p.get("ssl.keystore.password"));
            }
            if(!p.containsKey("ssl.key.password")){
                p.put("ssl.key.password",p.get("ssl.keystore.password"));
            }
        }
    }

    public Properties allProperties(){
        Properties p = new Properties();
        p.putAll(generalProperties());
        p.putAll(consumerProperties());
        p.putAll(producerProperties());
        return p;
    }

    public Properties generalProperties(){
        Properties p = new Properties();
        p.putAll(userSettings);
        return p;
    }

    public Properties consumerProperties() {
        Properties props = new Properties();
        props.put("ssl.enabled.protocols", "TLSv1.1,TLSv1.2");
        props.put("ssl.protocol", "TLSv1.2");
        props.put("group.id", "test123");
        appendDeserialiser(props);
        appendClientKeystore(props);
        props.putAll(userSettings);
        return props;
    }

    public Properties producerProperties() {
        Properties props = new Properties();
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

    public Properties sessionManagerProperties() {
        Properties props = new Properties();
        props.put("removeInactive.initalDelay", "0");
        props.put("removeInactive.period", "15");
        props.put("removeInactive.TimeUnit", TimeUnit.SECONDS.toString());
        props.put("def.amount", "5");
        props.put("def.CalendarUnit", Integer.toString(Calendar.MINUTE));
        props.putAll(userSettings);
        return props;
    }

    public Properties JUNITsessionManagerProperties(){
        Properties props = new Properties();
        props.put("removeInactive.initalDelay", "0");
        props.put("removeInactive.period", "1");
        props.put("removeInactive.TimeUnit", TimeUnit.SECONDS.toString());
        props.put("def.amount", "1");
        props.put("def.CalendarUnit", Integer.toString(Calendar.MINUTE));
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
        //props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, private_key_passphrase);

        // configure the following three settings for SSL Authentication
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, myKeyHomeDir.resolve("kafka.client.keystore.jks").toString());
        //props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, private_key_passphrase);
        //props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, private_key_passphrase);
    }

}