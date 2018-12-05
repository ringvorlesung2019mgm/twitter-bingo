package producer;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class TwingoSession {

    public TwingoSession(UUID sessionId, Date timeToRemove) {
        this.sessionId = sessionId;
        this.timeToRemove = timeToRemove;
    }

    private final UUID sessionId;
    private Date timeToRemove;
    private Query query;

    public UUID getSessionId() {
        return sessionId;
    }

    public Date getTimeToRemove() {
        return timeToRemove;
    }

    public Query getQuery() {
        return query;
    }

    public void setTimeToRemove(Date timeToRemove) {
        this.timeToRemove = timeToRemove;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * Add time by calender, depracted
     * @param field
     * @param number
     */
    public void addTimeToUser(int field, int number) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeToRemove);
        calendar.add(field, number);
        timeToRemove = calendar.getTime();
    }

    /**
     * Creates a json string from this twingotweet obj
     *
     * @return
     */
    public String toJSON() {
        return new Gson().toJson(this);
    }

}
