package webapps.api;

import producer.PropertyManager;
import producer.SessionManager;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/GetToken")
public class GetTokenServlet extends HttpServlet{

    SessionManager sessionManager = SessionManager.getDefaultInstance();

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write(sessionManager.generateSession().toJSON());
        response.getWriter().flush();
    }


}