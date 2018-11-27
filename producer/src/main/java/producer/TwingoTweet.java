package producer;

import com.google.gson.Gson;
import twitter4j.Status;

import java.util.Date;

public class TwingoTweet {

    private long id;
    private String text;
    private String userName;
    private Date createdAt;
    private double geoLat;
    private double geoLon;
    private double rating;

    private TwingoTweet(long id, String text, String userName, Date createdAt, double geoLat, double geoLon, double rating) {
        this.id = id;
        this.text = text;
        this.userName = userName;
        this.createdAt = createdAt;
        this.geoLat = geoLat;
        this.geoLon = geoLon;
        this.rating = rating;
    }

    /**
     * Creates a twingotweet obj from a twitter4j status. rating is not given at that point, so Double.MAX_VALUE is set.
     * @param status
     * @return
     */
    public static TwingoTweet fromStatus(Status status){
        return new TwingoTweet(status.getId(),status.getText(), status.getUser().getName(), status.getCreatedAt(), status.getGeoLocation().getLatitude(), status.getGeoLocation().getLongitude(), Double.MAX_VALUE);
    }

    /**
     * Creates a json string from this twingotweet obj
     * @return
     */
    public String toJSON(){
        return new Gson().toJson(this);
    }

    /**
     * Creates a twingotweet obj from the given json string
     * @param json
     * @return
     */
    public static TwingoTweet fromJson(String json){
        return new Gson().fromJson(json, TwingoTweet.class);
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getUserName() {
        return userName;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public double getGeoLat() {
        return geoLat;
    }

    public double getGeoLon() {
        return geoLon;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) throws Exception {
        if(rating == Double.MAX_VALUE){
            this.rating = rating;
        }else{
            throw new Exception("Rating was set on an already rated tweet!");
        }

    }
}
