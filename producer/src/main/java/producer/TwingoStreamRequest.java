package producer;

import com.google.gson.Gson;

import java.util.UUID;

public class TwingoStreamRequest {

    public UUID sessionId;
    public String hashtag;

    public TwingoStreamRequest(String sessionId, String hashtag) {
        this.sessionId = UUID.fromString(sessionId);
        this.hashtag = hashtag;
    }

    public static TwingoStreamRequest fromJson(String json) {
        return new Gson().fromJson(json, TwingoStreamRequest.class);
    }




}
