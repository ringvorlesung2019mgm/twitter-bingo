package producer;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class StreamManagerTest {

    @Test
    public void testAddRemove(){
        PropertyManager pm = new PropertyManager();
        Properties p = pm.allProperties();
        StreamManager.setCleanupTaskInterval(1);
        StreamManager m = StreamManager.getInstance(p);
        m.setRemovalTimeout(3);

        Assert.assertEquals(0,m.activeStreams());

        Query q1 = new Query("love");
        Query q2 = new Query("hate");


        // Create string using constructor to work arount java's string-pooling
        Query otherq1 = new Query(new String("love"));


        m.requestStream(q1);
        Assert.assertEquals(1,m.activeStreams());
        Assert.assertEquals(1,m.currentUsage(q1));

        m.requestStream(otherq1);
        Assert.assertEquals(1,m.activeStreams());
        Assert.assertEquals(2,m.currentUsage(q1));

        m.requestStream(q2);
        Assert.assertEquals(2,m.activeStreams());
        Assert.assertEquals(1,m.currentUsage(q2));

        m.releaseStream(q1);
        Assert.assertEquals(2,m.activeStreams());
        Assert.assertEquals(1,m.currentUsage(q1));

        m.releaseStream(q1);
        Assert.assertEquals(2,m.activeStreams());
        Assert.assertEquals(0,m.currentUsage(q1));

        try {
            Thread.sleep(1000);
            // This is to early. No stream should be deleted at this time
            Assert.assertEquals(2,m.activeStreams());
            Assert.assertEquals(0,m.currentUsage(q1));

            //Now q1 should be gone and q2 should still be here
            Thread.sleep(7000);
            Assert.assertEquals(1,m.activeStreams());
            Assert.assertEquals(1,m.currentUsage(q2));


        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        m.requestStream(q2);


    }
}
