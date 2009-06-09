package org.spagic3.core;

import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

public class SpagicUtils {

	protected final static TransformerFactory tf = TransformerFactory
			.newInstance();

	public static URL getURL(String uriString) throws Exception {
		URI uri = new URI(uriString);
		return uri.toURL();
	}

	public static String normalizeTopic(String topic) {
		return topic.replaceAll("\\.", "-");
	}

	/**
	 * Converts the given input Source into the required result
	 */
	protected static void toResult(Source source, Result result)
			throws TransformerException {

		if (source == null) {
			return;
		}
		Transformer transformer = tf.newTransformer();
		if (transformer == null) {
			throw new TransformerException(
					"Could not create a transformer - JAXP is misconfigured!");
		}
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.transform(source, result);

	}

	/**
	 * Converts the given input Source into text
	 */
	public static String toString(Source source) {
		try {
			if (source == null) {
				return null;
			} else {
				StringWriter buffer = new StringWriter();
				toResult(source, new StreamResult(buffer));
				return buffer.toString();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
