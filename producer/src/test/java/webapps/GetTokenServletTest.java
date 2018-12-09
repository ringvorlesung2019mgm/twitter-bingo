package webapps;

import org.junit.Test;
import webapps.api.GetTokenServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;

public class GetTokenServletTest {

    @Test
    public void testServlet(){
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        GetTokenServlet servlet = new GetTokenServlet();
        servlet.doGet(request, response);
    }


}
