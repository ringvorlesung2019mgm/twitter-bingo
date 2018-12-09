package producer;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;

public class MongoAdapterTest {

    @Test
    public void testMongoAdapter(){
        MongoClient client = new MongoClient();
        MongoCollection col = client.getDatabase("twitter").getCollection("testtweets");
        TwingoTweet testTweet1 = new TwingoTweet(123, "My test tweet #love #whatever", "trump", Calendar.getInstance().getTime(), false, 0, 0, true, 2, Arrays.asList("love","whatever"));
        TwingoTweet testTweet2 = new TwingoTweet(456, "My test tweet #hate #winter", "trump", Calendar.getInstance().getTime(), false, 0, 0, true, 2, Arrays.asList("hate","winter"));

        col.drop();
        col.insertOne(Document.parse(testTweet1.toJSON()));
        col.insertOne(Document.parse(testTweet2.toJSON()));

        MongoAdapter m = new MongoAdapter("localhost:27017");

        Query query1 = new Query("love");
        Query query2 = new Query("winter");

        Iterator<Document> resultset1 = m.stream(query1,"twitter","testtweets");
        Assert.assertTrue(resultset1.hasNext());
        Document d1 = resultset1.next();
        TwingoTweet tw1 = TwingoTweet.fromJson(d1.toJson());
        Assert.assertEquals(testTweet1.toJSON(), tw1.toJSON());

        Iterator<Document> resultset2 = m.stream(query2,"twitter","testtweets");
        Assert.assertTrue(resultset2.hasNext());
        Document d2 =  resultset2.next();
        TwingoTweet tw2 = TwingoTweet.fromJson(d2.toJson());
        Assert.assertEquals(testTweet2.toJSON(), tw2.toJSON());

        TwingoTweet testTweet3 = new TwingoTweet(789, "Another tweet #hate #winter", "trump", Calendar.getInstance().getTime(), false, 0, 0, true, 2, Arrays.asList("hate","winter"));

        // Insert a new document and wait for it to appear on the matching stream (and ONLY the MATCHING stream)
        col.insertOne(Document.parse(testTweet3.toJSON()));


        Object next = ((MongoAdapter.ResultCursor) resultset2).tryNext();

        Assert.assertNotNull(next);
        Document d3 =  (Document)next;
        TwingoTweet tw3 = TwingoTweet.fromJson(d3.toJson());
        Assert.assertEquals(tw3.toJSON(), tw3.toJSON());

        Assert.assertNull(((MongoAdapter.ResultCursor) resultset1).tryNext());


    }
}
