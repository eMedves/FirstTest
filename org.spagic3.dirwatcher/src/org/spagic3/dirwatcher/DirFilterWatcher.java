package org.spagic3.dirwatcher;

import java.io.File;

public class DirFilterWatcher implements java.io.FileFilter {
	private String filter;

	  public DirFilterWatcher() {
	    this.filter = "";
	  }

	  public DirFilterWatcher(String filter) {
	    this.filter = filter;
	  }
	  
	  public boolean accept(File file) {
	    if ("".equals(filter)) {
	      return true;
	    }
	    return (file.getName().endsWith(filter));
	  }

}
