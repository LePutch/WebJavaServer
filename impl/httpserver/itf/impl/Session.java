package httpserver.itf.impl;

import javax.swing.*;
import java.util.HashMap;

public class Session implements httpserver.itf.HttpSession{

    public HashMap<String, Object> m_session;
    private Timer timer;
    public String id;

    public Session(String id,HttpServer hs) {
        this.id = id;
        m_session = new HashMap<String, Object>();
        timer = new Timer(10000,new SessionListener(this,hs));
        timer.start();
    }
    @Override
    public String getId() {
        return this.id;
    }

    public void restartTimer() {
        timer.restart();
    }

    @Override
    public Object getValue(String key) {
        restartTimer();
        return m_session.get(key);
    }

    @Override
    public void setValue(String key, Object value) {
        m_session.put(key,value);
    }

    public HashMap<String, Object> getM_session() {
        return m_session;
    }

    public Timer getTimer() {
        return timer;
    }
    public void stopTimer() {
        timer.stop();
    }
}
