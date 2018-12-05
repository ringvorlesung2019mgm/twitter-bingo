package producer;

import java.util.Date;
import java.util.UUID;

public class UnregisteredTwingoUserException extends Throwable {

    UUID userId;
    Query query;
    Date timeToRemove;

    public UnregisteredTwingoUserException(String message, UUID userId, Query query, Date timeToRemove) {
        super(message);
        this.userId = userId;
        this.query = query;
        this.timeToRemove = timeToRemove;
    }
}
