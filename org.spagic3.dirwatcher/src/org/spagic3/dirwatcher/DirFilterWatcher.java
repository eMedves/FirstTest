package org.spagic3.dirwatcher;

import java.io.File;

public class DirFilterWatcher implements java.io.FileFilter {
	
	  private String[] filters;

	  public DirFilterWatcher() {
	    this.filters = new String[0];
	  }

	  public DirFilterWatcher(String filter) {
	    this(new String[]{filter});
	  }
	  
	  public DirFilterWatcher(String[] filters) {
		    this.filters = filters;
	  }
	  
	  public boolean accept(File file) {
		  if (filters.length == 0){
			  return true;
		  }else{
			  String fileName = file.getName().toLowerCase();
			  for ( int i =0; i < filters.length; i++){
	    		if (fileName.endsWith(filters[i]))
	    	        return true;
			  	}
			  return false;
		  }
	  }

}
