package org.spagic3.connectors.http.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.spagic3.connectors.http.HTTPServer;

public class SpagicServlet extends HttpServlet{
	
	   	private HTTPServer spagicService = null;

	    public void init(ServletConfig config) throws ServletException {
	        super.init(config);
	        if (spagicService == null) {
	        	spagicService = (HTTPServer) getServletContext().getAttribute("spagicService");
	            if (spagicService == null) {
	                throw new ServletException("Spagic Service Not Bound");
	            }
	        }
	    }

	    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	        try {
	        	spagicService.processHttp(request, response);
	        }catch (IOException e) {
	        	throw e;
	        }catch (RuntimeException e) {
	        	throw e;
	        }catch (Exception e) {
	            throw new ServletException("Failed to process request: " + e, e);
	        }
	    }

	    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                    IOException {
	        try {
	        	spagicService.processHttp(request, response);
	        }catch (IOException e) {
	        	throw e;
	        }catch (RuntimeException e) {
	        	throw e;
	        }catch (Exception e) {
	            throw new ServletException("Failed to process request: " + e, e);
	        }
	    }


}
