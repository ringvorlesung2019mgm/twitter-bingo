package producer;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Properties;

public class StreamManager {

    HashMap<Query, TweetStream> streams = new HashMap<>();
    HashMap<Query, Integer> counts = new HashMap<>();
    Properties properties;

    static StreamManager instance;

    private StreamManager(Properties properties) {
        this.properties = properties;
    }

    public static StreamManager getInstance(Properties properties) {
        if (instance == null) {
            instance = new StreamManager(properties);
        } else {
            // TODO maybe refactor this to factory pattern, not sure if it makes sense
            // instance.properties = properties;
        }
        return instance;
    }

    // HACKY SOLUTION, definitly refactor to factory pattern
    public static StreamManager getDefaultInstance(){
        PropertyManager pm = new PropertyManager();
        if(instance == null){
            instance = new StreamManager(pm.allProperties());
        }
        return instance;
    }

    public static void destroyInstance(){
        StreamManager.instance = null;
    }

    public synchronized void addStream(Query q) {
        // if this user has already registered a query
        if (!streams.containsKey(q)) {
            KafkaAdapter adap = new KafkaAdapter(properties, topicFromQuery(q));
            TweetStream stream = new TweetStream(properties.getProperty("twitter.consumer"), properties.getProperty("twitter.consumerSecret"), properties.getProperty("twitter.token"), properties.getProperty("twitter.tokenSecret"));
            stream.stream(q, adap);
            streams.put(q, stream);
            counts.put(q, 0);
        }
        counts.put(q, counts.get(q) + 1);
    }

    public synchronized void removeStream(Query q) {
        Integer count = counts.get(q);
        if (count == null) {
            throw new NoSuchElementException("This query is not registered for streaming");
        }
        count--;
        if (count == 0) {
            TweetStream stream = streams.get(q);
            stream.close();
            streams.remove(q);
            counts.remove(q);
        } else {
            counts.put(q, count);
        }
    }

    public synchronized int activeQueries() {
        return streams.size();
    }

    public String topicFromQuery(Query q) {
        return "uni_" + q.toString();
    }
}
