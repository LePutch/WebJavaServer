package httpserver.itf.impl;

import httpserver.itf.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;

public class HttpRicmletRequestImpl extends HttpRicmletRequest{
    //Hashtable of arguments
    private Hashtable<String, String> m_args = new Hashtable<String, String>();
    private Hashtable<String, String> m_cookies = new Hashtable<String, String>();


    public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
        super(hs, method, ressname, br);
        //Take the cookies
        String line = br.readLine();
        while (line != null && !line.equals("")){
            if (line.contains("Cookie: ")){
                String[] cookies = line.split("Cookie: ")[1].split("; ");
                for (String cookie : cookies){
                    String[] cookieSplit = cookie.split("=");
                    m_cookies.put(cookieSplit[0], cookieSplit[1]);
                }
            }
            line = br.readLine();
        }
    }



    @Override
    public void process(HttpResponse resp) throws Exception {
        //Checking the method
        if (m_method.equals("GET")){
            //Take the name of the ricmlet to launch
            String rawName = m_ressname.substring(10).replace("/", ".");

            //Take the name of the ricmlet before "?"
            String ricmletName = rawName.split("\\?")[0];

            /*Debug
            System.out.println("Raw name: " + rawName);
            System.out.println("Ricmlet name: " + ricmletName);
            */

            //Take the arguments if there are
            if (rawName.contains("?")){
                //If there are no arguments, send a 404 error
                if (rawName.split("\\?").length == 1){
                    resp.setReplyError(404, "No arguments \n");
                    return;
                }
                //Split the arguments
                String[] args = rawName.split("\\?")[1].split("&");
                for (String arg : args){
                    //Split the argument in two: name and value
                    String[] argSplit = arg.split("=");

                    //Add the argument to the hashtable
                    m_args.put(argSplit[0], argSplit[1]);
                }
            }
            try{
                //try to launch the ricmlet
                HttpRicmlet launchRicmlet = m_hs.getInstance(ricmletName);
                launchRicmlet.doGet(this, (HttpRicmletResponse)resp);
            }
            catch (Exception e){
                //if the ricmlet doesn't exist, send a 404 error
                e.printStackTrace();
                resp.setReplyError(404, "Ricmlet can't be launch \n");
            }

        }
    }


    @Override
    synchronized public HttpSession getSession() {
        String sessionID = m_cookies.get("sessionID");
        if (sessionID == null){
            sessionID = m_hs.newSession();
            m_cookies.put("sessionID", sessionID);
        }
        return m_hs.getSession(sessionID);
    }

    @Override
    public String getArg(String name) {
        return m_args.get(name);
    }

    @Override
    public String getCookie(String name) {
        return m_cookies.get(name);
    }
}
