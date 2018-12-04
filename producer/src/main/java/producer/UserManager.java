package producer;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static UserManager instance;
    HashMap<String, TwingoUser> users;
    Properties properties;
    public StreamManager streamManager;

    Runnable removeInactiveStreams = () -> {
        for(Map.Entry<String, TwingoUser> user: users.entrySet()){
            if(user.getValue().timeToRemove.before(new Date())){
                streamManager.removeStream(user.getValue().query);
            }
        }
    };

    private UserManager (Properties properties) {
        users = new HashMap<>();
        scheduler.scheduleAtFixedRate(removeInactiveStreams, 0, 5, TimeUnit.SECONDS);
        this.properties = properties;
        streamManager = StreamManager.getInstance(properties);
    }

    public static UserManager getInstance(Properties properties){
        if (UserManager.instance == null) {
            UserManager.instance = new UserManager (properties);
        }
        return UserManager.instance;
    }

    public static void destroyInstance(){
        StreamManager.destroyInstance();
        UserManager.instance = null;
    }

    /**
     * Adds a user with the given id/query and the default timeout specified in config
     * @param sessionId
     * @param query
     */
    public synchronized void addUser(String sessionId, Query query){
        // TODO move default value to config
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 5);
        addUser(sessionId, query, calendar.getTime());
    }

    /**
     * Adds a user with given params
     * @param sessionId
     * @param query
     * @param timeToRemove
     */
    public synchronized void addUser(String sessionId, Query query, Date timeToRemove){
        if(users.keySet().contains(sessionId)){
            TwingoUser user = users.get(sessionId);
            if(!user.query.equals(query)){
                streamManager.removeStream(users.get(sessionId).query);
                streamManager.addStream(query);
            }

        }
        users.put(sessionId, new TwingoUser(sessionId, query, timeToRemove));
        streamManager.addStream(query);
    }

    public synchronized void removeUser(String sessionId){
        if(users.containsKey(sessionId)){
            streamManager.removeStream(users.get(sessionId).query);
        }
        users.remove(sessionId);
    }

    public synchronized void scheduleRemoveUser(String sessionId, Date timeToRemove){
        if(users.containsKey(sessionId)){
            users.get(sessionId).timeToRemove = timeToRemove;
        }
    }



}
