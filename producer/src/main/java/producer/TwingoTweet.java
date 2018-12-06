package producer;

import com.google.gson.Gson;
import twitter4j.HashtagEntity;
import twitter4j.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TwingoTweet {

    private long id;
    private String text;
    private String userName;
    private Date createdAt;
    private boolean hasGeo;
    private double geoLat;
    private double geoLon;
    private boolean isRated;
    private double rating;
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
     * Creates a twingotweet obj from a twitter4j status. rating is not given at that point, so Double.MAX_VALUE is set.
     *
     * @param status
     * @return
     */
    public static TwingoTweet fromStatus(Status status) {
        List<String> hashtags = new ArrayList<String>();
        for(HashtagEntity ht : status.getHashtagEntities()){
            hashtags.add(ht.getText().toLowerCase());
        }
        if (status.getGeoLocation() != null) {
            return new TwingoTweet(status.getId(), status.getText(), status.getUser().getName(), status.getCreatedAt(), true, status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude(), false, 0,hashtags);
        } else {
            return new TwingoTweet(status.getId(), status.getText(), status.getUser().getName(), status.getCreatedAt(), false, 0, 0, false, 0,hashtags);
        }
    }

    /**
     * Creates a json string from this twingotweet obj
     *
     * @return
     */
    public String toJSON() {
        return new Gson().toJson(this);
    }

    /**
     * Creates a twingotweet obj from the given json string
     *
     * @param json
     * @return
     */
    public static TwingoTweet fromJson(String json) {
        return new Gson().fromJson(json, TwingoTweet.class);
    }

    public void setRating(double rating) throws Exception {
        if (!isRated) {
            this.rating = rating;
            this.isRated = true;
        } else {
            throw new Exception("Rating was set on an already rated tweet!");
        }

    }
}
