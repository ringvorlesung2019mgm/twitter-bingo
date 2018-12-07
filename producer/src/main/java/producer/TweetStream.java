package producer;

import twitter4j.*;
import twitter4j.auth.AccessToken;


/**
 * Connects to the twitter API and provides a stream of historical and new tweets.
 *
 * @author db
 * @see twitter4j.Twitter
 * @see twitter4j.TwitterStream
 */
public class TweetStream {
    private Twitter twitterClient;
    private TwitterStream twitterStream;

    /**
     * Creates a TweetStream with the given credentials
     *
     * @param consumer       twitter api consumer key
     * @param consumerSecret twitter api consumer secret
     * @param token          twitter api access token
     * @param tokenSecret    twitter api access token secret
     * @see twitter4j.Twitter
     * @see twitter4j.TwitterStream
     */
    public TweetStream(String consumer, String consumerSecret, String token, String tokenSecret) {
        AccessToken at = new AccessToken(token, tokenSecret);

        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.setOAuthConsumer(consumer, consumerSecret);
        twitterStream.setOAuthAccessToken(at);

        twitterClient = new TwitterFactory().getInstance();
        twitterClient.setOAuthConsumer(consumer, consumerSecret);
        twitterClient.setOAuthAccessToken(at);
    }

    /**
     * Start streaming tweets from the API. Should be called only ONCE.
     * Returns after all historical tweets have been collected. New tweets are processed by a background thread.
     *
     * @param query            only receive tweets matching this filter
     * @param listener         forward received tweets to this listener
     * @param histTweetCount count of historical tweets to be collected
     */
    public void stream(Query query, StatusListener listener, int histTweetCount) {
        if (histTweetCount > 0) {
            getHistoricalTweets(query.getT4JQuery(), listener, histTweetCount);
        }
        twitterStream.addListener(listener);
        twitterStream.filter(query.getT4JFilterQuery());
    }

    /**
     * Start streaming tweets from the API. Should be called only ONCE.
     * Returns after all historical tweets have been collected. New tweets are processed by a background thread.
     * Default count of historical tweets from config file is used. Property: tweetStream.defaultHistoricalCount
     *
     * @param query    only receive tweets matching this filter
     * @param listener Forward reiceived tweets to this listener
     */
    public void stream(Query query, StatusListener listener) {
        // TODO use default parameter from config instead of hardcoded value
        stream(query, listener, 100);
    }

    /**
     * Closes the tweet stream. Should be called only ONCE.
     */
    public void close() {
        twitterStream.shutdown();
    }


    /**
     * Collect historical tweets for query.
     *
     * @param q only collect tweets matching this filter
     * @param l forward received tweets to this listener
     * @param count count of historical tweets to be collected
     */
    private void getHistoricalTweets(twitter4j.Query q, StatusListener l, int count) {
        q.setCount(count);
        try {
            QueryResult r = twitterClient.search(q);
            for (Status s : r.getTweets()) {
                l.onStatus(s);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }


}
