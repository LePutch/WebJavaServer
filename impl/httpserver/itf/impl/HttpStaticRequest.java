package httpserver.itf.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;


import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;

/*
 * This class allows to build an object representing an HTTP static request
 */
public class HttpStaticRequest extends HttpRequest {
	static final String DEFAULT_FILE = "index.html";
	
	public HttpStaticRequest(HttpServer hs, String method, String ressname) throws IOException {
		super(hs, method, ressname);
	}
	
	public void process(HttpResponse resp) throws Exception {
		//Checking the method
		if (m_method.equals("GET")) {
			//Checking the resource name
			if (m_ressname.endsWith("/")) m_ressname += DEFAULT_FILE;

			//Checking the file
			File fileRequested = new File(this.m_hs.getFolder(), m_ressname);
			if (fileRequested.exists()) {

				//Sending the file
				FileInputStream fis = new FileInputStream(fileRequested);
				resp.setReplyOk();

				//need to send the size and the content type:
				resp.setContentLength((int) (fileRequested.length()));
				resp.setContentType(getContentType(m_ressname));

				//Printing the body
				PrintStream ps = resp.beginBody();
				ps.write(fis.readAllBytes());
				ps.flush();
				fis.close();

			} else {
				resp.setReplyError(404, "File not found \n");
			}
		}
	}


}
