package producer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;
import twitter4j.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import static java.lang.System.out;

public class KafkaAdapterTest {

    private static final String TESTTOPIC = "my-topic";

    @Test
    public void testPubSubTweet(){
        Properties props = new PropertyManager().producerProperties();
        KafkaAdapter ad = new KafkaAdapter(props,TESTTOPIC);

        Status s = new Status() {
            @Override
            public Date getCreatedAt() {
                return null;
            }

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public String getText() {
                return "This is a test tweet";
            }

            @Override
            public String getSource() {
                return null;
            }

            @Override
            public boolean isTruncated() {
                return false;
            }

            @Override
            public long getInReplyToStatusId() {
                return 0;
            }

            @Override
            public long getInReplyToUserId() {
                return 0;
            }

            @Override
            public String getInReplyToScreenName() {
                return null;
            }

            @Override
            public GeoLocation getGeoLocation() {
                return null;
            }

            @Override
            public Place getPlace() {
                return null;
            }

            @Override
            public boolean isFavorited() {
                return false;
            }

            @Override
            public boolean isRetweeted() {
                return false;
            }

            @Override
            public int getFavoriteCount() {
                return 0;
            }

            @Override
            public User getUser() {
                return null;
            }

            @Override
            public boolean isRetweet() {
                return false;
            }

            @Override
            public Status getRetweetedStatus() {
                return null;
            }

            @Override
            public long[] getContributors() {
                return new long[0];
            }

            @Override
            public int getRetweetCount() {
                return 0;
            }

            @Override
            public boolean isRetweetedByMe() {
                return false;
            }

            @Override
            public long getCurrentUserRetweetId() {
                return 0;
            }

            @Override
            public boolean isPossiblySensitive() {
                return false;
            }

            @Override
            public String getLang() {
                return null;
            }

            @Override
            public Scopes getScopes() {
                return null;
            }

            @Override
            public String[] getWithheldInCountries() {
                return new String[0];
            }

            @Override
            public long getQuotedStatusId() {
                return 0;
            }

            @Override
            public Status getQuotedStatus() {
                return null;
            }

            @Override
            public int compareTo(Status status) {
                return 0;
            }

            @Override
            public UserMentionEntity[] getUserMentionEntities() {
                return new UserMentionEntity[0];
            }

            @Override
            public URLEntity[] getURLEntities() {
                return new URLEntity[0];
            }

            @Override
            public HashtagEntity[] getHashtagEntities() {
                return new HashtagEntity[0];
            }

            @Override
            public MediaEntity[] getMediaEntities() {
                return new MediaEntity[0];
            }

            @Override
            public ExtendedMediaEntity[] getExtendedMediaEntities() {
                return new ExtendedMediaEntity[0];
            }

            @Override
            public SymbolEntity[] getSymbolEntities() {
                return new SymbolEntity[0];
            }

            @Override
            public RateLimitStatus getRateLimitStatus() {
                return null;
            }

            @Override
            public int getAccessLevel() {
                return 0;
            }
        };

        KafkaConsumer<String,String> cons = new KafkaConsumer<>(new PropertyManager().consumerProperties());

        cons.subscribe(Collections.singletonList(TESTTOPIC));
        waitForAssignments(cons,10000);
        cons.seekToEnd(cons.assignment());
        //seek is lazy. Needs to be followed by poll or position to actually do anything
        cons.position((TopicPartition)cons.assignment().toArray()[0]);


        ad.onStatus(s);

        ConsumerRecords<String, String> consumerRecords = cons.poll(Duration.ofSeconds(10L));
        Assert.assertNotEquals(0,consumerRecords.count());
        for(ConsumerRecord<String,String> r : consumerRecords){
            out.println(r.value());
        }

    }

    /** Wait until the list of assignments for the consumer is not longer empty
     *
     * @param cons The consumer
     * @param timeout Timeout in milliseconds after which to give up
     */
    private void waitForAssignments(KafkaConsumer cons, long timeout){
        long waited = 0;
        while(waited < timeout){
            cons.poll(Duration.ofMillis(100));
            if (cons.assignment().size() > 0){
                return;
            }
            waited++;
        }
        throw new IllegalStateException("Consumer did not get assignments in time");
    }
}
