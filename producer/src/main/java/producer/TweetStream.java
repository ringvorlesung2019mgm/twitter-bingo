package producer;

import twitter4j.*;
import twitter4j.auth.AccessToken;


/** Connects to the twitter API and provides a stream of historical and new tweets
 *
 */
public class TweetStream {
    private Twitter twitterClient;
    private TwitterStream twitterStream;

    public TweetStream(String consumer, String consumerSecret, String token, String tokenSecret) {
        AccessToken at = new AccessToken(token,tokenSecret);

        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer(consumer,consumerSecret);
        twitterStream.setOAuthAccessToken(at);

        twitterClient = new TwitterFactory().getInstance();
        twitterClient.setOAuthConsumer(consumer,consumerSecret);
        twitterClient.setOAuthAccessToken(at);
    }

    public void stream(Query q, StatusListener l, int historicalTweets){
        if (historicalTweets > 0) {
            getHistoricalTweets(q.getT4JQuery(), l, historicalTweets);
        }
        twitterStream.addListener(l);
        twitterStream.filter(q.getT4JFilterQuery());
    }

    /** Start streaming tweets from the API. Should be called only ONCE.
     * Returns after all historical tweets have been collected. New tweets are processed by a background thread
     *
     * @param q Only receive tweets matching this filter
     * @param l Forward reiceived tweets to this listener
     */
    public void stream(Query q, StatusListener l){
        stream(q,l,100);
    }

    public void close(){
        twitterStream.shutdown();
    }


    private void getHistoricalTweets(twitter4j.Query q, StatusListener l, int count){
        q.setCount(count);
        try {
            QueryResult r = twitterClient.search(q);
            for(Status s : r.getTweets()){
                l.onStatus(s);
            }
        } catch(TwitterException e){
            e.printStackTrace();
        }
    }



}
