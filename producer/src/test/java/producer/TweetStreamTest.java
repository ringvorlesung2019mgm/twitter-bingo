package producer;

import org.junit.Assert;
import org.junit.Test;
import twitter4j.*;

import java.util.Properties;

public class TweetStreamTest {
    int tweetsReceived = 0;


    @Test
    public void testStream(){

        StatusListener sl = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                System.out.println(status.getText());
                System.out.println("-----------------------");
                tweetsReceived++;
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
        };

        PropertyManager pm = new PropertyManager();
        Properties p = pm.generalProperties();
        TweetStream s = new TweetStream(p.getProperty("twitter.consumer"),p.getProperty("twitter.consumerSecret"),p.getProperty("twitter.token"),p.getProperty("twitter.tokenSecret"));
        Query q = new producer.Query("love");
        s.stream(q,sl);
        Assert.assertNotEquals(0,tweetsReceived);
        System.out.printf("Received %d historical tweets\r\n",tweetsReceived);
        tweetsReceived = 0;
        System.out.println("Finished historical tweets. Starting stream.");
        System.out.flush();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        s.close();
        Assert.assertNotEquals(0,tweetsReceived);
        System.out.printf("----------------\r\nRead %d new tweets\r\n",tweetsReceived);

    }
}
