package producer;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * Helper class for handling request data sent as JSON from frontend to {@link webapps.api.TweetStreamServlet}.
 * Uses Gson for conversion.
 *
 * @author jp
 * @see Gson
 */
public class TwingoStreamRequest {

    private UUID sessionId;
    private String hashtag;

    private TwingoStreamRequest(String sessionId, String hashtag) {
        this.sessionId = UUID.fromString(sessionId);
        this.hashtag = hashtag;
    }

    /**
     * Creates a TwingoStreamRequest object from a json string using Gson.
     *
     * @param inputJson json to be converted
     * @return TwingoStreamRequest created from inputJson
     * @see Gson
     */
    public static TwingoStreamRequest fromJson(String inputJson) {
        return new Gson().fromJson(inputJson, TwingoStreamRequest.class);
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getHashtag() {
        return hashtag;
    }
}
