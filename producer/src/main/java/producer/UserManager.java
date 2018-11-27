package producer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UserManager {

    private HashMap<String, TwingoUser> userMap;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static UserManager instance;


    private UserManager () {
        userMap = new HashMap<>();
        final Runnable keepAlive = () -> {
            System.out.println(userMap.values());
            for(TwingoUser user : userMap.values()){
                if(user.response.getStatus() != 200){
                    user.setInactive();
                }else {
                    try {
                        user.response.getWriter().write("\r\n");
                        user.response.getWriter().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        scheduler.scheduleAtFixedRate(keepAlive, 0, 500, TimeUnit.MILLISECONDS);
    }

    public static UserManager getInstance(){
        if(UserManager.instance == null){
            UserManager.instance = new UserManager();
        }
        return UserManager.instance;
    }

    public TwingoUser addUser(String userId, String query, HttpServletRequest req, HttpServletResponse resp){
        TwingoUser newTwingoUser = new TwingoUser(userId, new Query(query), req, resp);
        userMap.put(userId, newTwingoUser);
        return newTwingoUser;
    }

    public void removeUser(String userId){
        userMap.remove(userId);
    }

    public int getUserCount(){
        return userMap.values().size();
    }


}