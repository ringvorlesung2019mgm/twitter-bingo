package webapps.api;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.bson.Document;
import producer.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;


@WebServlet("/api/TweetStream")
public class TweetStreamServlet extends HttpServlet {

    PropertyManager pm = new PropertyManager();
    StreamManager sm = StreamManager.getInstance(pm.allProperties());

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request,response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String querystring = request.getParameter("q");
        if (querystring == null){
            response.sendError(400,"Please add a query parameter(q) to this API-Call.");
            return;
        }

        Query query = new Query(querystring);

        // set headers for chunked transfer encoding stream
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Transfer-Encoding", "chunked");

        // listen for new tweets matching the query in mongodb
        MongoAdapter mad = new MongoAdapter(pm.allProperties().getProperty("mongodb"));
        MongoAdapter.ResultCursor cur = mad.stream(query);

        //request that a stream is started for the query
        sm.requestStream(query);
        while (!response.getWriter().checkError()) {

            Document tweet = cur.next();

            response.getWriter().write(tweet.toJson() + "\r\n");
            response.getWriter().flush();

        }

        //Tell the StreamManager that we are not longer interested in tweets for the query
        sm.releaseStream(query);
    }

    /**
     * Wait until the list of assignments for the consumer is not longer empty
     * TODO move to utilty because it is used in test and here
     *
     * @param cons    The consumer
     * @param timeout Timeout in milliseconds after which to give up
     */
    private void waitForAssignments(KafkaConsumer cons, long timeout) {
        long waited = 0;
        while (waited < timeout) {
            cons.poll(Duration.ofMillis(100));
            if (cons.assignment().size() > 0) {
                return;
            }
            waited++;
        }
        throw new IllegalStateException("Consumer did not get assignments in time");
    }

}
