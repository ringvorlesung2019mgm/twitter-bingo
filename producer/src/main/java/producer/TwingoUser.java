package producer;

import java.util.Calendar;
import java.util.Date;

public class TwingoUser {

    public TwingoUser(String sessionId, Query query, Date timeToRemove) {
        this.sessionId = sessionId;
        this.timeToRemove = timeToRemove;
        this.query = query;
    }

    String sessionId;
        Date timeToRemove;
        Query query;

    public void addTimeToUser(int field, int number){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timeToRemove);
        calendar.add(field, number);
        timeToRemove = calendar.getTime();
    }

}
