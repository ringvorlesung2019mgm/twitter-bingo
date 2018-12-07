package producer;

import com.google.gson.Gson;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TwingoTweet represents a subset of the data provider by twitter4j.Status,
 * adding the option to rate a tweet and some additional parameters
 * for easier index in mongodb/checking for relevance to search.
 *
 * @author jp
 * @see twitter4j.Status
 */
public class TwingoTweet {

    // twitter4j params
    private long id;
    private String text;
    private String userName;
    private Date createdAt;
    private double geoLat;
    private double geoLon;

    // rating
    private boolean isRated;
    private double rating;

    // easier indexing
    private boolean hasGeo;
    private List<String> hashtags;


    public TwingoTweet(long id, String text, String userName, Date createdAt, boolean hasGeo, double geoLat, double geoLon, boolean isRated, double rating, List<String> hashtags) {
        this.id = id;
        this.text = text;
        this.userName = userName;
        this.createdAt = createdAt;
        this.hasGeo = hasGeo;
        this.geoLat = geoLat;
        this.geoLon = geoLon;
        this.isRated = isRated;
        this.rating = rating;
        this.hashtags = hashtags;
    }

    /**
     * Creates a TwingoTweet from a twitter4j status.
     * Rating is not given at that point ->
     * isRated == false
     * rating == 0
     *
     * @param status {@link twitter4j.Status} of the tweet
     * @return TwingoTweet with subset of data from status and rating set to false/0
     */
    public static TwingoTweet fromStatus(Status status) {
        // if tweet has a geolocation
        List<String> hashtags = toHashtagList(status.getHashtagEntities());
        if (status.getGeoLocation() != null) {
            return new TwingoTweet(status.getId(), status.getText(), status.getUser().getName(), status.getCreatedAt(), true, status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude(), false, 0, hashtags);
        } else {
            return new TwingoTweet(status.getId(), status.getText(), status.getUser().getName(), status.getCreatedAt(), false, 0, 0, false, 0, hashtags);
        }
    }

    /**
     * Creates a list of lowercase strings from an {@link HashtagEntity}[]
     *
     * @param entities entities to be converted
     * @return list of lowercase hashtag strings
     */
    private static List<String> toHashtagList(HashtagEntity[] entities) {
        List<String> hashtags = new ArrayList<String>();
        for (HashtagEntity ht : entities) {
            hashtags.add(ht.getText().toLowerCase());
        }
        return hashtags;
    }

    /**
     * Creates a JSON string from this TwingoTweet object using Gson.
     *
     * @return string representation of this TwingoTweet
     * @see Gson
     */
    public String toJSON() {
        return new Gson().toJson(this);
    }

    /**
     * Creates a TwingoTweet object from a json string using Gson.
     *
     * @param inputJson json to be converted
     * @return TwingoTweet created from inputJson
     * @see Gson
     */
    public static TwingoTweet fromJson(String inputJson) {
        return new Gson().fromJson(inputJson, TwingoTweet.class);
    }

    /**
     * Sets the rating of a tweet.
     *
     * @param rating rating of tweet in range [-1,+1]
     * @throws RatingWasAlreadySetExecption rating can only be set once
     * @deprecated only used for testing
     */
    public void setRating(double rating) throws RatingWasAlreadySetExecption {
        if (!isRated) {
            this.rating = rating;
            this.isRated = true;
        } else {
            throw new RatingWasAlreadySetExecption("Rating was set on an already rated tweet!", this, rating);
        }

    }
}
