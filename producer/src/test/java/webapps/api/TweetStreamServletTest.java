package webapps.api;

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

public class TweetStreamServletTest {


    @Test
    public void testServletWithoutQuery() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        Mockito.when(request.getParameter("q")).thenReturn(null);

        TweetStreamServlet tweetStreamServlet = new TweetStreamServlet();
        tweetStreamServlet.doPost(request, response);

        verify(response, times(1)).sendError(400, "Please add a query parameter(q) to this API-Call.");
    }

    @Test
    public void testServlet() throws IOException, InterruptedException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter outputWriter = new PrintWriter(stringWriter);

        Mockito.when(request.getParameter("q")).thenReturn("love");
        Mockito.when(response.getWriter()).thenReturn(outputWriter);

        TweetStreamServlet tweetStreamServlet = new TweetStreamServlet();
        tweetStreamServlet.doPost(request, response);

        Thread.sleep(5000);

        assert stringWriter.toString() != "";

    }

}
