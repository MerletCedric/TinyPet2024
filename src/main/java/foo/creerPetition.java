package foo;
import java.io.IOException;
import java.net.Inet4Address;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
    name = "creerpetition",
    urlPatterns = {"/creerpetition"}
)

public class creerPetition extends HttpServlet {
  
}



