package webapps.api;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import producer.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;


@WebServlet("/api/TweetStream")
public class TweetStreamServlet extends HttpServlet {

    PropertyManager pm = new PropertyManager();
    SessionManager sessionManager = SessionManager.getDefaultInstance();

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // read request data
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        }
        TwingoStreamRequest data = TwingoStreamRequest.fromJson(sb.toString());
        UUID sessionId = data.sessionId;
        String hashtag = data.hashtag;

        // set headers for chunked transfer encoding stream
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Transfer-Encoding", "chunked");


        Query query = new producer.Query(hashtag);
        String kafkaRawTopic = sessionManager.streamManager.getRawTopicFromQuery(query);
        String kafkaAnalysedTopic = sessionManager.streamManager.getAnalysedTopicFromQuery(query);

        // create stream from query
        try {
            sessionManager.selectQuery(sessionId, query);
        } catch (UnregisteredTwingoUserException e) {
            e.printStackTrace();
        }

        // create demo consumer
        KafkaConsumer<String, String> cons = new KafkaConsumer<>(pm.consumerProperties());
        cons.subscribe(Collections.singletonList(kafkaAnalysedTopic));

        // read from consumer and write to frontend
        // TODO make fancy
        while (!response.getWriter().checkError()) {
            waitForAssignments(cons, 10000);
            cons.seekToEnd(cons.assignment());
            ConsumerRecords<String, String> consumerRecords = cons.poll(Duration.ofSeconds(10L));

            // TODO do something less stupid
            sessionManager.scheduleRemoveSessionDefaultTimeout(sessionId);
            for (ConsumerRecord record : consumerRecords.records(kafkaAnalysedTopic)) {
                response.getWriter().write(record.value().toString() + "\r\n");
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
