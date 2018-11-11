import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import java.util.Properties;


/** Receives tweets via .onStatus() and pushes all received tweets into kafka
 *
 */
public class KafkaAdapter implements StatusListener {
    private Producer<String,String> producer;
    private String topic;

    /**
     *
     * @param kafkaProperties The properties vor the kafka producer
     * @param topic The topic to which all tweets are pushed
     */
    public KafkaAdapter(Properties kafkaProperties,String topic){
        this.topic = topic;
        producer = new KafkaProducer<>(kafkaProperties);
    }


    @Override
    public void onStatus(Status status) {
        ProducerRecord<String,String> pr = new ProducerRecord<>(topic,status.getText());
        producer.send(pr);
        producer.flush();
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {

    }

    @Override
    public void onStallWarning(StallWarning warning) {

    }

    @Override
    public void onException(Exception ex) {

    }
}
