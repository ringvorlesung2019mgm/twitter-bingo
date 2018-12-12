package webapps.api;

import org.apache.kafka.common.protocol.types.Field;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

public class TweetStreamServletTest {


    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletResponse response;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private HttpServletRequest request;

    /**
     * Tests if correct exception is thrown when no query is given.
     *
     * @throws IOException
     */
    @Test
    public void testServletWithoutQuery() throws IOException {

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        Mockito.when(request.getParameter("q")).thenReturn(null);

        TweetStreamServlet tweetStreamServlet = new TweetStreamServlet();
        tweetStreamServlet.doPost(request, response);

        verify(response, times(1)).sendError(400, "Please add a query parameter(q) to this API-Call.");
    }

/*    *//**
     * Tests if tweets are beeing streamed for hashtag "love".
     *
     * @throws IOException
     * @throws InterruptedException
     *//*
    @Test
    public void testServlet() throws IOException, InterruptedException {

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter outputWriter = new PrintWriter(stringWriter);

        Mockito.when(request.getParameter("q")).thenReturn("love");
        Mockito.when(response.getWriter()).thenReturn(outputWriter);

        TweetStreamServlet tweetStreamServlet = new TweetStreamServlet();
        tweetStreamServlet.doPost(request, response);

        Thread.sleep(10000);

        Mockito.when(response.getWriter().checkError()).thenReturn(true);

        assert stringWriter.toString() != "";
    }


    *//**
     * Tests if the correct headers were set in TweetStreamServlet response.
     *
     * @throws IOException
     *//*
    @Test
    public void testServletHeader() throws IOException, InterruptedException {

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter outputWriter = new PrintWriter(stringWriter);

        Mockito.when(request.getParameter("q")).thenReturn("love");
        Mockito.when(response.getWriter()).thenReturn(outputWriter);

        TweetStreamServlet tweetStreamServlet = new TweetStreamServlet();
        tweetStreamServlet.doPost(request, response);

        // TODO do different, this is more of a hotfix
        verify(response, times(1)).setHeader("Access-Control-Allow-Origin", "*");
        verify(response, times(1)).setCharacterEncoding("UTF-8");
        verify(response, times(1)).setContentType("text/json");
    }*/
}
