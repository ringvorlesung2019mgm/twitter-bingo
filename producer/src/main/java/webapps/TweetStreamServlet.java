package webapps;

import producer.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Properties;


@WebServlet("/TweetStream")
public class TweetStreamServlet extends HttpServlet {

    private UserManager userManager = UserManager.getInstance();

    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        // get parameters from request
        String id = request.getParameter("id");
        String hashtag = request.getParameter("hashtag");

        // set headers for chunked transfer encoding stream
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Transfer-Encoding", "chunked");

        // create user
        TwingoUser twingoUser = userManager.addUser(id, hashtag, request, response);

        PropertyManager pm = new PropertyManager();
        Properties p = pm.generalProperties();

        String kafkaTopicName = "UNI_tweets_" + hashtag + "__";
        KafkaAdapter ad = new KafkaAdapter(pm.producerProperties(),kafkaTopicName);

        TweetStream s = new TweetStream(p.getProperty("twitter.consumer"),p.getProperty("twitter.consumerSecret"),p.getProperty("twitter.token"),p.getProperty("twitter.tokenSecret"));
        Query q = new producer.Query("love");
        s.stream(q,ad);



        while(twingoUser.isActive()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
