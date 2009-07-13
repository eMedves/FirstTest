package org.spagic3.core.resources;

import java.io.InputStream;
import java.net.URL;

public interface IResource {

	public InputStream openStream();
	
	public URL asURL();
}
