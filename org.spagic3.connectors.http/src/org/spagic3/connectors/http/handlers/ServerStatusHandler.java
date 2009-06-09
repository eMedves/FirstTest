package org.spagic3.connectors.http.handlers;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.util.StringUtil;
import org.spagic3.connectors.http.HttpServerManager;

public class ServerStatusHandler extends AbstractHandler {

    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
        throws IOException, ServletException {
        if (response.isCommitted() || HttpConnection.getCurrentConnection().getRequest().isHandled()) {
            return;
        }
            
        String method = request.getMethod();

        if (!method.equals(HttpMethods.GET) || !request.getRequestURI().equals("/")) {
            response.sendError(404);
            return;
        }

        response.setStatus(404);
        response.setContentType(MimeTypes.TEXT_HTML);

        org.mortbay.util.ByteArrayISO8859Writer writer = new org.mortbay.util.ByteArrayISO8859Writer(1500);

        String uri = request.getRequestURI();
        uri = StringUtil.replace(uri, "<", "&lt;");
        uri = StringUtil.replace(uri, ">", "&gt;");

        writer.write("<HTML>\n<HEAD>\n<TITLE>Error 404 - Not Found");
        writer.write("</TITLE>\n<BODY>\n<H2>Error 404 - Not Found.</H2>\n");
        writer.write("No service matched or handled this request.<BR>");
        writer.write("Known services are: <ul>");

        HttpServerManager.writeStatus(writer);
        
        writer.write("\n</BODY>\n</HTML>\n");
        writer.flush();
        response.setContentLength(writer.size());
        OutputStream out = response.getOutputStream();
        writer.writeTo(out);
        out.close();
    }

}

