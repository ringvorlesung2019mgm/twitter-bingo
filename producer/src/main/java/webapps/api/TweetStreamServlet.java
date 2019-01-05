package webapps.api;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import producer.*;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Receives a query and streams rated tweets matching this query to the user.
 *
 * @author jp & db
 */
@WebServlet("/api/TweetStream")
public class TweetStreamServlet extends HttpServlet {

    final Logger logger = LoggerFactory.getLogger(TweetStreamServlet.class);

    PropertyManager pm = new PropertyManager();
    StreamManager sm = StreamManager.getInstance(new StreamManager.DefaultStreamFactory(pm.allProperties()));

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        logger.info(request.getSession().getId() + "Received a query");

        String querystring = request.getParameter("q");
        if (querystring == null) {
            response.sendError(400, "Please add a query parameter(q) to this API-Call.");
            logger.debug(request.getSession().getId() + " -> Error 400: Please add a query parameter(q) to this API-Call.");
            return;
        }

        if (querystring.equals("")) {
            response.sendError(400, "Query-string can not be empty. Maybe you included a # without URL-encoding the string?");
            logger.debug(request.getSession().getId() + " -> Error 400: Query-string can not be empty. Maybe you included a # without URL-encoding the string?");
            return;
        }

        // query strings should be URL-encoded. Decode them!
        querystring = URLDecoder.decode(querystring,"UTF-8");

        logger.info(request.getSession().getId() + " Query is " + querystring);

        // The Query Class will handle filtering and normalizing the string
        Query query = new Query(querystring);

        // set headers for chunked transfer encoding stream
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json");

        // listen for new tweets matching the query in mongodb
        MongoAdapter mad = new MongoAdapter(pm.allProperties().getProperty("mongodb"));
        MongoAdapter.ResultCursor cur = mad.stream(query);

        logger.info(request.getSession().getId() + " Stream started");

        //request that a stream is started for the query
        sm.requestStream(query);
        while (!response.getWriter().checkError()) {

            Document tweet = cur.next();

            response.getWriter().write(tweet.toJson() + "\r\n");
            response.getWriter().flush();
        }

        //Tell the StreamManager that we are not longer interested in tweets for the query
        sm.releaseStream(query);
        logger.info(request.getSession().getId() + " Stream released");

    }

}
