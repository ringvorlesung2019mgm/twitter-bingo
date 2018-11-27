package webapps;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import producer.*;
import twitter4j.JSONException;
import twitter4j.JSONObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;


@WebServlet("/TweetStream")
public class TweetStreamServlet extends HttpServlet {

    PropertyManager pm = new PropertyManager();
    StreamManager m = StreamManager.getInstance(pm.allProperties());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // set headers for chunked transfer encoding stream
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Transfer-Encoding", "chunked");

        // get parameters from request
        String id = request.getParameter("id");
        String hashtag = request.getParameter("hashtag");
        Query query = new producer.Query(hashtag);
        String kafkaTopic = m.topicFromQuery(query);

        // create stream from query
        m.addStream(query);

        // create demo consumer
        KafkaConsumer<String, String> cons = new KafkaConsumer<>(pm.consumerProperties());
        cons.subscribe(Collections.singletonList(kafkaTopic));

        // read from consumer and write to frontend
        // TODO make fancy
        while (!response.getWriter().checkError()) {

            waitForAssignments(cons, 10000);
            cons.seekToEnd(cons.assignment());
            ConsumerRecords<String, String> consumerRecords = cons.poll(Duration.ofSeconds(10L));

            for (ConsumerRecord record : consumerRecords.records(kafkaTopic)) {
                TwingoTweet twingoTweet = TwingoTweet.fromJson(record.value().toString());
                // generate random analyser rating
                try {

                    Random random = new Random();
                    twingoTweet.setRating((random.nextDouble() - 0.5) * 50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                response.getWriter().write(twingoTweet.toJSON() + "\r\n");
                response.getWriter().flush();

            }
        }
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
