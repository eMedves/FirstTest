package org.spagic3.connectors.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.mortbay.io.Buffer;
import org.mortbay.io.BufferUtil;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.util.StringUtil;

public class SpagicJettyHTTPExchange extends HttpExchange {
	int responseStatus;
    HttpFields responseFields;
    String encoding = "utf-8";
    ByteArrayOutputStream responseContent;
    int contentLength;

    public SpagicJettyHTTPExchange() {
        responseFields = new HttpFields();
    }
	
	
    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
        responseStatus = status;
    }

    
    protected void onResponseHeader(Buffer name, Buffer value) throws IOException {
        if (responseFields != null) {
            responseFields.add(name, value);
        }
        int header = HttpHeaders.CACHE.getOrdinal(value);
        switch (header) {
        case HttpHeaders.CONTENT_LANGUAGE_ORDINAL:
            contentLength = BufferUtil.toInt(value);
            break;
        case HttpHeaders.CONTENT_TYPE_ORDINAL:
            String mime = StringUtil.asciiToLowerCase(value.toString());
            int i = mime.indexOf("charset=");
            if (i > 0) {
                mime = mime.substring(i + 8);
                i = mime.indexOf(';');
                if (i > 0) {
                    mime = mime.substring(0, i);
                }
            }
            if (mime != null && mime.length() > 0) {
                encoding = mime;
            }
            break;
        default:
            break;
        }
    }

   
    protected void onResponseContent(Buffer content) throws IOException {
        if (responseContent == null) {
            responseContent = new ByteArrayOutputStream(contentLength);
        }
        content.writeTo(responseContent);
    }

    public byte[] getResponse() throws UnsupportedEncodingException {
        if (responseContent != null) {
            return responseContent.toByteArray();
        }
        return null;
    }
    
    
}
