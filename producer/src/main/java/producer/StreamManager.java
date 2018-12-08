package producer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Handles creation and removal of tweet-streams and takes care that multiple users with the same query can safely share a single TweetStream
 *
 */
public class StreamManager {

    private HashMap<Query, TweetStream> streams = new HashMap<>();
    private HashMap<Query, Integer> counts = new HashMap<>();
    private HashMap<Query,Long> sheduledRemovals = new HashMap<>();
    private Properties properties;
    private ScheduledThreadPoolExecutor removalTaskSheduler;
    private static long cleanupTaskInterval = 60;
    private long removalTimeout = 5*60;

    static StreamManager instance;

    private StreamManager(Properties properties) {
        this.properties = properties;
        Runnable queryRemovalTask = new Runnable(){

            @Override
            public void run() {
                removeStreams();
            }
        };
        removalTaskSheduler= (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
        removalTaskSheduler.scheduleWithFixedDelay(queryRemovalTask, cleanupTaskInterval, cleanupTaskInterval,TimeUnit.SECONDS);

    }

    public static StreamManager getInstance(Properties properties) {
        if (instance == null) {
            instance = new StreamManager(properties);
        }
        return instance;
    }

    /** Request the creation of a stream for the given query. If no such stream exists one is created, else the existing one is used and the usage-counter is increased
     *
     * @param q
     */
    public synchronized void requestStream(Query q) {
        // if this user has already registered a query
        if (!streams.containsKey(q)) {
            //TODO do not hard-code the topic
            //TODO do something factory-pattern-like here. Decouple stream creation and management. (Let the caller decide how to create streams)
            KafkaAdapter adap = new KafkaAdapter(properties, "tweets");
            TweetStream stream = new TweetStream(properties.getProperty("twitter.consumer"), properties.getProperty("twitter.consumerSecret"), properties.getProperty("twitter.token"), properties.getProperty("twitter.tokenSecret"));
            stream.stream(q, adap);
            streams.put(q, stream);
            counts.put(q, 0);
        }
        counts.put(q, counts.get(q) + 1);
        if (sheduledRemovals.containsKey(q)){
            sheduledRemovals.remove(q);
        }
    }

    /** Signals the StreamManager that one user is not longer interested in the given query.
     * If no user is left for a query the associated stream is sheduled for removal and will finally be removed after a certain duration.
     * If a new user requests a query that is sheduled for removal the removal of the Stream will be aborted and the Stream will be reused.
     *
     * @param q
     */
    public synchronized void releaseStream(Query q) {
        Integer count = counts.get(q);
        if (count == null) {
            throw new NoSuchElementException("This query is not registered for streaming");
        }
        count--;
        if (count == 0) {
            sheduledRemovals.put(q,System.currentTimeMillis()+1000* removalTimeout);
        }
        counts.put(q, count);
    }

    /** Iterates over the streams sheduled for removal and removes the ones that have reached their timeout.
     *
     */
    private synchronized void removeStreams(){
        Iterator<Map.Entry<Query,Long>> it = sheduledRemovals.entrySet().iterator();

        while (it.hasNext())
        {
            Map.Entry<Query,Long> entry = it.next();
            if (entry.getValue() < System.currentTimeMillis()) {
                it.remove();
                TweetStream stream = streams.get(entry.getKey());
                stream.close();
                streams.remove(entry.getKey());
                counts.remove(entry.getKey());
            }
        }
    }

    /** Set the interval in which the removal task should run. Must be called before the first getInstance() call.
     *
     * @param cleanupTaskInterval The interval in seconds
     */
    public static void setCleanupTaskInterval(long cleanupTaskInterval) {
        if (instance != null){
            throw new IllegalStateException("CleanupTaskInterval can only be set before the first call to getInstance()");
        }
        StreamManager.cleanupTaskInterval = cleanupTaskInterval;
    }

    /** Set the timeout after which an unused stream is finally removed
     * @param removalTimeout The timeout in seconds
     */
    public synchronized void setRemovalTimeout(long removalTimeout) {
        this.removalTimeout = removalTimeout;
    }

    /** Returns the number of currently active streams
     */
    public synchronized int activeStreams() {
        return streams.size();
    }

    /** Returns how many users/requests are currently interested in the given query
     */
    public synchronized int currentUsage(Query q){
        return counts.getOrDefault(q,0);
    }
}
