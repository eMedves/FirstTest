package org.spagic3.invokerremote;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.ebpm.client.api.ClientMessage;
import org.eclipse.ebpm.clientproxy.SpagicInvoker;
/**
 * Servlet implementation class SpagicProxyServlet
 */
public class SpagicProxyServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SpagicProxyServlet() {
        super();
       
    }
    
	public void init() throws ServletException {
		System.out.println("Initialized");
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		StringBuffer xmlResponse = new StringBuffer();
		
		String spagicServiceID = request.getParameter("serviceId");
		
		if ((spagicServiceID == null) || (spagicServiceID.trim().length() == 0)){
			xmlResponse.append("<ERROR>");
			xmlResponse.append("Service ID - NOT SPECIFIED");
			xmlResponse.append("</ERROR>");
		}else{
			String requestMessage = request.getParameter("request");
			if ((requestMessage == null) || (requestMessage.trim().length() == 0)){
				xmlResponse.append("<ERROR>");
				xmlResponse.append("RequestMessage - NOT SPECIFIED");
				xmlResponse.append("</ERROR>");
			}else{
				SpagicInvoker invoker = new SpagicInvoker();
			
				requestMessage = requestMessage.trim();
				ClientMessage message = new ClientMessage("testMessage", requestMessage);
				// ATTACHMENT EXAMPLE
//				File file1 = new File("D:/tmp/attachment_test/send_by_web/by_web.pdf");
//				DataSource ds1 = new FileDataSource(file1);
//				DataHandler dh = new DataHandler(ds1);
//
//		
//				message.setAttachment("a1",  SpagicInvoker.fromDataHandler(dh));
				
				Date before = new Date();
				ClientMessage responseFromSpagic = null;
				for (int i = 0; i < 1000; i++) {
					responseFromSpagic = invoker.invokeAndWait(spagicServiceID, message);
				}
				Date after = new Date();
				long time = after.getTime() - before.getTime();
				System.out.println("Time spent: " + time + " msec");
				xmlResponse.append(responseFromSpagic.getBody());
			}
		}
		PrintWriter writer = response.getWriter();
		writer.write(xmlResponse.toString());
		writer.flush();
	
	}
	

		
}
