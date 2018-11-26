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

    private UserManager userManager = UserManager.getInstance();
    private PropertyManager pm = new PropertyManager();
    private Properties p = pm.generalProperties();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // get parameters from request
        String id = request.getParameter("id");
        String hashtag = request.getParameter("hashtag");

        // set headers for chunked transfer encoding stream
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Transfer-Encoding", "chunked");

        // create user
        TwingoUser twingoUser = userManager.addUser(id, hashtag, request, response);

        String kafkaTopicName = "UNI_tweets_" + hashtag + "__";
        KafkaAdapter ad = new KafkaAdapter(pm.producerProperties(),kafkaTopicName);

        TweetStream s = new TweetStream(p.getProperty("twitter.consumer"),p.getProperty("twitter.consumerSecret"),p.getProperty("twitter.token"),p.getProperty("twitter.tokenSecret"));
        Query q = new producer.Query("love");
        s.stream(q,ad);

        KafkaConsumer<String,String> cons = new KafkaConsumer<>(new PropertyManager().consumerProperties());

        cons.subscribe(Collections.singletonList(kafkaTopicName));


        while(twingoUser.isActive()){
            waitForAssignments(cons,10000);
            cons.seekToEnd(cons.assignment());
            //seek is lazy. Needs to be followed by poll or position to actually do anything
            // cons.position((TopicPartition)cons.assignment().toArray()[0]);

            ConsumerRecords<String, String> consumerRecords = cons.poll(Duration.ofSeconds(10L));

            for(ConsumerRecord record : consumerRecords.records(kafkaTopicName)){

                Random random = new Random();
                JSONObject obj = new JSONObject();
                try {
                    obj.put("status", record.value());
                    obj.put("rating", (random.nextDouble() - 0.5) * 50);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                response.getWriter().write(obj.toString() + "\r\n");
                response.getWriter().flush();

            }
        }
    }

    /** Wait until the list of assignments for the consumer is not longer empty
     *
     * @param cons The consumer
     * @param timeout Timeout in milliseconds after which to give up
     */
    private void waitForAssignments(KafkaConsumer cons, long timeout){
        long waited = 0;
        while(waited < timeout){
            cons.poll(Duration.ofMillis(100));
            if (cons.assignment().size() > 0){
                return;
            }
            waited++;
        }
        throw new IllegalStateException("Consumer did not get assignments in time");
    }

}
