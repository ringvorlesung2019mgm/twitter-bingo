package producer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TwingoUser {

    String userId;
    HttpServletResponse response;
    HttpServletRequest request;
    TweetStream tweetStream;
    Boolean isActive;
    Query query;
    KafkaAdapter kafkaAdapter;

    public Boolean isActive() {
        return isActive;
    }

    public TwingoUser(String userId, Query query, HttpServletRequest request, HttpServletResponse response) {
        this.userId = userId;
        this.response = response;
        this.request = request;
        this.query = query;
        this.isActive = true;
    }

    public void setInactive(){
        isActive = false;
    }


}
