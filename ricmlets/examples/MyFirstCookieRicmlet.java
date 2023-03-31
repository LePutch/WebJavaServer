package examples;

import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpRicmletResponse;

import java.io.IOException;
import java.io.PrintStream;

public class MyFirstCookieRicmlet implements httpserver.itf.HttpRicmlet{

    @Override
    public void doGet(HttpRicmletRequest req, HttpRicmletResponse resp) throws IOException {

        resp.setReplyOk();
        resp.setContentType("text/html");

        String mfc = req.getCookie("MyFirstCookie");
        String secondCookie=req.getCookie("MySecondCookie");
        String value;
        String value1;
        String countHello;

        if (mfc == null||secondCookie==null) {
            value = "1";
            resp.setCookie("MyFirstCookie", value);
            value1 = "Hello for the first time ! ";
            resp.setCookie("MySecondCookie",value);
        } else {
            int n = Integer.valueOf(mfc);
            value = Integer.valueOf(n + 1).toString();
            resp.setCookie("MyFirstCookie", value);
            countHello = Integer.valueOf(n + 1).toString();
            value1= "Hello for the "+countHello;
            resp.setCookie("MySecondCookie",value1);
        }

        PrintStream ps = resp.beginBody();
        ps.println("<HTML><HEAD><TITLE> Ricmlet processing </TITLE></HEAD>");
        ps.print("<BODY><H4> MyFirstCookie " + value + "<br>");
        if (value1.equals("Hello for the first time ! ")){
            ps.print("<BODY><H4> MySecondCookie " + value1 +"<br>");
        }
        else {
            ps.print("<BODY><H4> MySecondCookie " + value1 + " times"+"<br>");
        }
        ps.println("</H4></BODY></HTML>");
        ps.println();

    }

}
