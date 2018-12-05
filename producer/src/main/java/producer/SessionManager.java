package producer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    private static SessionManager instance;

    HashMap<UUID, TwingoSession> sessions;
    public StreamManager streamManager;
    Properties sessionManagerProperties;


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable removeInactiveStreams = () -> {
        try {
            for (Map.Entry<UUID, TwingoSession> user : sessions.entrySet()) {
                System.out.println(user.getValue().getTimeToRemove());
                if (user.getValue().getTimeToRemove().before(new Date())) {
                    if (user.getValue().getQuery() != null) {
                        streamManager.removeStream(user.getValue().getQuery());
                    }
                    sessions.remove(user.getKey());
                    System.out.println(user.getKey() + " was removed!");
                }
            }
        } catch (Exception r) {
            System.out.println(r);
        }
    };

    private SessionManager(Properties allProperties, Properties sessionManagerProperties) {
        sessions = new HashMap<>();
        this.sessionManagerProperties = sessionManagerProperties;
        scheduler.scheduleAtFixedRate(removeInactiveStreams,
                Integer.parseInt(this.sessionManagerProperties.getProperty("removeInactive.initalDelay")),
                Integer.parseInt(this.sessionManagerProperties.getProperty("removeInactive.period")),
                TimeUnit.valueOf(this.sessionManagerProperties.getProperty("removeInactive.TimeUnit")));
        this.streamManager = StreamManager.getInstance(allProperties);

    }

    public static SessionManager getInstance(Properties allProperties, Properties sessionManagerProperties) {
        if (SessionManager.instance == null) {
            SessionManager.instance = new SessionManager(allProperties, sessionManagerProperties);
        }
        return SessionManager.instance;
    }

    public static void destroyInstance() {
        StreamManager.destroyInstance();
        SessionManager.instance = null;
    }

    private Date getDefaultTimeOut() {
        String calUni_String = this.sessionManagerProperties.getProperty("def.CalendarUnit");
        String calAmount_String = this.sessionManagerProperties.getProperty("def.amount");
        int calUni = Integer.parseInt(calUni_String);
        int calAmount = Integer.parseInt(calAmount_String);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(calUni, calAmount);
        return calendar.getTime();
    }

    /**
     * Generates  a user with the given id/query and the default timeout specified in config
     *
     * @return
     */
    public synchronized TwingoSession generateSession() {
        // TODO move default value to config
        return generateSession(getDefaultTimeOut());
    }

    /**
     * Generates a user with the given timeout
     *
     * @param timeToRemove
     * @return
     */
    public synchronized TwingoSession generateSession(Date timeToRemove) {

        UUID newSessionId = UUID.randomUUID();
        TwingoSession newSession = new TwingoSession(newSessionId, timeToRemove);
        sessions.put(newSessionId, newSession);

        System.out.println(newSession + " was created");
        return newSession;
    }

    /**
     * Selects  a user with the given id/query and the default timeout specified in config
     *
     * @param userId
     * @param query
     */
    public synchronized void selectQuery(UUID userId, Query query) throws UnregisteredTwingoUserException {
        selectQuery(userId, query, getDefaultTimeOut());
    }

    /**
     * Adds a user with given params
     *
     * @param userId
     * @param query
     * @param timeToRemove
     */
    public synchronized void selectQuery(UUID userId, Query query, Date timeToRemove) throws UnregisteredTwingoUserException {
        // if user is already registred
        if (sessions.keySet().contains(userId)) {
            TwingoSession session = sessions.get(userId);
            // if query is not equivalent to previous query
            if (session.getQuery() != null && !session.getQuery().equals(query)) {
                streamManager.removeStream(sessions.get(userId).getQuery());
            }
            session.setTimeToRemove(timeToRemove);
            session.setQuery(query);
            System.out.println(userId + " selected " + query.toString());
            streamManager.addStream(query);
        } else {
            throw new UnregisteredTwingoUserException("Unregistered User", userId, query, timeToRemove);
        }
    }

    public synchronized void removeSession(UUID sessionId) {
        if (sessions.containsKey(sessionId)) {
            TwingoSession session = sessions.get(sessionId);
            if (session.getQuery() != null) {
                streamManager.removeStream(sessions.get(sessionId).getQuery());
            }
            sessions.remove(sessionId);
        }

    }

    public synchronized void scheduleRemoveSession(UUID sessionId, Date timeToRemove) {
        if (sessions.containsKey(sessionId)) {
            sessions.get(sessionId).setTimeToRemove(timeToRemove);
        }
    }

    public synchronized void scheduleRemoveSessionDefaultTimeout(UUID sessionId) {
        scheduleRemoveSession(sessionId, getDefaultTimeOut());
    }


}
