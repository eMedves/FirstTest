package org.spagic3.embedded.proxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.equinox.servletbridge.SpagicServlet;
import org.spagic3.client.api.Client;
import org.spagic3.client.api.ClientMessage;

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
				Client spagicClient = getSpagicClient();
			
				requestMessage = requestMessage.trim();
				ClientMessage message = new ClientMessage("testMessage", requestMessage);
				ClientMessage responseFromSpagic = invokeService(spagicClient,spagicServiceID,message);
				xmlResponse.append(responseFromSpagic.getBody());
			}
		}
			
		
		
		PrintWriter writer = response.getWriter();
		writer.write(xmlResponse.toString());
		writer.flush();
	
	}
	
	public Client getSpagicClient(){
		Client client = (Client) getServletContext().getAttribute(SpagicServlet.SPAGIC_DELEGATE);
		return client;
	}
		
		
	public ClientMessage invokeService(Client clientAPI, String serviceId, ClientMessage requestMessage ){
	
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(SpagicServlet.getFrameworkContextClassLoader());
			ClientMessage response = null;
			if (clientAPI != null) {
				
				response = clientAPI.invokeAndWait(serviceId, requestMessage);
				System.out.println("Message As Body" + response.getBody());
			}
			return response;
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		
		}
		
	}
		
}
