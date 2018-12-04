package producer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class UserManagerTest {

    UserManager userManager;

    @Before
    public void before(){
        PropertyManager propertyManager = new PropertyManager();
        Properties properties = propertyManager.allProperties();
         userManager = UserManager.getInstance(properties);
    }

    @After
    public void after(){
        UserManager.destroyInstance();
    }

    @Test
    public void testAddNewUser(){

        String sessionId = "demoSessionId";
        Query query  = new Query("love");

        userManager.addUser(sessionId, query);

        // check if user was added
        assert userManager.users.containsKey(sessionId);
        assert userManager.streamManager.streams.containsKey(query);
        assert userManager.streamManager.counts.containsKey(query);
        System.out.println(userManager.streamManager.counts.get(query));
        assert userManager.streamManager.counts.get(query) == 1;
    }

    @Test
    public void removeUserWithQueryNotUsed(){
        String sessionId = "demoSessionId";
        Query query  = new Query("love");

        userManager.addUser(sessionId, query);

        assert userManager.users.containsKey(sessionId);
        assert userManager.streamManager.streams.containsKey(query);
        assert userManager.streamManager.counts.containsKey(query);
        assert userManager.streamManager.counts.get(query) == 1;

        userManager.removeUser(sessionId);

        assert !userManager.users.containsKey(sessionId);
        assert !userManager.streamManager.streams.containsKey(query);
        assert !userManager.streamManager.counts.containsKey(query);
    }

    @Test
    public void removeUserQueryNotUsed(){
        String sessionIdUser1 = "demoSessionId1";
        String sessionIdUser2 = "demoSessionId2";
        Query query  = new Query("love");

        userManager.addUser(sessionIdUser1, query);
        userManager.addUser(sessionIdUser2, query);

        assert userManager.users.containsKey(sessionIdUser1);
        assert userManager.users.containsKey(sessionIdUser2);
        assert userManager.streamManager.streams.containsKey(query);
        assert userManager.streamManager.counts.containsKey(query);
        assert userManager.streamManager.counts.get(query) == 2;

        userManager.removeUser(sessionIdUser1);

        assert !userManager.users.containsKey(sessionIdUser1);
        assert userManager.users.containsKey(sessionIdUser2);
        assert userManager.streamManager.streams.containsKey(query);
        assert userManager.streamManager.counts.containsKey(query);
        assert userManager.streamManager.counts.get(query) == 1;
    }

}
