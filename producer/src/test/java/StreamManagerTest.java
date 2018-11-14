import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class StreamManagerTest {

    @Test
    public void testAddRemove(){
        PropertyManager pm = new PropertyManager();
        Properties p = pm.allProperties();
        StreamManager m = new StreamManager(p);

        Assert.assertEquals(0,m.activeQueries());

        Query q1 = new Query("love");
        Query q2 = new Query("hate");

        m.addStream(q1);
        Assert.assertEquals(1,m.activeQueries());

        m.addStream(q1);
        Assert.assertEquals(1,m.activeQueries());

        m.addStream(q2);
        Assert.assertEquals(2,m.activeQueries());

        m.removeStream(q1);
        Assert.assertEquals(2,m.activeQueries());

        m.removeStream(q2);
        Assert.assertEquals(1,m.activeQueries());

        m.removeStream(q1);
        Assert.assertEquals(0,m.activeQueries());

    }
}
