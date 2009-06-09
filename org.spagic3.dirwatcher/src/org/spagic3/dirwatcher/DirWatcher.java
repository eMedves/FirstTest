package org.spagic3.dirwatcher;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TimerTask;

public abstract class DirWatcher extends TimerTask {

	  private String path;
	  private File filesArray [];
	  private HashMap<File, Long> dir = new HashMap();
	  
	  public HashMap<File, Long> getDir() {
		return dir;
	  }

	  public void setDir(HashMap<File, Long> dir) {
		this.dir = dir;
	  }

	private DirFilterWatcher dfw;

	  public DirWatcher(String path) {
	    this(path, "");
	  }

	  public DirWatcher(String path, String filter) {
	    
		this.path = path;
	    dfw = new DirFilterWatcher(filter);
	    
	    File pathFile = new File(path);
	    if (!pathFile.exists())
	    	pathFile.mkdirs();
	    
	    filesArray = pathFile.listFiles(dfw);

	    // transfer to the hashmap be used a reference and keep the
	    // lastModfied value
	    for(int i = 0; i < filesArray.length; i++) {
	       dir.put(filesArray[i], null);
	    }
	  }
	  
	  

	  public final void run() {
	    HashSet<File> checkedFiles = new HashSet();
	    filesArray = new File(path).listFiles(dfw);

	    
	    // scan the files and check for modification/addition
	    for(int i = 0; i < filesArray.length; i++) {
	      Long current = (Long)dir.get(filesArray[i]);
	      checkedFiles.add(filesArray[i]);
	      if (current == null) {
	        // new file
	        dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
	        onChange(filesArray[i], "add");
	      }
	      else if (current.longValue() != filesArray[i].lastModified()){
	        // modified file
	        dir.put(filesArray[i], new Long(filesArray[i].lastModified()));
	        onChange(filesArray[i], "modify");
	      }
	    }

	    // now check for deleted files
	    Set<File> ref = ((HashMap)dir.clone()).keySet();
	    ref.removeAll((Set)checkedFiles);
	    Iterator<File> it = ref.iterator();
	    while (it.hasNext()) {
	      File deletedFile = (File)it.next();
	      dir.remove(deletedFile);
	      onChange(deletedFile, "delete");
	    }
	  }

	  protected abstract void onChange( File file, String action );


}
