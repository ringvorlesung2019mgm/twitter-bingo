import java.util.Properties;

public class Main {
    public static void main(String[] args){
        PropertyManager pm = new PropertyManager();
        Properties p = pm.generalProperties();
        TweetStream s = new TweetStream(p.getProperty("twitter.consumer"),p.getProperty("twitter.consumerSecret"),p.getProperty("twitter.token"),p.getProperty("twitter.tokenSecret"));
        KafkaAdapter adap = new KafkaAdapter(new PropertyManager().producerProperties(),"my-topic");
        Query q = new Query("love");
        s.stream(q,adap);
        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("finished");
            }
        }

    }
}
