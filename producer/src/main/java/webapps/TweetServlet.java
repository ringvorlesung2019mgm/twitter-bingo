package webapps;



import producer.PropertyManager;
import producer.Query;
import producer.TweetStream;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

@WebServlet("/TweetStream")
public class TweetServlet extends HttpServlet{

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Transfer-Encoding", "chunked");

        StatusListener sl = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                // System.out.println(status.getText());
                try {
                    response.getWriter().write(status.getText()+ "\r\n");
                    response.getWriter().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {

            }

            @Override
            public void onStallWarning(StallWarning warning) {

            }

            @Override
            public void onException(Exception ex) {

            }
        };

        PropertyManager pm = new PropertyManager();
        Properties p = pm.generalProperties();
        TweetStream s = new TweetStream(p.getProperty("twitter.consumer"),p.getProperty("twitter.consumerSecret"),p.getProperty("twitter.token"),p.getProperty("twitter.tokenSecret"));
        Query q = new producer.Query("love");
        s.stream(q,sl);

        System.out.println("Keep Alive started!");

        for(int i = 0; i < 10000; i++){
            response.getWriter().write("\r\n");
            response.getWriter().flush();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Service Stopped!");
    }

}