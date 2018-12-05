package producer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    private static SessionManager instance;

    HashMap<UUID, TwingoSession> sessions;
    public StreamManager streamManager;


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable removeInactiveStreams = () -> {
        System.out.println("I just ran");
        try {
            for (Map.Entry<UUID, TwingoSession> user : sessions.entrySet()) {
                System.out.println(user.getValue().getTimeToRemove());
                if (user.getValue().getTimeToRemove().before(new Date())) {
                    if(user.getValue().getQuery() != null){
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

    private SessionManager(StreamManager streamManager) {
        sessions = new HashMap<>();
        scheduler.scheduleAtFixedRate(removeInactiveStreams, 0, 10, TimeUnit.SECONDS);
        this.streamManager = streamManager;
    }

    public static SessionManager getInstance(Properties properties) {
        if (SessionManager.instance == null) {
            SessionManager.instance = new SessionManager(StreamManager.getInstance(properties));
        }
        return SessionManager.instance;
    }

    public static SessionManager getDefaultInstance() {
        if (SessionManager.instance == null) {
            SessionManager.instance = new SessionManager(StreamManager.getDefaultInstance());
        }
        return SessionManager.instance;
    }

    public static void destroyInstance() {
        StreamManager.destroyInstance();
        SessionManager.instance = null;
    }

    /**
     * Generates  a user with the given id/query and the default timeout specified in config
     *
     * @return
     */
    public synchronized TwingoSession generateSession() {
        // TODO move default value to config
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 5);
        return generateSession(calendar.getTime());
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
        // TODO move default value to config
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 5);
        selectQuery(userId, query, calendar.getTime());
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
            if(session.getQuery() != null) {
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


}
