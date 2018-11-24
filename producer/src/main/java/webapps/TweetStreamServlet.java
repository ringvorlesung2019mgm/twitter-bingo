package webapps;

import producer.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@WebServlet("/TweetStream")
public class TweetStreamServlet extends HttpServlet {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String id = request.getParameter("id");
        String hashtag = request.getParameter("hashtag");


        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Transfer-Encoding", "chunked");

        System.out.println("I am here!");

        UserManager userManager = UserManager.getInstance();
        TwingoUser twingoUser = userManager.addUser(id, hashtag, request, response);

        // TODO workaround, fix this, keep Tomcat from recycling the response
        while(true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
