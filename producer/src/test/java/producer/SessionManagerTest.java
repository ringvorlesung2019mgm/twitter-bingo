package producer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class SessionManagerTest {

    SessionManager sessionManager;

    @Before
    public void before(){
        PropertyManager propertyManager = new PropertyManager();
        Properties properties = propertyManager.allProperties();
         sessionManager = SessionManager.getInstance(properties);
    }

    @After
    public void after(){
        SessionManager.destroyInstance();
    }

    @Test
    public void testGenerateSession(){
        TwingoSession newSession = sessionManager.generateSession();
        assert sessionManager.sessions.containsKey(newSession.getSessionId());
    }

    @Test
    public void testAutomaticEmptySessionRemoval() throws InterruptedException {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MILLISECOND, 50);

        TwingoSession newSession = sessionManager.generateSession(calendar.getTime());

        assert sessionManager.sessions.containsKey(newSession.getSessionId());

        Thread.sleep(10000);

        assert !sessionManager.sessions.containsKey(newSession.getSessionId());
    }

    @Test
    public void testManualEmptySessionRemoval(){

        TwingoSession newSession = sessionManager.generateSession();

        assert sessionManager.sessions.containsKey(newSession.getSessionId());

        sessionManager.removeSession(newSession.getSessionId());

        assert !sessionManager.sessions.containsKey(newSession.getSessionId());
    }


    @Test
    public void testAddNewUser(){

        TwingoSession session = sessionManager.generateSession();
        Query query  = new Query("love");

        try {
            sessionManager.selectQuery(session.getSessionId(), query);
        } catch (UnregisteredTwingoUserException e) {
            e.printStackTrace();
        }

        // check if user was added
        assert sessionManager.sessions.containsKey(session.getSessionId());
        assert sessionManager.streamManager.streams.containsKey(query);
        assert sessionManager.streamManager.counts.containsKey(query);
        System.out.println(sessionManager.streamManager.counts.get(query));
        assert sessionManager.streamManager.counts.get(query) == 1;
    }

    @Test
    public void testRemoveUserWithQueryNotUsed() throws UnregisteredTwingoUserException {
        TwingoSession session = sessionManager.generateSession();
        Query query  = new Query("love");

        sessionManager.selectQuery(session.getSessionId(), query);

        assert sessionManager.sessions.containsKey(session.getSessionId());
        assert sessionManager.streamManager.streams.containsKey(query);
        assert sessionManager.streamManager.counts.containsKey(query);
        assert sessionManager.streamManager.counts.get(query) == 1;

        sessionManager.removeSession(session.getSessionId());

        assert !sessionManager.sessions.containsKey(session.getSessionId());
        assert !sessionManager.streamManager.streams.containsKey(query);
        assert !sessionManager.streamManager.counts.containsKey(query);
    }

    @Test
    public void removeUserQueryNotUsed() throws UnregisteredTwingoUserException {
        TwingoSession sessionIdUser1 = sessionManager.generateSession();
        TwingoSession sessionIdUser2 = sessionManager.generateSession();
        Query query  = new Query("love");

        sessionManager.selectQuery(sessionIdUser1.getSessionId(), query);
        sessionManager.selectQuery(sessionIdUser2.getSessionId(), query);

        assert sessionManager.sessions.containsKey(sessionIdUser1.getSessionId());
        assert sessionManager.sessions.containsKey(sessionIdUser2.getSessionId());
        assert sessionManager.streamManager.streams.containsKey(query);
        assert sessionManager.streamManager.counts.containsKey(query);
        assert sessionManager.streamManager.counts.get(query) == 2;

        sessionManager.removeSession(sessionIdUser1.getSessionId());

        assert !sessionManager.sessions.containsKey(sessionIdUser1.getSessionId());
        assert sessionManager.sessions.containsKey(sessionIdUser2.getSessionId());
        assert sessionManager.streamManager.streams.containsKey(query);
        assert sessionManager.streamManager.counts.containsKey(query);
        assert sessionManager.streamManager.counts.get(query) == 1;
    }

}
