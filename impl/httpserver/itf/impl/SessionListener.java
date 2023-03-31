package httpserver.itf.impl;

import java.awt.event.ActionListener;

public class SessionListener implements ActionListener {

        private Session m_session;
        private HttpServer m_hs;

        public SessionListener(Session s,HttpServer hs) {
            m_session = s;
            m_hs = hs;
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {

            m_session.restartTimer();
            m_hs.removeSession(m_session.getId());
        }
}
