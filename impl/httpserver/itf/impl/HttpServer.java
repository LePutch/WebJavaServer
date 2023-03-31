package httpserver.itf.impl;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Hashtable;
import java.util.StringTokenizer;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;
import httpserver.itf.HttpRicmlet;
import httpserver.itf.HttpSession;


/**
 * Basic HTTP Server Implementation 
 * 
 * Only manages static requests
 * The url for a static ressource is of the form: "http//host:port/<path>/<ressource name>"
 * For example, try accessing the following urls from your brower:
 *    http://localhost:<port>/
 *    http://localhost:<port>/voile.jpg
 *    ...
 */
public class HttpServer {

	private int m_port;
	private File m_folder;  // default folder for accessing static resources (files)
	private ServerSocket m_ssoc;
	public Hashtable<String, HttpRicmlet> instance;
	public Hashtable<String, HttpSession> sessions;
	private int session;

	protected HttpServer(int port, String folderName) {
		session = 0;
		m_port = port;
		if (!folderName.endsWith(File.separator)) 
			folderName = folderName + File.separator;
		m_folder = new File(folderName);
		try {
			m_ssoc=new ServerSocket(m_port);
			System.out.println("HttpServer started on port " + m_port);
		} catch (IOException e) {
			System.out.println("HttpServer Exception:" + e );
			System.exit(1);
		}
		instance = new Hashtable<String, HttpRicmlet>();
		sessions = new Hashtable<String, HttpSession>();
	}
	
	public File getFolder() {
		return m_folder;
	}
	
	

	public HttpRicmlet getInstance(String clsname)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException, 
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		HttpRicmlet inst = this.instance.get(clsname);
		if(inst==null) {
			// instanciate the class and add it to the hashtable of instances
			Class<?> c = Class.forName(clsname);
			inst = ((HttpRicmlet) c.getDeclaredConstructor().newInstance());
			this.instance.put(clsname, inst);
		}
		return inst;
	}





	/*
	 * Reads a request on the given input stream and returns the corresponding HttpRequest object
	 */
	public HttpRequest getRequest(BufferedReader br) throws IOException {
		HttpRequest request = null;
		
		String startline = br.readLine();
		StringTokenizer parseline = new StringTokenizer(startline);
		String method = parseline.nextToken().toUpperCase(); 
		String ressname = parseline.nextToken();
		if (method.equals("GET")) {
			String redirection = redirection(ressname);
			if (redirection.equals("static")) {
				System.out.println("Static GET request for " + ressname);
				request = new HttpStaticRequest(this, method, ressname);
			}
			else if (redirection.equals("dynamic")) {
				System.out.println("Dynamic GET request for " + ressname);
				request = new HttpRicmletRequestImpl(this, method, ressname, br);
			}
		} else 
			request = new UnknownRequest(this, method, ressname);
		return request;
	}

	/*
	 * Reads an url and returns the corresponding HttpRequest object if there is an extension or not
	 */
	public String redirection(String url) throws IOException {
		//checking if there is an extension
		if(url.contains("ricmlets")) {
			return "dynamic";
		}
		else {
			return "static";
		}
	}

	/*
	 * Returns an HttpResponse object associated to the given HttpRequest object
	 */
	public HttpResponse getResponse(HttpRequest req, PrintStream ps) {
		if (req instanceof HttpRicmletRequestImpl) {
			return new HttpRicmletResponseImpl(this, req, ps);
		}else {
			return new HttpResponseImpl(this, req, ps);
		}
	}


	/*
	 * Server main loop
	 */
	protected void loop() {
		try {
			while (true) {
				Socket soc = m_ssoc.accept();
				(new HttpWorker(this, soc)).start();
			}
		} catch (IOException e) {
			System.out.println("HttpServer Exception, skipping request");
			e.printStackTrace();
		}
	}

	
	
	public static void main(String[] args) {
		int port = 0;
		if (args.length != 2) {
			System.out.println("Usage: java Server <port-number> <file folder>");
		} else {
			port = Integer.parseInt(args[0]);
			String foldername = args[1];
			HttpServer hs = new HttpServer(port, foldername);
			hs.loop();
		}
	}

	synchronized public String newSession() {
		this.session++;
		return String.valueOf(this.session);
	}

	synchronized HttpSession getSession(String id) {
		HttpSession res= this.sessions.get(id);
		if(res==null) {
			res = new Session(id,this);
			this.sessions.put(id, res);
		}
		return res;
	}

	public Collection<HttpSession> getAllSessions() {
		return this.sessions.values();
	}

	public void removeSession(String id) {
		Session s = (Session) this.sessions.get(id);
		s.stopTimer();
		this.sessions.remove(id);
	}
}

