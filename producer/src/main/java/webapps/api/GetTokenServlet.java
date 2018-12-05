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

    PropertyManager pm = new PropertyManager();
    SessionManager sessionManager = SessionManager.getInstance(pm.allProperties(), pm.sessionManagerProperties());

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write(sessionManager.generateSession().toJSON());
        response.getWriter().flush();
    }


}